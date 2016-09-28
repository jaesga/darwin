package models.api;

import models.utils.AuthUtils;
import play.Play;

public abstract class APIClient {

    protected static final int CLIENT_ID_LENGTH = Play.configuration.getProperty("APIClient.clientIdLength") != null ? Integer.parseInt(Play.configuration.getProperty("APIClient.clientIdLength")) : 20 ;
    protected static final int CLIENT_SECRET_LENGTH = Play.configuration.getProperty("APIClient.clientSecretLength") != null ? Integer.parseInt(Play.configuration.getProperty("APIClient.clientSecretLength")) : 40 ;

    protected String clientId;
    protected String name;
    protected String secret;
    protected String email;

    protected APIClient() {}

    protected APIClient(String name, String email) {
        this.clientId = AuthUtils.generateToken(CLIENT_ID_LENGTH);
        this.secret = AuthUtils.generateToken(CLIENT_SECRET_LENGTH);
        this.name = name;
        this.email = email;
    }

    public abstract void remove();
    public abstract void save();
    public abstract boolean isExistingAPIClient();
    public abstract void renewSecret();

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
