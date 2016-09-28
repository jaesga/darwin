package models.latch;

import com.elevenpaths.latch.LatchUser;
import com.google.gson.JsonElement;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;

import java.util.Map;

public class LatchUserSDK extends LatchUser {

    public static LatchUserSDK getLatchAPI() {
        String appId = Play.configuration.getProperty("latch.user.appid");
        String secret = Play.configuration.getProperty("latch.user.secret");
        if (!StringUtils.isEmpty(appId) && !StringUtils.isEmpty(secret)) {
            return new LatchUserSDK(appId, secret);
        } else {
            return null;
        }
    }

    static {
        setHost(Play.configuration.getProperty("latch.host"));
    }

    public LatchUserSDK(String appId, String secretKey) {
        super(appId, secretKey);
    }

    @Override
    public JsonElement HTTP_GET(String URL, Map<String, String>headers) {
        WS.WSRequest request = WS.url(URL);
        request.headers.putAll(headers);

        try {
            HttpResponse response = request.get();
            if (response != null && response.success() && response.getJson().isJsonObject()) {
                return response.getJson();
            }
        } catch (Exception e) {
            Logger.error("Host is not reacheable: " + URL + ". " + e.getMessage());
        }
        return null;
    }

}
