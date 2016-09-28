package models.validation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import controllers.WebController;
import models.user.User;
import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.OValContext;
import net.sf.oval.exception.OValException;
import play.Logger;
import play.Play;
import play.mvc.Http;
import play.utils.Java;

public class PasswordPolicy extends AbstractAnnotationCheck<Password> {

    private static String CONFIG_KEY = "passwordPolicy";
    private static String CONFIG_SCHEMA = "%s.%s";
    private static Integer INITIAL_KEY = 1;
    private static String VALIDATION_MESSAGES_PREFIX = "validation.passwordPolicy.";
    protected static final boolean USERNAME_CHECK_ACTIVE;

    public static List<Pattern> policies = new ArrayList<Pattern>();
    public static final String message = "validation.passwordPolicy";

    static {
        int key = INITIAL_KEY;
        String policyKey = Play.configuration.getProperty(String.format(CONFIG_SCHEMA, CONFIG_KEY, key));
        while ( policyKey != null) {
            Pattern policyPattern = compilePattern(policyKey);
            if (policyPattern != null) {
                policies.add(policyPattern);
            }
            policyKey = Play.configuration.getProperty(String.format(CONFIG_SCHEMA, CONFIG_KEY, ++key));
        }
        USERNAME_CHECK_ACTIVE = Boolean.parseBoolean(Play.configuration.getProperty(String.format(CONFIG_SCHEMA, CONFIG_KEY, "username"), "false"));
    }

    protected static Pattern compilePattern(String policyRegex) {
        try {
            return Pattern.compile(policyRegex);
        } catch (PatternSyntaxException e) {
            Logger.error("Password Policy invalid regex ( " + policyRegex + " ): " + e.getMessage());
            return null;
        }
    }

    private Integer regexRuleKeyFail;
    private String usernameField;
    private boolean checkUsername;
    private boolean checkUsernameFail;
    private boolean extendedCheckFail;

    @Override
    public void configure(Password password) {
        super.configure(password);
        this.checkUsername = password.checkUsername() && USERNAME_CHECK_ACTIVE;
        this.usernameField = password.usernameField();
    }

    protected boolean checkRegexRules(String password) {
        boolean checkOk = true;
        Iterator<Pattern> it = policies.iterator();
        int i  = 1;
        while (it.hasNext() && checkOk) {
            checkOk = it.next().matcher(password).matches();
            regexRuleKeyFail = (!checkOk) ? i : null;
            i++;
        }
        return checkOk;
    }

    protected boolean checkSpecialRules(String password) {
        boolean checkOk = true;
        if (checkUsername) {
            String username = getUsername();
            checkOk = (username == null) || !password.toLowerCase().contains(username.toLowerCase());
            checkUsernameFail = !checkOk;
        }
        try {
            boolean extendedCheckOk = (Boolean) PasswordPolicy.PasswordPolicyExtended.invoke("onCheckSpecialRule", password);
            extendedCheckFail = !extendedCheckOk;
            checkOk = checkOk && extendedCheckOk;
        } catch (Throwable throwable) {
            Logger.error("PasswordPolicy check special rule fails: " + throwable.getMessage());
        }
        return checkOk;
    }

    protected String getUsername() {
        Http.Request request = Http.Request.current();
        if (request != null && request.params.get(usernameField) != null) {
            return request.params.get(usernameField).toLowerCase();
        } else {
            User user = WebController.getCurrentUser();
            if (user != null) {
                return user.getId();
            }
        }
        return null;
    }

    /**
     * Add the URI schemes to the message variables so they can be included
     * in the error message.
     */
    @Override
    public Map<String, String> createMessageVariables() {
        final Map<String, String> variables = new TreeMap<String, String>();
        if (regexRuleKeyFail != null) {
            variables.put("2", VALIDATION_MESSAGES_PREFIX + regexRuleKeyFail);
        } else if (checkUsername && checkUsernameFail) {
            variables.put("2", VALIDATION_MESSAGES_PREFIX + "username");
        } else if (extendedCheckFail) {
            String extendedCheckMessage = "";
            try {
                extendedCheckMessage = (String) PasswordPolicyExtended.invoke("getSpecialRuleMessageKey");
            } catch (Throwable throwable) {
                Logger.error("PasswordPolicy special message fails: " + throwable.getMessage());
            }
            variables.put("2", extendedCheckMessage);
        }
        return variables;
    }

    @Override
    public boolean isSatisfied(Object validatedObject, Object value, OValContext context, Validator validator) throws OValException {
        requireMessageVariablesRecreation();
        return checkRegexRules(value.toString()) && checkSpecialRules(value.toString());
    }

    public static class PasswordPolicyExtended {

        public static String getSpecialRuleMessageKey() {
            return VALIDATION_MESSAGES_PREFIX + "custom";
        }

        public static boolean onCheckSpecialRule(String password) {
            return true;
        }

        private static Object invoke(String m, Object... args) throws Throwable {
            try {
                return Java.invokeChildOrStatic(PasswordPolicyExtended.class, m, args);
            } catch(InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
}
