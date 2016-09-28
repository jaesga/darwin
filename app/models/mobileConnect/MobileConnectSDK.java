package models.mobileConnect;

import com.elevenpaths.api.MobileConnect;
import com.elevenpaths.api.authorization.AuthorizationRequestUrl;
import com.elevenpaths.api.authorization.AuthorizationRequestUrlBuilder;
import com.elevenpaths.api.token.IdToken;
import com.elevenpaths.api.token.TokenRequest;
import com.elevenpaths.api.token.TokenRequestBuilder;
import models.Constants;
import models.exception.UndefinedMobileConnectCredentialsException;
import models.utils.AuthUtils;

public class MobileConnectSDK extends MobileConnect {

    public static MobileConnectSDK getMobileConnectAPI() {
        if (Constants.MobileConnect.CONSUMER_ID.isEmpty()  || Constants.MobileConnect.CONSUMER_SECRET.isEmpty()) {
            throw new UndefinedMobileConnectCredentialsException("You must define the consumer id and the consumer secret at application.conf");
        }
        return new MobileConnectSDK(Constants.MobileConnect.CONSUMER_ID, Constants.MobileConnect.CONSUMER_SECRET);
    }

    private MobileConnectSDK(String consumerId, String consumerSecret) {
        super(consumerId, consumerSecret);
    }

    public String getAuthorizeUrl() {
        String state = AuthUtils.generateToken(12);
        String nonce = AuthUtils.generateToken(12);
        setState(state);
        setNonce(nonce);
        AuthorizationRequestUrl authorizationRequestUrl = new AuthorizationRequestUrlBuilder()
                .setClientId(Constants.MobileConnect.CONSUMER_ID)
                .setResponseType(Constants.MobileConnect.Authorize.RESPONSE_TYPE)
                .setRedirectUri(Constants.MobileConnect.REDIRECT_URI)
                .setScope(Constants.MobileConnect.Authorize.SCOPE)
                .setAcrValues(Constants.MobileConnect.Authorize.ACR_VALUES)
                .setState(state)
                .setNonce(nonce)
                .create();
        return super.getAuthorizeUrl(authorizationRequestUrl);
    }

    public IdToken tokenRequest(String code) {
        TokenRequest tokenRequest = new TokenRequestBuilder()
                .setClientId(Constants.MobileConnect.CONSUMER_ID)
                .setCode(code)
                .setGrantType(Constants.MobileConnect.Token.GRANT_TYPE)
                .setRedirectUri(Constants.MobileConnect.REDIRECT_URI)
                .createTokenRequest();
        return super.tokenRequest(tokenRequest);
    }
}
