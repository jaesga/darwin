package controllers;

import models.Config;
import models.user.User;
import play.mvc.Before;
import play.mvc.With;

@With(Secure.class)
public class WebSecurityController extends WebController {

    @Before(unless = {"PublicContentBase.requestPasswordReset"})
    private static void checkPasswordExpiration() {
        if (Config.isResetPasswordPolicyExpirationActive() && Security.isConnected() && isSessionPasswordExpired()) {
            redirect("PublicContentBase.passwordReset");
        }
    }

    protected static boolean isSessionPasswordExpired() {
        User user = getCurrentUser();
        return user != null && user.checkPasswordExpiration();
    }

    @Before(unless = {"Latch.checkLatch", "Latch.pairAccount", "Latch.unpairAccount", "checkPasswordExpiration"})
    private static void checkPairLatchAccountRedirect() {
        if (Config.isLatchModeMandatory() && !isSessionPasswordExpired() && isSessionLatchMandatoryRedirection()) {
            redirect("Latch.checkLatch");
        }
    }

    private static boolean isSessionLatchMandatoryRedirection() {
        User user = getCurrentUser();
        return user != null && (user.getLatchId() == null || user.getLatchId().isEmpty());
    }
}
