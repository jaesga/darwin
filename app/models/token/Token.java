package models.token;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Date;

public abstract class Token {

    protected String email;
    protected String token;
    protected Date generated;
    protected TokenType type;
    protected JsonObject data;

    protected Token() {}

    protected Token(String email, String token, Date generated, TokenType type) {
        this(email, token, generated, type, null);
    }

    protected Token(String email, String token, Date generated, TokenType type, JsonObject data) {
        this.email = email;
        this.token = token;
        this.generated = generated;
        this.type = type;
        this.data = data;
    }

    public abstract void save();
    public abstract void remove();
    public abstract void renew();
    public abstract boolean isValid();

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getGenerated() {
        return generated;
    }

    public void setGenerated(Date generated) {
        this.generated = generated;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }

    public JsonElement getDataValue(String key) {
        if (data != null && data.has(key)) {
            return data.get(key);
        }
        return null;
    }

}
