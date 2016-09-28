package controllers;

import models.factory.DarwinFactory;
import models.latch.LatchSDK;
import models.latch.response.LatchHistory;
import models.latch.response.LatchOperation;
import models.user.User;
import play.data.validation.Email;
import play.data.validation.Required;

public class Profile extends WebSecurityController {

    public static void index() {
        index(getCurrentUser());
    }

    protected static void index(User user) {
        if (user != null) {
            LatchSDK latchSDK = LatchSDK.getLatchAPI();
            LatchOperation latchOperation = null;
            if (latchSDK != null) {
                LatchHistory history = (LatchHistory) latchSDK.history(user.getLatchId());
                latchOperation = history.getLatchOperations();
            }
            render("Profile/index.html", user, latchOperation);
        }
        notFound();
    }

    @Check("USER_READ_PROFILES")
    public static void showUserProfile(@Required String email) {
        if (validation.hasErrors()) {
            notFound();
        }
        index(DarwinFactory.getInstance().loadUser(email));
    }

    @Check("ADMIN")
    public static void changeLatch(@Required String email, @Required String operationId, @Required boolean status) {
        checkAuthenticity();
        User user = DarwinFactory.getInstance().loadUser(email);
        if (user != null && operationId != null && !operationId.isEmpty()) {
            if (status) {
                user.lockLatch(operationId);
            } else {
                user.unlockLatch(operationId);
            }
            index(user);
        }
        index();
    }

    @Check("USER_SELF_DELETE")
    public static void deleteAccount(@Required String email) {
        checkAuthenticity();
        if (!validation.hasErrors()) {
            User user = DarwinFactory.getInstance().loadUser(email);
            if (user != null && session.get("username").equals(email)) {
                Users.removeUserAPIClients(user);
                user.remove();
                if (user.isLatched()){
                    LatchSDK api = LatchSDK.getLatchAPI();
                    api.unpair(user.getLatchId());
                }
                Security.doLogout();
            }
        }
        index();
    }
}
