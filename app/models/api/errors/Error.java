package models.api.errors;

import com.google.gson.JsonObject;

public interface Error {

    public int getCode();
    public String getMessage();
    public JsonObject toJson();
    public String toString();
}
