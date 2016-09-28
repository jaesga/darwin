package controllers;

import com.elevenpaths.latch.LatchResponse;
import models.Config;
import models.latch.LatchSDK;
import models.user.User;
import play.Play;
import play.data.validation.Required;
import play.data.validation.Validation.ValidationResult;
import play.mvc.Before;


public class Latch extends WebSecurityController {

    @Before
    private static void checkActive() {
        if (!Config.isLatchActive()) {
            notFound();
        }
    }

    public static void checkLatch() {
        User user = getCurrentUser();
        if (user.getLatchId() == null || user.getLatchId().isEmpty()) {
            pair(user);
        } else {
            unpair(user);
        }
    }

    private static void pair(User user) {
        render("Latch/pair.html",user);
    }

    private static void unpair(User user) {
        render("Latch/unpair.html",user);
    }


    public static void pairAccount(@Required(message= "latch.validation.token")String token) {
        checkAuthenticity();
        LatchSDK api = LatchSDK.getLatchAPI();

        if (token == null || token.isEmpty()) {
            new ValidationResult().message("latch.validation.token");
            render("Latch/pair.html");
        } else if (api == null) {
            new ValidationResult().message("latch.validation.nocfg");
            render("Latch/pair.html");
        } else {
            token = token.trim();
            User user = getCurrentUser();
            boolean isPairingAllowed = (Boolean)DarwinHooks.AppHooks.invoke("onLatchPair", token, user);
            if (isPairingAllowed) {
                LatchResponse response = api.pair(token);
                String accountId = "";
                if (response != null && response.getData() != null && response.getData().get("accountId") != null) {
                    accountId = response.getData().get("accountId").getAsString();
                } else {
                    flash.put("badToken", true);
                    render("Latch/pair.html");
                    return;
                }

                user.setLatchId(accountId);
                DarwinHooks.AppHooks.invoke("onLatchPaired", user);
                user.save();
                flash.put("userPaired", true);
            }
            redirect("Latch.checkLatch");
        }
    }

    public static void unpairAccount() {
        checkAuthenticity();


        LatchSDK api = LatchSDK.getLatchAPI();
        if (api != null) {
            User user = getCurrentUser();
            boolean isUnpairingAllowed = (Boolean)DarwinHooks.AppHooks.invoke("onLatchUnpair",user);
            if (isUnpairingAllowed) {
                if (Play.configuration.getProperty("latch.operation.unpair") != null) {
                    if (!Security.checkLatch(user, Play.configuration.getProperty("latch.operation.unpair"))) {
                        flash.put("unpairingLocked", true);
                        redirect("Latch.checkLatch");
                    }
                }

                String accountID = user.getLatchId();
                user.setLatchId("");
                DarwinHooks.AppHooks.invoke("onLatchUnpaired", user);
                user.save();
                api.unpair(accountID);
                flash.put("userUnpaired", true);
            }
            redirect("Latch.checkLatch");
        } else {
            new ValidationResult().message("latch.validation.nocfg");
            render("Latch/unpair.html");
        }
    }

    public static void disableAlert(@Required Boolean disableAlert) {
        checkAuthenticity();
        if (!validation.hasErrors()) {
            User user = getCurrentUser();
            if (user != null) {
                user.setEnabledLatchAlertMessage(!disableAlert);
                user.save();
            }
        }
        redirect(request.headers.get("referer").value());
    }

}
