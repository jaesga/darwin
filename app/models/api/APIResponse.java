package models.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.api.errors.Error;
import models.api.errors.ErrorBase;

/**
 * This class models a response from any of the endpoints in the API.
 * It consists of a "data" and an "error" elements. Although normally only one of them will be
 * present, they are not mutually exclusive, since errors can be non fatal, and therefore a response
 * could have valid information in the data field and at the same time inform of an error.
 */
public class APIResponse {

	private JsonObject data = null;
	private Error error = null;

	public APIResponse() {};

	/**
	 *
	 * @param json a json string received from one of the methods of the API
	 */
	public APIResponse(String json) {
		this(new JsonParser().parse(json));
	}

	/**
	 * @param json a JsonElement created from the response of one of the methods of the API
	 * @throws NullPointerException when the json element is null, preventing the instantiation of the object
	 */
	public APIResponse(JsonElement json) {
		if (json.isJsonObject()) {
			if (json.getAsJsonObject().has("data")) {
				this.data = json.getAsJsonObject().getAsJsonObject("data");
			}
			if (json.getAsJsonObject().has("error")) {
				this.error = ErrorBase.getError(json.getAsJsonObject().getAsJsonObject("error").get("code").getAsInt());
			}
		}
	}

	/**
	 *
	 * @return the data part of the API response
	 */
	public JsonObject getData() {
		return data;
	}

	/**
	 *
	 * @param data the data to include in the API response
	 */
	public void setData(JsonObject data) {
		this.data = data;
	}

	/**
	 *
	 * @return the error part of the API response, consisting of an error code and an error message
	 */
	public Error getError() {
		return error;
	}

	/**
	 *
	 * @param error an error to include in the API response
	 */
	public void setError(Error error) {
		this.error = error;
	}

	/**
	 *
	 * @return a JsonObject with the data and error parts set if they exist
	 */
	public JsonObject toJSON() {
		JsonObject edition = new JsonObject();
		if(data != null) {
			edition.add("data", data);
		}
		if(error != null) {
			edition.add("error", getError().toJson());
		}
		return edition;
	}

}
