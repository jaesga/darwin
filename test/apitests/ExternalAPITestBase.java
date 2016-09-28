package apitests;

import com.google.gson.JsonObject;
import controllers.APIController;
import models.api.APIResponse;
import models.api.DarwinAuth;
import models.api.errors.ErrorBase;
import org.junit.Test;
import play.Logger;
import play.Play;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public abstract class ExternalAPITestBase extends ApiTestBase {

    private static final String USER_AGENT_HEADER_NAME = "User-Agent";
    public static final String X_FORWARDED_FOR_HEADER_NAME = "x-forwarded-for";
    public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private static String USER_AGENT = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7 ExternalTests";

    private static String X_FORWARDED_FOR = null; // use to simulat x-forwarded-for

    private String DEBUG_PARAMS = APIController.HTTP_DEBUG_TIME;

    private void assertBasics(Response response) throws UnsupportedEncodingException {
        //Logger.info(response.out.toString("utf-8"));
        assertIsOk(response);
        assertContentType("application/json", response);
        assertCharset("utf-8", response);
    }

    /**
     * @param response    The response from the API
     * @param debugParams The debug parameters that where requested, if any
     * @param debug       true if debug information is expected in the response
     */
    private void assertDebug(Response response, String debugParams, boolean debug) {
        if (debug) {
            assertTrue(response.headers.containsKey(APIController.HTTP_DEBUG));
        } else {
            assertFalse(response.headers.containsKey(APIController.HTTP_DEBUG));
        }
    }

    protected APIResponse api_GET(String url) {
        return api_GET(url, null);
    }

    protected APIResponse api_GET(String url, Map<String, String[]> queryParams) {
        try {
            url += apiAuth.parseQueryParams(queryParams);
            return api_HTTP(url, "GET", apiAuth.authenticationHeaders("GET", url, null, null), "", "", false);
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
            return new APIResponse(new JsonObject());
        }
    }

    protected APIResponse api_POST(String url) {
        return api_POST(url, new HashMap<String, String[]>());
    }

    protected APIResponse api_POST(String url, Map<String, String[]> data) {
        try {
            String body = apiAuth.getSerializedParams(data);
            return api_HTTP(url, "POST", apiAuth.authenticationHeaders("POST", url, null, data), body, "application/x-www-form-urlencoded", false);
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
            return new APIResponse(new JsonObject());
        }
    }

    protected APIResponse api_POST(String url, JsonObject body) {
        try {
            return api_HTTP(url, "POST", apiAuth.authenticationHeadersWithBody("POST", url, null, body.toString()), body.toString(), "application/json", false);
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
            return new APIResponse(new JsonObject());
        }
    }


    protected APIResponse api_PUT(String url) {
        return api_POST(url, new HashMap<String, String[]>());
    }

    protected APIResponse api_PUT(String url, Map<String, String[]> data) {
        try {
            String body = apiAuth.getSerializedParams(data);
            return api_HTTP(url, "POST", apiAuth.authenticationHeaders("POST", url, null, data), body, "application/x-www-form-urlencoded", false);
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
            return new APIResponse(new JsonObject());
        }
    }

    protected APIResponse api_PUT(String url, JsonObject body) {
        try {
            return api_HTTP(url, "PUT", apiAuth.authenticationHeadersWithBody("PUT", url, null, body.toString()), body.toString(), "application/json", false);
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
            return new APIResponse(new JsonObject());
        }
    }

    protected APIResponse api_DELETE(String url) {
        return api_DELETE(url, null);
    }

    protected APIResponse api_DELETE(String url, Map<String, String[]> queryParams) {
        try {
            url += apiAuth.parseQueryParams(queryParams);
            return api_HTTP(url, "DELETE", apiAuth.authenticationHeaders("DELETE", url, null, null), "", "", false);
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
            return new APIResponse(new JsonObject());
        }
    }

    private APIResponse api_HTTP(String URL, String method, Map<String, String> headers, String body, String contentType, boolean debug) {
        try {
            Request request = buildRequestWithHeaders(headers, contentType, debug);
            Response response = null;
            if ("GET".equals(method.toUpperCase())) {
                response = GET(request, URL);
            } else if ("POST".equals(method.toUpperCase())) {
                if (body != null) {
                    response = POST(request, URL, contentType, body);
                } else {
                    response = POST(request, URL);
                }
            } else if ("PUT".equals(method.toUpperCase())) {
                response = PUT(request, URL, contentType, body);
            } else if ("DELETE".equals(method.toUpperCase())) {
                response = DELETE(request, URL);
            } else {
                Logger.error("INVALID HTTP METHOD");
            }
            assertBasics(response);
            assertDebug(response, DEBUG_PARAMS, debug);
            return new APIResponse(response.out.toString("utf-8"));
        } catch (Exception e) {
            Logger.error("Invalid APIResponse:" + e.getMessage());
            return new APIResponse(new JsonObject());
        }
    }

    private Request buildRequestWithHeaders(Map<String, String> headers, String contentType, boolean debug) {
        Request request = newRequest();
        for (Map.Entry<String, String> header : headers.entrySet()) {
            request.headers.put(header.getKey().toLowerCase(), new Http.Header(header.getKey(), header.getValue()));
        }
        request.headers.put(USER_AGENT_HEADER_NAME.toLowerCase(), new Http.Header(USER_AGENT_HEADER_NAME, USER_AGENT));
        if (X_FORWARDED_FOR != null) {
            request.headers.put(X_FORWARDED_FOR_HEADER_NAME, new Http.Header(X_FORWARDED_FOR_HEADER_NAME, X_FORWARDED_FOR));
        }
        if (debug) {
            request.headers.put(APIController.HTTP_DEBUG.toLowerCase(), new Http.Header(APIController.HTTP_DEBUG, DEBUG_PARAMS));
        }
        if (contentType != null && !contentType.isEmpty()) {
            request.headers.put(CONTENT_TYPE_HEADER_NAME.toLowerCase(), new Http.Header(CONTENT_TYPE_HEADER_NAME, contentType));
        }
        return request;
    }

    /**
     * **********    Authentication    ****************
     */
    @Test
    public void testAuthentication() throws UnsupportedEncodingException {
        String url = getAuthenticationURL();

        Request r = newRequest();
        Response response = null;
        APIResponse p2response = null;
        DarwinAuth api = null;

        // Authorization HTTP header missing
        response = GET(r, url);
        assertBasics(response);
        p2response = new APIResponse(response.out.toString("utf-8"));
        assertEquals("Authorization HTTP header missing", ErrorBase.E103, p2response.getError());

        // Date HTTP header missing
        r.headers.put(DarwinAuth.AUTHORIZATION_HEADER_NAME.toLowerCase(),
                new Http.Header(DarwinAuth.AUTHORIZATION_HEADER_NAME,
                        "foo"));
        response = GET(r, url);
        assertBasics(response);
        p2response = new APIResponse(response.out.toString("utf-8"));
        assertEquals("Date HTTP header missing", ErrorBase.E104, p2response.getError());

        // Invalid method 11PATHS in Authorization HTTP Header
        r.headers.put(DarwinAuth.DATE_HEADER_NAME.toLowerCase(),
                new Http.Header(DarwinAuth.DATE_HEADER_NAME,
                        "foo"));
        response = GET(r, url);
        assertBasics(response);
        p2response = new APIResponse(response.out.toString("utf-8"));
        assertEquals("Invalid authorization header format", ErrorBase.E101, p2response.getError());

        // Unknown ApiClient ID
        api = new DarwinAuth("foo", "bar");
        Map<String, String> headers = api.authenticationHeaders("GET", "foo", null, null);
        r.headers.put(DarwinAuth.AUTHORIZATION_HEADER_NAME.toLowerCase(),
                new Http.Header(DarwinAuth.AUTHORIZATION_HEADER_NAME,
                        headers.get(DarwinAuth.AUTHORIZATION_HEADER_NAME)));
        r.headers.put(DarwinAuth.DATE_HEADER_NAME.toLowerCase(),
                new Http.Header(DarwinAuth.DATE_HEADER_NAME,
                        headers.get(DarwinAuth.DATE_HEADER_NAME)));
        response = GET(r, url);
        assertBasics(response);
        p2response = new APIResponse(response.out.toString("utf-8"));
        assertEquals("Unknown APP_ID", ErrorBase.E102, p2response.getError());

        // Wrong signature
        r.headers.put(DarwinAuth.AUTHORIZATION_HEADER_NAME.toLowerCase(),
                new Http.Header(DarwinAuth.AUTHORIZATION_HEADER_NAME,
                        DarwinAuth.AUTHORIZATION_METHOD + " " + apiClientId + " foo"));
        response = GET(r, url);
        assertBasics(response);
        p2response = new APIResponse(response.out.toString("utf-8"));
        assertEquals("Unknown APP_ID", ErrorBase.E102, p2response.getError());

        // TODO: app only allowed to use API with HTTPS client cert
    }

    /**
     * **********    Request expiration    ****************
     */
    @Test
    public void testRequestExpiration() {
        try {
            Request r = newRequest();
            DarwinAuth api = new DarwinAuth(apiClientId, secret);
            String url = getRequestExpirationURL();

            // First all good
            Map<String, String> headers = api.authenticationHeaders("GET", url, null, null);

            r.headers.put(DarwinAuth.AUTHORIZATION_HEADER_NAME.toLowerCase(),
                    new Http.Header(DarwinAuth.AUTHORIZATION_HEADER_NAME,
                            headers.get(DarwinAuth.AUTHORIZATION_HEADER_NAME)));
            r.headers.put(DarwinAuth.DATE_HEADER_NAME.toLowerCase(),
                    new Http.Header(DarwinAuth.DATE_HEADER_NAME,
                            headers.get(DarwinAuth.DATE_HEADER_NAME)));

            Response response = GET(r, url);
            assertBasics(response);
            APIResponse p2r = new APIResponse(response.out.toString("utf-8"));
            assertNull("Unexpected error for request testing expiration ", p2r.getError());

            // Now request older than config "x-11paths-date_expiration_millis"
            final SimpleDateFormat sdf = new SimpleDateFormat(DarwinAuth.UTC_STRING_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            int pastTimestamp = Integer.parseInt(Play.configuration.getProperty("APIController.x-11paths-dateExpirationMillis", "3600000")) + 500;
            headers = api.authenticationHeaders("GET", url, null, null, sdf.format(new Date(System.currentTimeMillis() - pastTimestamp)));

            r.headers.put(DarwinAuth.AUTHORIZATION_HEADER_NAME.toLowerCase(),
                    new Http.Header(DarwinAuth.AUTHORIZATION_HEADER_NAME,
                            headers.get(DarwinAuth.AUTHORIZATION_HEADER_NAME)));
            r.headers.put(DarwinAuth.DATE_HEADER_NAME.toLowerCase(),
                    new Http.Header(DarwinAuth.DATE_HEADER_NAME,
                            headers.get(DarwinAuth.DATE_HEADER_NAME)));

            response = GET(r, url);
            assertBasics(response);
            p2r = new APIResponse(response.out.toString("utf-8"));
            assertEquals("Undetected expired request ", ErrorBase.E106, p2r.getError());

            // Garbage in date header
            headers = api.authenticationHeaders("GET", url, null, null, "foo");

            r.headers.put(DarwinAuth.AUTHORIZATION_HEADER_NAME.toLowerCase(),
                    new Http.Header(DarwinAuth.AUTHORIZATION_HEADER_NAME,
                            headers.get(DarwinAuth.AUTHORIZATION_HEADER_NAME)));
            r.headers.put(DarwinAuth.DATE_HEADER_NAME.toLowerCase(),
                    new Http.Header(DarwinAuth.DATE_HEADER_NAME,
                            headers.get(DarwinAuth.DATE_HEADER_NAME)));

            response = GET(r, url);
            assertBasics(response);
            p2r = new APIResponse(response.out.toString("utf-8"));
            assertEquals("Undetected invalid format for date ", ErrorBase.E105, p2r.getError());
        } catch (UnsupportedEncodingException e) {
            return;
        }
    }

    /**
     *
     * @return An endpoint of the API under test, to test the auth expiration
     */
    protected abstract String getRequestExpirationURL();

    /**
     *
     * @return Any endpoint of the API under test, to test the authentication 
     */
    protected abstract String getAuthenticationURL();
}
