package controllers;

import com.elevenpaths.api.token.IdToken;
import models.Config;
import models.Constants;
import models.mobileConnect.MobileConnectSDK;
import models.user.User;
import play.mvc.Before;

public class MobileConnect extends WebSecurityController {

    @Before
    public static void checkActive() {
        if (!Config.isMobileConnectActive()) {
            notFound();
        }
    }

    public static void checkMobileConnect() {
        User user = getCurrentUser();
        if (user.getMobileConnectId() == null || user.getMobileConnectId().isEmpty()) {
            pair();
        } else {
            unpair();
        }
    }

    private static void pair() {
        render("MobileConnect/pair.html");
    }

    private static void unpair() {
        render("MobileConnect/unpair.html");
    }

    public static void pairAccount() {
        checkAuthenticity();
        MobileConnectSDK mobileConnectSdk = MobileConnectSDK.getMobileConnectAPI();
        session.put(Constants.MobileConnect.SESSION, Constants.MobileConnect.ACTION_PAIR);
        if (mobileConnectSdk != null) {
            String authorizeUrl = mobileConnectSdk.getAuthorizeUrl();
            Security.storeMobileConnectStateToken(mobileConnectSdk.getState());
            redirect(authorizeUrl);
        }
    }

    protected static void pairAccountCallback(String code, boolean hasErrors) {
        if (!hasErrors) {
            MobileConnectSDK mobileConnectSdk = MobileConnectSDK.getMobileConnectAPI();
            if (mobileConnectSdk != null) {
                IdToken userToken = mobileConnectSdk.tokenRequest(code);
                if (userToken != null) {
                    User user = getCurrentUser();
                    if (user != null) {
                        user.setMobileConnectId(userToken.getSubject());
                        user.save();
                        flash.put("userPaired", true);
                        flash.keep();
                        unpair();
                    }
                }
            }
        }
        pair();
    }

    public static void unpairAccount() {
        checkAuthenticity();
        User user = getCurrentUser();
        if (user != null) {
            user.setMobileConnectId("");
            user.save();
            flash.put("userUnpaired", true);
            flash.keep();
        }
        pair();
    }
}
