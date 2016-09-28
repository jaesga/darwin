package models.latch.response;

import com.elevenpaths.latch.LatchResponse;
import com.google.gson.JsonObject;
import models.latch.LatchSDK;

public class LatchHistory extends LatchResponse {

    private LatchOperation latchOperations;

    public LatchHistory(LatchResponse latchResponse) {
        super(latchResponse.toJSON());
        JsonObject data = latchResponse.getData();
        if (data != null) {
            this.latchOperations = new LatchOperation(latchResponse.getData().getAsJsonObject(LatchSDK.APP_ID), LatchSDK.APP_ID);
        }
    }

    public LatchOperation getLatchOperations() {
        return latchOperations;
    }
}
