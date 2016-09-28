package models.ticket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import models.exception.UndefinedZendeskCredentialsException;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.libs.WS;

import static models.Constants.ZendeskApi.*;

public class ZendeskApi {

    private static ZendeskApi instance = null;

    public static ZendeskApi getInstance() {
        if (instance == null) {
            synchronized (ZendeskApi.class) {
                if (instance == null) {
                    if (StringUtils.isEmpty(SERVICE_URL) || StringUtils.isEmpty(SERVICE_USER) || StringUtils.isEmpty(SERVICE_KEY)) {
                        Logger.error("Undefined Zendesk credentials.");
                        throw new UndefinedZendeskCredentialsException("Undefined Zendesk credentials.");
                    }
                    instance = new ZendeskApi(SERVICE_URL, SERVICE_USER, SERVICE_KEY);
                }
            }
        }
        return instance;
    }

    public static boolean sendTicket(Ticket ticket) {
        return getInstance().send(ticket);
    }

    protected String url;
    protected String user;
    protected String key;

    private ZendeskApi(String url, String user, String key) {
        this.url = url;
        this.user = user;
        this.key = key;
    }

    public boolean send(Ticket ticket) {
        WS.WSRequest request = prepareRequest();
        request.body(buildTicket(ticket));
        WS.HttpResponse response = request.post();
        boolean success = response.success();
        if (!success) {
            Logger.error("Error sending ticket: " + response.getString()  + " (" + response.getStatus() + ")");
        }
        return success;
    }

    private WS.WSRequest prepareRequest() {
        WS.WSRequest request = WS.url(url);
        request.authenticate(user, key);
        request.setHeader("Content-type", "application/json");
        return request;
    }

    private JsonObject buildTicket(Ticket ticket) {
        Gson gson = new Gson();
        JsonObject rv = new JsonObject();

        JsonObject requester = new JsonObject();
        requester.addProperty(FIELD_NAME, ticket.getUsername());
        requester.addProperty(FIELD_EMAIL, ticket.getEmail());

        JsonObject ticketJson = new JsonObject();
        ticketJson.add(FIELD_REQUESTER, requester);
        ticketJson.addProperty(FIELD_SUBJECT, ticket.getSubject());
        ticketJson.addProperty(FIELD_COMMENT, ticket.getMessage());
        ticketJson.addProperty(FIELD_TAGS, gson.toJson(ticket.getTags()));

        rv.add(FIELD_TICKET, ticketJson);
        return rv;
    }
}
