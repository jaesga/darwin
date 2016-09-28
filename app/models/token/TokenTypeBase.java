package models.token;


import models.Constants;

public enum TokenTypeBase implements TokenType {

    RESET_PASSWORD(Constants.Token.RESET_PASSWORD_COLLECTION_NAME, 64 , 24 * 60 * 60 * 1000, true),
    ACTIVATE_ACCOUNT(Constants.Token.ACTIVATE_ACCOUNT_COLLECTION_NAME, 64 , 24 * 60 * 60 * 1000, true),
    INVITE_USER(Constants.Token.INVITE_USER_COLLECTION_NAME, 64 , 24 * 60 * 60 * 1000, false);

    private String type;
    private int length;
    private long expirationMillis;
    private boolean checkExpirationOnLoad;

    private TokenTypeBase(String type, int length, long expirationMillis, boolean checkExpirationOnLoad) {
        this.type = type;
        this.length = length;
        this.expirationMillis = expirationMillis;
        this.checkExpirationOnLoad = checkExpirationOnLoad;
    }

    public String getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public long getExpirationMillis() {
        return expirationMillis;
    }

    public boolean checkExpirationOnLoad() {
        return checkExpirationOnLoad;
    }
}
