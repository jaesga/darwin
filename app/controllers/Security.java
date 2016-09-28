package controllers;

import com.elevenpaths.api.token.IdToken;
import models.Config;
import models.Constants;
import models.factory.DarwinFactory;
import models.latch.AsyncLatchHandler;
import models.latch.LatchSDK;
import models.mobileConnect.MobileConnectSDK;
import models.roles.UserRole;
import models.user.User;
import models.utils.DateUtils;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.data.validation.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class Security extends Secure.Security {

    public final static String DEFAULT_INVALID_CHAR = "_";
    private static final String VALIDATION_TOKEN_KEY = "token";

    private static final String SESSION_OTP_REQUIRED = "sor";
    private static final String SESSION_OTP_OPERATION_ID = "oid";
    private static final String SESSION_ACCOUNTS_CHOICES = "sac";
    private static final String SESSION_MC_STATE = "smcs";
    private static final String SESSION_LAST_ACCESS = "sla";
    private static final String SESSION_CREATED_AT = "sca";
    private static final String CACHE_EXPIRATION = Play.configuration.getProperty("latch.otp.attempts.expiration", "30mn");
    private static final int LATCH_MAX_ATTEMPTS_BEFORE_GET_STATUS = Integer.parseInt(Play.configuration.getProperty("latch.maxAttemptsBeforeGetStatus", "3"));
    private static final String SESSION_INACTIVE_EXPIRATION = Play.configuration.getProperty("application.session.expiration", "7d");
    private static final String SESSION_MAX_AGE = Play.configuration.getProperty("application.session.maxAge", "1h");

    static boolean authenticate(String username, String password) {
        checkAuthenticity();
        User user = DarwinFactory.getInstance().loadUser(username);
        boolean authenticationOk = user != null && user.authenticate(password);
        AuthResult authResult = new AuthResult();
        if (authenticationOk) {
            authResult = (AuthResult)DarwinHooks.AppHooks.invoke("authenticate", username, password, user);
            authenticationOk = authResult.isAuthAllowed();
        }

        if (authResult.checkLatch()) {
            authenticationOk = authenticationOk && authenticateWithLatch(user);
        }

        if (authenticationOk) {
            boolean renewCookie = isCookieExpired();
            markSessionAsValid(renewCookie);
            generateRedirectURL();
        }        
        return authenticationOk;
    }

    static boolean authenticateWithLatch(User user) {
        if (Config.isLatchActive()) {
            String operationId = Play.configuration.getProperty("latch.operation.login", Play.configuration.getProperty("latch.appid", ""));
            boolean authenticationOk = checkLatch(user, operationId);
            return addSessionOTPIfNeeded(user, authenticationOk, operationId);
        } else {
            return true;
        }
    }

    private static boolean addSessionOTPIfNeeded(User user, boolean authenticationOk, String operationId){
        if (!authenticationOk && user.getLatchOtp() != null && !user.getLatchOtp().isEmpty()) {
            session.put(Security.SESSION_OTP_REQUIRED, user.getEmail());
            session.put(Security.SESSION_OTP_OPERATION_ID, operationId);
            render("Secure/twofactor.html");
            return false;
        }
        return authenticationOk;
    }

    public static void authenticateWithMobileConnect() {
        checkMobileConnectActivated();
        checkAuthenticity();
        MobileConnectSDK mobileConnectSdk = MobileConnectSDK.getMobileConnectAPI();
        session.put(Constants.MobileConnect.SESSION, Constants.MobileConnect.ACTION_LOGIN);
        if (mobileConnectSdk != null) {
            String authorizeUrl = mobileConnectSdk.getAuthorizeUrl();
            storeMobileConnectStateToken(mobileConnectSdk.getState());
            redirect(authorizeUrl);
        }
    }

    public static void authorizeCallback(String code, String state, String error, String error_description) {
        checkMobileConnectActivated();
        String action = session.get(Constants.MobileConnect.SESSION);
        MobileConnectSDK mobileConnectSdk = MobileConnectSDK.getMobileConnectAPI();
        boolean stateValid = false;

        if (mobileConnectSdk != null) {
            stateValid = checkValidMobileConnectStateToken(state);
        }
        boolean hasErrors = !stateValid || (error != null && !error.isEmpty());
        if (hasErrors) {
            if (error_description != null && error_description.contains(Constants.MobileConnect.Error.USER_CANCEL)) {
                flash.error("mobileConnect.error.userCancel");
            } else {
                flash.error("mobileConnect.error.default");
            }
        }

        if (action !=  null && !action.isEmpty()) {
            if (action.equals(Constants.MobileConnect.ACTION_PAIR)) {
                MobileConnect.pairAccountCallback(code, hasErrors);
            } else if (action.equals(Constants.MobileConnect.ACTION_LOGIN)) {
                authenticateWithMobileConnectCallback(code, hasErrors);
            }
        } else {
            redirect("Secure.login");
        }
    }

    protected static void storeMobileConnectStateToken(String state) {
        session.put(SESSION_MC_STATE, state);
    }

    protected static boolean checkValidMobileConnectStateToken(String state) {
        boolean isValid =  session.contains(SESSION_MC_STATE) && !session.get(SESSION_MC_STATE).isEmpty() && session.get(SESSION_MC_STATE).equals(state);
        if (isValid) {
            storeMobileConnectStateToken("");
        }
        return isValid;
    }

    private static void checkMobileConnectActivated() {
        if (!Config.isMobileConnectActive()) {
            notFound();
        }
    }

    private static void authenticateWithMobileConnectCallback(String code, boolean hasErrors) {
        checkMobileConnectActivated();
        if (!hasErrors) {
            MobileConnectSDK mobileConnectSdk = MobileConnectSDK.getMobileConnectAPI();
            if (mobileConnectSdk != null) {
                IdToken userToken = mobileConnectSdk.tokenRequest(code);
                if (userToken != null) {
                    List<User> users = DarwinFactory.getInstance().retrieveUsersByMobileConnectToken(userToken.getSubject());
                    if (users.size() == 1) {
                        authenticateUserWithMobileConnect(users.get(0));
                    } else {
                        renderUserAccountsChoices(users);
                    }
                }
                flash.error("secure.error");
                params.flash();
            }
        }
        redirect("Secure.login");
    }

    private static void renderUserAccountsChoices(List<User> users) {
        if (users != null && users.size() > 0) {
            List<String> accounts = new ArrayList<String>();
            for (User user : users) {
                accounts.add(user.getEmail());
            }
            session.put(SESSION_ACCOUNTS_CHOICES, accounts);
            render("Secure/accountChoices.html", users);
        }
        redirect("Secure.login");
    }

    public static void checkAccountChoices(@Required String username) {
        checkAuthenticity();

        if (!session.contains(SESSION_ACCOUNTS_CHOICES) || username == null || username.isEmpty()
                || !session.get(SESSION_ACCOUNTS_CHOICES).contains(username)) {
            redirect("Secure.login");
            return;
        }

        User user = DarwinFactory.getInstance().loadUser(username);
        authenticateUserWithMobileConnect(user);

        flash.error("secure.error");
        params.flash();
        redirect("Secure.login");
    }

    private static void authenticateUserWithMobileConnect(User user) {
        if (user != null) {
            boolean authenticationOk = !Config.isMobileConnectWithLatchActive() || authenticateWithLatch(user);
            if (authenticationOk) {
                String username = user.getId();
                session.put("username", username);
                boolean renewCookie = isCookieExpired();
                markSessionAsValid(renewCookie);
                onAuthenticated();
                redirect((String)DarwinHooks.AppHooks.invoke("onUrlAfterLogin"));
            }
        }
    }

    static void generateRedirectURL() {
        String url = flash.get("url") == null ? (String)DarwinHooks.AppHooks.invoke("onUrlAfterLogin") : flash.get("url");
        flash.put("url", url);
    }

    public static boolean checkLatch(User abs, String operationId){
        boolean result = true;
        if (abs.getLatchId() != null && !abs.getLatchId().isEmpty()) {
            result = AsyncLatchHandler.checkLatchStatus(LatchSDK.getLatchAPI(), abs, operationId);
        }
        return result;
    }

    public static boolean checkLatch(User abs){
        return checkLatch(abs, null);
    }

    public static void checkOtp(String token) {
        checkAuthenticity();

        if (!session.contains(Security.SESSION_OTP_REQUIRED) ||
                !session.contains(Security.SESSION_OTP_OPERATION_ID) ||
                token == null || token.isEmpty()) {
            redirect("Secure.login");
        }

        String username = session.get(Security.SESSION_OTP_REQUIRED);
        String operationId = session.get(Security.SESSION_OTP_OPERATION_ID);
        User user = DarwinFactory.getInstance().loadUser(username);
        if (user == null) {
            redirect("Secure.login");
        }
        user.increaseLatchOtpAttempts();
        int otpAttempts = user.getLatchOtpAttempts();
        boolean otpAttemptsExceeded = otpAttempts > LATCH_MAX_ATTEMPTS_BEFORE_GET_STATUS;
        validation.isTrue(!otpAttemptsExceeded).message("latch.otp.attempts").key("otpAttemptsExceeded");
        validation.equals(user.getLatchOtp(), token).message("latch.otp.wrong").key("otpWrong");
        if (validation.hasErrors()) {
            if (otpAttemptsExceeded) {
                user.resetLatchOtpAttempts();
                checkLatch(user, operationId);
            }
            user.save();
            // ...in addition, check if the user has latched him account between form submits
            if (user.getLatchOtp() == null || user.getLatchOtp().isEmpty()){
                redirect("Secure.login");
            } else {
                render("Secure/twofactor.html");
            }
        }
        user.setLatchOtp("");
        user.save();
        try {
            session.put("username", username);
            markSessionAsValid();
            session.remove(Security.SESSION_OTP_REQUIRED);
            onAuthenticated();
            redirect((String)DarwinHooks.AppHooks.invoke("onUrlAfterLogin"));
        } catch (Throwable e) {
            redirect("Secure.login");
        }
    }

    static boolean check(String profile) {
        if (!isCookieValid()) {
            session.clear();
            flash.clear();
            response.removeCookie("rememberme");
            return false;
        }

        if (session.contains("username")) {
            User user = WebController.getCurrentUser();
            if (user != null) {
                UserRole role = DarwinFactory.getInstance().loadUserRole(user.getRoleId());
                return  role != null && role.hasPermission(profile);
            }
        }
        return false;
    }

    protected static boolean isCookieValid() {
        if (isCookieExpired() || isAuthTimeExpired()){
            return false;
        }
        markSessionAsValid();
        return true;
    }

    private static void markSessionAsValid() {
        markSessionAsValid(false);
    }

    static void markSessionAsValid(boolean renewCreationDate) {
        Date now = new Date();
        session.put(SESSION_LAST_ACCESS, now.getTime());
        if (session.get(SESSION_CREATED_AT) == null || renewCreationDate) {
            session.put(SESSION_CREATED_AT, now.getTime());
        }
    }

    static boolean isCookieExpired() {
        if (session.get(SESSION_CREATED_AT) != null) {
            Date creation = getCookieDateField(SESSION_CREATED_AT);
            return creation == null || DateUtils.isDateExpired(creation, SESSION_MAX_AGE);
        }
        return true;
    }

    private static boolean isAuthTimeExpired() {

        boolean rv = true;

        if (session.get(SESSION_LAST_ACCESS) != null) {
            Date lastAccess = getCookieDateField(SESSION_LAST_ACCESS);
            rv = (lastAccess == null || DateUtils.isDateExpired(lastAccess, SESSION_INACTIVE_EXPIRATION));
        }

        rv = (Boolean)DarwinHooks.AppHooks.invoke("onIsAuthTimeExpired", rv);

        return rv;

    }

    private static Date getCookieDateField(String field) {
        Date cookieDate = null;
        try {
            long cookieDateTime = Long.parseLong(session.get(field));
            cookieDate = new Date(cookieDateTime);
        } catch (NumberFormatException e) {}
        return cookieDate;
    }

    static void onDisconnect() {
        DarwinHooks.AppHooks.invoke("onDisconnect");

        String validationToken = session.get(VALIDATION_TOKEN_KEY);
        if (validationToken != null) {
            Cache.delete(validationToken);
        }
    }

    static void onAuthenticated() {
        if (session.get("username") != null) {
            String lowerCaseUsername = session.get("username").toLowerCase();
            session.put("username", lowerCaseUsername);
        }

        DarwinHooks.AppHooks.invoke("onAuthenticated");
    }


    static void onDisconnected() {
        
        String url = (String)DarwinHooks.AppHooks.invoke("onDisconnected");
        
        if (url != null) {
            redirect(url);
        } else {
            redirect(request.getBase());
        }
            
    }

    public static void logout() {
        if (Security.isCookieValid()) {
            checkAuthenticity();
            doLogout();
        } else {
            session.clear();
            redirect(request.getBase());
        }
    }

    protected static void doLogout() {
        // Code copied&pasted from Secure.logout
        onDisconnect();
        session.clear();
        response.removeCookie("rememberme");
        onDisconnected();
    }

    public static String cleanInput(String input) {
        return input != null ? input.replaceAll("[^\\x21-\\x7E\\x80-\\xFE]", DEFAULT_INVALID_CHAR).trim() : "";
    }

    public static class AuthResult {

        private boolean authAllowed;
        private boolean checkLatch;

        public AuthResult() {
            authAllowed = true;
            checkLatch = true;
        }

        public AuthResult(boolean authAllowed) {
            this.authAllowed = authAllowed;
            this.checkLatch = true;
        }

        public AuthResult(boolean authAllowed, boolean checkLatch) {
            this.authAllowed = authAllowed;
            this.checkLatch = checkLatch;
        }

        public boolean isAuthAllowed() {
            return authAllowed;
        }

        public boolean checkLatch() {
            return checkLatch;
        }
    }
}
