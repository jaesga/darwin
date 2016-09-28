package models.latch.response;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LatchOperation {

    private String id;
    private String name;
    private String status;
    private List<LatchOperation> operations;

    public LatchOperation(JsonObject json, String id) {
        this.id = id;
        if (json.has("name")) {
            this.name = json.get("name").getAsString();
        }
        if (json.has("status")) {
            this.status = json.get("status").getAsString();
        }
        this.operations = new ArrayList<LatchOperation>();
        if (json.has("operations")) {
            for (Map.Entry<String, JsonElement> operation : json.get("operations").getAsJsonObject().entrySet()) {
                this.operations.add(new LatchOperation(operation.getValue().getAsJsonObject(), operation.getKey()));
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public List<LatchOperation> getOperations() {
        return operations;
    }
}
