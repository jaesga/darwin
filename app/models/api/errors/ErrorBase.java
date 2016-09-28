package models.api.errors;

import com.google.gson.JsonObject;

import java.util.Map;
import java.util.TreeMap;

public enum ErrorBase implements Error {

    /*********************** E1xx codes refer to apps authentication problems ***********************/
    /**
     * Invalid Authorization header format
     */
    E101(101, "Invalid Authorization header format"),
    /**
     * Invalid application signature
     */
    E102(102, "Invalid application signature"),
    /**
     * Authorization header missing
     */
    E103(103, "Authorization header missing"),
    /**
     * Date header missing
     */
    E104(104, "Date header missing"),
    /**
     * Invalid date format
     */
    E105(105, "Invalid date format"),
    /**
     * Request expired, date is too old
     */
    E106(106, "Request expired, date is too old"),
    /**
     * File hash header missing
     */
    E110(110, "File hash header missing"),
    /**
     * Body hash header missing
     */
    E111(111, "Body hash header missing"),
    /**
     * Action not authorised
     */
    E112(112, "Action not authorised"),
    /*********************** E2xx codes refer to problems with user accounts ***********************/
    /**
     * User not found
     */
    E201(201, "User not found"),
    /**
     * Invalid token
     */
    E202(202, "Invalid token"),
    /*********************** E3xx codes refer to APIClient problems ***********************/
    /**
     * APIClient not found
     */
    E301(301, "APIClient not found"),
    /**
     * APIClient already exists
     */
    E302(302, "APIClient already exists"),

    /*********************** E4xx codes refer to generic API problems ***********************/
    /**
     * Missing parameter in API call
     */
    E401(401, "Missing parameter in API call"),
    /**
     * Invalid parameter value
     */
    E402(402, "Invalid parameter value"),
    /**
     * Invalid content-type value
     */
    E403(403, "Invalid content-type value"),
    /**
     * Invalid http verb value
     */
    E404(404, "Invalid http verb value"),
    /**
     * Invalid http verb
     */
    E405(405, "Invalid http verb");

    private static Map<Integer, Error> map =  new TreeMap<Integer, Error>();

    static {
        addErrorEnumValues(ErrorBase.values());
    }

    public static void addErrorEnumValues(Error[] errorEnum) {
        for (Error error : errorEnum) {
            map.put(error.getCode(), error);
        }
    }

    public static Error getError(Integer code) {
        return map.containsKey(code) ? map.get(code) : null;
    }

    private final int code;
    private String message;

    private ErrorBase(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * @return a JsonObject with the code and message of the error
     */
    @Override
    public JsonObject toJson() {
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);
        return error;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }
}
