package controllers;

import com.google.gson.JsonObject;
import models.api.APIClient;
import models.api.APIResponse;
import models.api.DarwinAuth;
import models.api.errors.Error;
import models.api.errors.ErrorBase;
import models.factory.DarwinFactory;
import models.roles.UserRole;
import models.user.User;
import org.apache.commons.codec.digest.DigestUtils;
import play.Logger;
import play.Play;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Header;

import java.io.*;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class APIController extends Controller {

    protected static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    protected static final String MULTIPART_FORM_DATA = "multipart/form-data";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String FILE_HASH = "fileHash";
    protected static final long DATE_EXPIRATION_MILLIS = Play.configuration.getProperty("APIController.x-11paths-dateExpirationMillis") != null ? Long.parseLong(Play.configuration.getProperty("APIController.x-11paths-dateExpirationMillis")) : 3600000;

    protected static final String APIResponse = "APIResponse";
    protected static final String API_CLIENT_ID = "clientId";
    protected static final String DEBUG_START_TIME = "";

    public static final String HTTP_DEBUG = "X-Debug";
    public static final String HTTP_DEBUG_TIME = "time";
    protected static final String HTTP_DEBUG_ERROR = "error";

    @Before
    protected static void debug() {
        if (request.headers.containsKey(HTTP_DEBUG.toLowerCase())) {
            String[] debugKeys = request.headers.get(HTTP_DEBUG.toLowerCase()).value().split(";");
            for (int i=0; i<debugKeys.length; i++) {
                if (HTTP_DEBUG_TIME.equals(debugKeys[i])) {
                    renderArgs.put(DEBUG_START_TIME, System.currentTimeMillis());
                } else if (debugKeys[i].startsWith(HTTP_DEBUG_ERROR)) {
                    String[] errorCode = debugKeys[i].split("=");
                    if (errorCode.length>1) {
                        setError(ErrorBase.getError(Integer.parseInt(errorCode[1])));
                        renderAPIResponse();
                    } else {
                        Logger.info("Format error in debug header");
                    }
                }
            }
        }
    }

    @Before
    protected static void authentication() throws IOException {
        TreeMap<String,String> xHeaders = getXHeaders();

        renderArgs.put(APIResponse, new APIResponse());
        if (!request.headers.containsKey(DarwinAuth.AUTHORIZATION_HEADER_NAME.toLowerCase())) {
            setError(ErrorBase.E103);
            renderAPIResponse();
        }

        if (!request.headers.containsKey(DarwinAuth.DATE_HEADER_NAME.toLowerCase())) {
            setError(ErrorBase.E104);
            renderAPIResponse();
        }

        String authHeader = request.headers.get(DarwinAuth.AUTHORIZATION_HEADER_NAME.toLowerCase()).value();
        if (!DarwinAuth.AUTHORIZATION_METHOD.equals(DarwinAuth.getAuthMethodFromHeader(authHeader))) {
            setError(ErrorBase.E101);
            renderAPIResponse();
        }

        String apiClientId = DarwinAuth.getEntityIdFromHeader(authHeader);

        APIClient apiClient = DarwinFactory.getInstance().loadAPIClient(apiClientId);
        if (apiClient == null) {
            //The error is actually application Id not found but this is sent to prevent user enumeration attacks
            renderAPIError(ErrorBase.E102);
        }

        String secret = apiClient.getSecret();
        String signature = DarwinAuth.getSignatureFromHeader(authHeader);
        String xDate = request.headers.get(DarwinAuth.DATE_HEADER_NAME.toLowerCase()).value();

        DarwinAuth darwinAuth = new DarwinAuth(apiClientId, secret);
        Map<String, String> expectedHeaders = null;

        checkInvalidRequestHttpVerb();
        if (isHttpGET() || isHttpDELETE()) {
            expectedHeaders = getExpectedHeaders(darwinAuth, xHeaders, xDate);
        } else if (isHttpPOST() || isHttpPUT()) {
            if (request.contentType == null || request.contentType.isEmpty()) {
                renderAPIError(ErrorBase.E403);
            }

            if (APPLICATION_JSON.equalsIgnoreCase(request.contentType)){
                if (!request.headers.containsKey(DarwinAuth.BODY_HASH_HEADER_NAME.toLowerCase())) {
                    renderAPIError(ErrorBase.E111);
                }

                String body = getBody();
                byte[] bodyArray = body.getBytes(DarwinAuth.CHARSET_UTF_8);
                expectedHeaders = getExpectedHeadersWhitBody(darwinAuth, xHeaders, xDate, bodyArray);
            } else if (APPLICATION_X_WWW_FORM_URLENCODED.equalsIgnoreCase(request.contentType)) {
                Map<String, String[]> bodyParams = getBodyParams();
                expectedHeaders = getExpectedHeaders(darwinAuth, xHeaders, xDate, bodyParams);
            } else if (MULTIPART_FORM_DATA.equalsIgnoreCase(request.contentType)){
                if(!request.headers.containsKey(DarwinAuth.FILE_HASH_HEADER_NAME.toLowerCase())) {
                    renderAPIError(ErrorBase.E110);
                }

                String hash = request.headers.get(DarwinAuth.FILE_HASH_HEADER_NAME.toLowerCase()).value();
                expectedHeaders = getExpectedHeaders(darwinAuth, xHeaders, xDate);
                renderArgs.put(FILE_HASH, hash);
            } else {
                renderAPIError(ErrorBase.E403);
            }
        } else {
            renderAPIError(ErrorBase.E404);
        }

        String expectedSignature = DarwinAuth.getSignatureFromHeader(expectedHeaders.get(DarwinAuth.AUTHORIZATION_HEADER_NAME));
        if (!signature.equals(expectedSignature)) {
            renderAPIError(ErrorBase.E102);
        }

        try {
            SimpleDateFormat utcDateFormat = new SimpleDateFormat(DarwinAuth.UTC_STRING_FORMAT);
            utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            if (utcDateFormat.parse(xDate).before(
                    new Date(System.currentTimeMillis() - DATE_EXPIRATION_MILLIS))) {
                renderAPIError(ErrorBase.E106);
            }
        } catch (NumberFormatException e) {
            // The long is read from the config file, this should not happen. Do nothing.
            e.printStackTrace();
        } catch (ParseException e) {
            renderAPIError(ErrorBase.E105);
        }

        renderArgs.put(API_CLIENT_ID, apiClientId);
        checkAccess(apiClient.getEmail());
    }

    private static void checkAccess(String email) {
        CheckAPI check = getActionAnnotation(CheckAPI.class);
        if(check != null) {
            check(check, email);
        }
        check = getControllerInheritedAnnotation(CheckAPI.class);
        if(check != null) {
            check(check, email);
        }
    }

    private static void check(CheckAPI check, String email) {
        User user =  DarwinFactory.getInstance().loadUser(email);
        UserRole role = DarwinFactory.getInstance().loadUserRole(user.getRoleId());
        if (role != null) {
            for (String profile : check.value()) {
                if (!role.hasPermission(profile)) {
                    renderAPIError(ErrorBase.E112);
                }
            }
        }else{
            renderAPIError(ErrorBase.E112);
        }
    }

    protected static boolean isHttpGET() {
        return "GET".equals(request.method.toUpperCase());
    }

    protected static boolean isHttpPOST() {
        return "POST".equals(request.method.toUpperCase());
    }

    protected static boolean isHttpPUT() {
        return "PUT".equals(request.method.toUpperCase());
    }

    protected static boolean isHttpDELETE() {
        return "DELETE".equals(request.method.toUpperCase());
    }


    protected static void checkInvalidRequestHttpVerb() {
        if (!isSupported(request.method.toUpperCase())) {
            renderAPIError(ErrorBase.E405);
        }

    }

    protected static boolean isSupported(String method) {
        String supported = Play.configuration.getProperty("APIController.httpSupported-" + method);
        return supported == null || Boolean.valueOf(supported);
    }

    protected static TreeMap<String,String> getXHeaders() {
        TreeMap<String,String> xHeaders = new TreeMap<String,String>();
        for(String headerName : request.headers.keySet()) {
            if(headerName.toLowerCase().startsWith(DarwinAuth.X_11PATHS_HEADER_PREFIX.toLowerCase()) &&
                    !DarwinAuth.DATE_HEADER_NAME.toLowerCase().equals(headerName.toLowerCase())) {
                xHeaders.put(headerName, request.headers.get(headerName).value());
            }
        }
        return xHeaders;
    }

    protected static Map<String, String> getExpectedHeaders(DarwinAuth darwinAuth, TreeMap<String,String> xHeaders, String xDate) throws UnsupportedEncodingException {
        return getExpectedHeaders(darwinAuth, xHeaders, xDate, null);
    }

    protected static Map<String, String> getExpectedHeaders(DarwinAuth darwinAuth, TreeMap<String,String> xHeaders, String xDate, Map<String, String[]> bodyParams) throws UnsupportedEncodingException {
        return darwinAuth.authenticationHeaders(request.method,
                request.path + (request.querystring.isEmpty() ? "" : "?" + request.querystring),
                xHeaders,
                bodyParams,
                xDate);
    }

    protected static Map<String, String> getExpectedHeadersWhitBody(DarwinAuth darwinAuth, TreeMap<String,String> xHeaders, String xDate, byte[] bodyArray) throws UnsupportedEncodingException {
        return darwinAuth.authenticationHeadersWithBody(request.method,
                request.path + (request.querystring.isEmpty() ? "" : "?" + request.querystring),
                xHeaders,
                bodyArray,
                xDate);
    }

    protected static Map<String, String[]> getBodyParams() throws UnsupportedEncodingException {
        Map<String, String[]> bodyParams = null;
        if (params.get("body") != null) {
            bodyParams = new HashMap<String, String[]>();
            for (String param : params.get("body").split(DarwinAuth.PARAM_SEPARATOR)) {
                if (param.contains(DarwinAuth.PARAM_VALUE_SEPARATOR)) {
                    String[] tokens = param.split(DarwinAuth.PARAM_VALUE_SEPARATOR);
                    if (bodyParams.get(URLDecoder.decode(tokens[0], DarwinAuth.CHARSET_UTF_8)) != null) {
                        List<String> values = new LinkedList<String>(Arrays.asList(bodyParams.get(URLDecoder.decode(tokens[0], DarwinAuth.CHARSET_UTF_8))));
                        values.add(tokens.length == 1 ? "" : URLDecoder.decode(tokens[1], DarwinAuth.CHARSET_UTF_8));
                        bodyParams.put(URLDecoder.decode(tokens[0], DarwinAuth.CHARSET_UTF_8), values.toArray(new String[values.size()]));
                    } else {
                        bodyParams.put(URLDecoder.decode(tokens[0], DarwinAuth.CHARSET_UTF_8), new String[] { tokens.length == 1 ? "" : URLDecoder.decode(tokens[1], DarwinAuth.CHARSET_UTF_8) });
                    }
                }
            }
        }
        return bodyParams;
    }

    protected static String getBody() throws IOException {
        String body = readBody(request.body);
        String bodyHash = DigestUtils.shaHex(body);
        if (!bodyHash.equals(request.headers.get(DarwinAuth.BODY_HASH_HEADER_NAME.toLowerCase()).value())) {
            renderAPIError(ErrorBase.E102);
        }

        return body;
    }

    protected static String readBody(InputStream is) throws IOException {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                    is.reset();
                    return sb.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    protected static void setData(JsonObject data) {
        if(renderArgs.get(APIResponse) != null) {
            ((APIResponse)renderArgs.get(APIResponse)).setData(data);
        }
    }

    protected static void setError(Error e) {
        if(renderArgs.get(APIResponse) != null) {
            ((APIResponse)renderArgs.get(APIResponse)).setError(e);
        }
    }

    protected static void renderAPIError(Error error) {
        setError(error);
        renderAPIResponse();
    }

    @After
    protected static void renderAPIResponse() {
        if (renderArgs.get(APIResponse) != null) {
            if (renderArgs.get(DEBUG_START_TIME)!= null) {
                response.headers.put(HTTP_DEBUG, new Header(HTTP_DEBUG, "time:"+(System.currentTimeMillis()-(Long)renderArgs.get(DEBUG_START_TIME)+"ms")));
            }
            renderJSON(((APIResponse)renderArgs.get(APIResponse)).toJSON().toString());
        } else {
            response.status = Http.StatusCode.BAD_REQUEST;
        }
    }
}