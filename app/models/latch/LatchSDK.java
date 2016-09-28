package models.latch;

import com.elevenpaths.latch.LatchApp;
import com.elevenpaths.latch.LatchResponse;
import com.google.gson.JsonElement;
import models.latch.response.LatchHistory;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;

import java.util.Map;

public class LatchSDK extends LatchApp {

    public static String APP_ID = Play.configuration.getProperty("latch.appid", "");
    protected static String APP_SECRET = Play.configuration.getProperty("latch.secret", "");

    static {
        setHost(Play.configuration.getProperty("latch.host"));
    }

    public static LatchSDK getLatchAPI() {
        if (!StringUtils.isEmpty(APP_ID) && !StringUtils.isEmpty(APP_SECRET)) {
            return new LatchSDK(APP_ID, APP_SECRET);
        }
        return null;
    }

    public LatchSDK(String appId, String secretKey) {
        super(appId, secretKey);
    }

    @Override
    public LatchResponse history(String accountId) {
        LatchResponse history =  super.history(accountId);
        return new LatchHistory(history);
    }

    @Override
    public JsonElement HTTP_GET(String URL, Map<String, String>headers) {
        WS.WSRequest request = WS.url(URL);
        request.headers.putAll(headers);

        try {
            HttpResponse response = request.get();
            if (response != null && response.success()) {
                return response.getJson();
            }
        } catch (Exception e) {
            Logger.error("Host is not reacheable: " + URL + ". " + e.getMessage());
        }
        return null;
    }

}
