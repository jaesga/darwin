package models.latch;

import com.elevenpaths.latch.LatchResponse;
import com.google.gson.JsonObject;
import models.user.User;
import play.Logger;
import play.Play;

import java.util.concurrent.*;

public class AsyncLatchHandler implements Callable<Boolean> {

    private final static int LATCH_SECONDS_TIMEOUT = Integer.parseInt(Play.configuration.getProperty("latch.seconds.timeout"));

    private LatchSDK api;
    private User user;
    private String operationId;

    private AsyncLatchHandler(LatchSDK api, User user, String operationId) {
        this.api = api;
        this.user = user;
        this.operationId = operationId;
    }

    @Override
    public Boolean call() throws Exception {
        LatchResponse response;

        if (operationId == null){
            response = this.api.status(this.user.getLatchId());
            operationId = Play.configuration.getProperty("latch.appid");
        }else{
            response = this.api.status(this.user.getLatchId(), operationId);
        }


        if (response.getData() != null && response.getData().has("operations")) {

            JsonObject json = response.getData().getAsJsonObject("operations")
                    .getAsJsonObject(operationId);

            if (json == null) {
                return true; //Something is wrong with latch appId or connectivity
            }

            if (json.has("status") && "off".equals(json.get("status").getAsString())) {
                this.user.setLatchOtp("");
                this.user.save();
                return false;
            } else {
                if (json.has("two_factor")) {
                    String otpToken = json.get("two_factor").getAsJsonObject().get("token").getAsString();
                    this.user.setLatchOtp(otpToken);
                    this.user.save();
                    return false;
                } else {
                    return true;
                }
            }
        }

        return true;
    }

    public static boolean checkLatchStatus(LatchSDK api, User user){
        return checkLatchStatus(api, user, null);
    }

    public static boolean checkLatchStatus(LatchSDK api, User user, String operationId){
        boolean result = true;

        try {
            if (api != null && user != null) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<Boolean> future = executor.submit(new AsyncLatchHandler(api, user, operationId));
                result = future.get(LATCH_SECONDS_TIMEOUT, TimeUnit.SECONDS);
                executor.shutdownNow();
            }

        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return result;
    }
}
