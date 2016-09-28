package models.token;


public interface TokenType {

    public String getType();
    public int getLength();
    public long getExpirationMillis();
    public boolean checkExpirationOnLoad();

}
