package models.api;

import com.ning.http.util.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import play.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class DarwinAuth {

    public static final String X_11PATHS_HEADER_PREFIX = "X-11paths-";
    private static final String X_11PATHS_HEADER_SEPARATOR = ":";

    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    public static final String DATE_HEADER_NAME = X_11PATHS_HEADER_PREFIX + "Date";
    public static final String BODY_HASH_HEADER_NAME = X_11PATHS_HEADER_PREFIX + "Body-Hash";
    public static final String FILE_HASH_HEADER_NAME = X_11PATHS_HEADER_PREFIX + "File-Hash";
    public static final String AUTHORIZATION_METHOD = "11PATHS";
    private static final String AUTHORIZATION_HEADER_FIELD_SEPARATOR = " ";

    public static final String UTC_STRING_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String HMAC_ALGORITHM = "HmacSHA1";

    public static final String CHARSET_ISO_8859_1 = "ISO-8859-1";
    public static final String CHARSET_UTF_8 = "UTF-8";
    public static final String PARAM_SEPARATOR = "&";
    public static final String PARAM_VALUE_SEPARATOR = "=";

    /**
     * The custom header consists of three parts, the method, the entity id and the signature
     * This method returns the specified part if it exists.
     * @param part The zero indexed part to be returned
     * @param header The HTTP header value from which to extract the part
     * @return the specified part from the header or an empty string if not existent
     */
    private static final String getPartFromHeader(int part, String header) {
        if (header != null) {
            String[] parts = header.split(AUTHORIZATION_HEADER_FIELD_SEPARATOR);
            if(parts.length > part) {
                return parts[part];
            }
        }
        return "";
    }

    /**
     *
     * @param authorizationHeader Authorization HTTP Header
     * @return the Authorization method. Typical values are "Basic", "Digest" or "11PATHS"
     */
    public static final String getAuthMethodFromHeader(String authorizationHeader) {
        return getPartFromHeader(0, authorizationHeader);
    }

    /**
     *
     * @param authorizationHeader Authorization HTTP Header
     * @return the requesting entity Id. Identifies the entity using the API
     */
    public static final String getEntityIdFromHeader(String authorizationHeader) {
        return getPartFromHeader(1, authorizationHeader);
    }

    /**
     *
     * @param authorizationHeader Authorization HTTP Header
     * @return the signature of the current request. Verifies the identity of the entity using the API
     */
    public static final String getSignatureFromHeader(String authorizationHeader) {
        return getPartFromHeader(2, authorizationHeader);
    }

    private String entityId;
    private String secretKey;

    /**
     * Create an instance of the class with the entity id and secret obtained from Darwin
     * @param entityId
     * @param secretKey
     */
    public DarwinAuth(String entityId, String secretKey){
        this.entityId = entityId;
        this.secretKey = secretKey;
    }

    /**
     *
     * @param data the string to sign
     * @return base64 encoding of the HMAC-SHA1 hash of the data parameter using {@code secretKey} as cipher key.
     */
    private String signData (String data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), HMAC_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(keySpec);
            return Base64.encode(mac.doFinal(data.getBytes(CHARSET_ISO_8859_1))); // data is ASCII except HTTP header values which can be ISO_8859_1
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Calculate the authentication headers to be sent with a request to the API
     * @param HTTPMethod the HTTP Method
     * @param queryString the urlencoded string including the path (from the first forward slash) and the parameters
     * @param xHeaders HTTP headers specific to the 11-paths API, excluding X-11Paths-Date. null if not needed.
     * @param params The HTTP request params. Must be only those to be sent in the body of the request and must be urldecoded. null if not needed.
     * @return a map with the Authorization and X-11Paths-Date headers needed to sign a Latch API request
     * @throws java.io.UnsupportedEncodingException If {@value CHARSET_UTF_8} charset is not supported.
     */
    public final Map<String, String> authenticationHeaders(String HTTPMethod, String queryString, Map<String,String>xHeaders, Map<String, String[]> params) throws UnsupportedEncodingException {
        String currentUTC=getCurrentUTC();
        return authenticationHeaders(HTTPMethod, queryString, xHeaders, params, currentUTC);
    }

    /**
     * Calculates the headers to be sent with a request to the API so the server
     * can verify the signature
     * <p>
     * Calls
     * {@link #authenticationHeadersWithBody(String, String, java.util.Map, byte[], String)}
     * with {@code body} converted to {@code byte[]} in {@value CHARSET_UTF_8}
     * charset and the current date as {@code utc}.
     * @param method The HTTP request method.
     * @param querystring The urlencoded string including the path (from the
     *        first forward slash) and the parameters.
     * @param xHeaders The HTTP request headers specific to the API, excluding
     *        X-11Paths-Date. null if not needed.
     * @param body The HTTP request body. Null if not needed.
     * @return A map with the {@value AUTHORIZATION_HEADER_NAME}, the {@value
     *         DATE_HEADER_NAME} and the {@value BODY_HASH_HEADER_NAME} headers
     *         needed to be sent with a request to the API.
     * @throws java.io.UnsupportedEncodingException If {@value CHARSET_UTF_8} charset is
     *         not supported.
     */
    public final Map<String, String> authenticationHeadersWithBody(String method, String querystring, Map<String, String> xHeaders, String body) throws UnsupportedEncodingException {
        String currentUTC = getCurrentUTC();
        return authenticationHeadersWithBody(method, querystring, xHeaders, body.getBytes(CHARSET_UTF_8), currentUTC);
    }

    /**
     *
     * Calculate the authentication headers to be sent with a request to the API
     * @param HTTPMethod the HTTP Method
     * @param queryString the urlencoded string including the path (from the first forward slash) and the parameters
     * @param xHeaders HTTP headers specific to the 11-paths API, excluding X-11Paths-Date. null if not needed.
     * @param params The HTTP request params. Must be only those to be sent in the body of the request and must be urldecoded. null if not needed.
     * @param utc the Universal Coordinated Time for the X-11Paths-Date HTTP header
     * @return a map with the Authorization and X-11Paths-Date headers needed to sign a Latch API request
     * @throws java.io.UnsupportedEncodingException If {@value CHARSET_UTF_8} charset is not supported.
     */
    //TODO: nonce
    public final Map<String, String> authenticationHeaders(String HTTPMethod, String queryString, Map<String,String>xHeaders, Map<String, String[]> params, String utc) throws UnsupportedEncodingException {
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append(HTTPMethod.toUpperCase().trim());
        stringToSign.append("\n");
        stringToSign.append(utc);
        stringToSign.append("\n");
        stringToSign.append(getSerializedHeaders(xHeaders));
        stringToSign.append("\n");
        stringToSign.append(queryString.trim());
        if (params != null && !params.isEmpty()) {
            String serializedParams = getSerializedParams(params);
            if (serializedParams != null && !serializedParams.isEmpty()) {
                stringToSign.append("\n");
                stringToSign.append(serializedParams);
            }
        }

        String signedData = signData(stringToSign.toString());
        String authorizationHeader = new StringBuilder(AUTHORIZATION_METHOD)
            .append(AUTHORIZATION_HEADER_FIELD_SEPARATOR)
            .append(this.entityId)
            .append(AUTHORIZATION_HEADER_FIELD_SEPARATOR)
            .append(signedData)
            .toString();

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(AUTHORIZATION_HEADER_NAME, authorizationHeader);
        headers.put(DATE_HEADER_NAME, utc);
        return headers;
    }

    /**
     * Calculates the headers to be sent with a request to the API so the server
     * can verify the signature
     * @param method The HTTP request method.
     * @param querystring The urlencoded string including the path (from the
     *        first forward slash) and the parameters.
     * @param xHeaders The HTTP request headers specific to the API, excluding
     *        X-11Paths-Date. null if not needed.
     * @param body The HTTP request body. Null if not needed.
     * @param utc the Universal Coordinated Time for the X-11Paths-Date HTTP
     *        header
     * @return A map with the {@value AUTHORIZATION_HEADER_NAME}, the {@value
     *         DATE_HEADER_NAME} and the {@value BODY_HASH_HEADER_NAME} headers
     *         needed to be sent with a request to the API.
     * @throws java.io.UnsupportedEncodingException If {@value CHARSET_UTF_8} charset is
     *         not supported.
     */
    public final Map<String, String> authenticationHeadersWithBody(String method, String querystring, Map<String, String> xHeaders, byte[] body, String utc) throws UnsupportedEncodingException {
        String bodyHash = null;
        if (body != null) {
            bodyHash = DigestUtils.shaHex(body);
            if (xHeaders == null) {
                xHeaders = new HashMap<String, String>();
            }
            xHeaders.put(BODY_HASH_HEADER_NAME, bodyHash);
        }
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append(method.toUpperCase().trim());
        stringToSign.append("\n");
        stringToSign.append(utc);
        stringToSign.append("\n");
        stringToSign.append(getSerializedHeaders(xHeaders));
        stringToSign.append("\n");
        stringToSign.append(querystring.trim());
        String signedData = signData(stringToSign.toString());
        String authorizationHeader = new StringBuilder(AUTHORIZATION_METHOD)
            .append(AUTHORIZATION_HEADER_FIELD_SEPARATOR)
            .append(this.entityId)
            .append(AUTHORIZATION_HEADER_FIELD_SEPARATOR)
            .append(signedData)
            .toString();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(AUTHORIZATION_HEADER_NAME, authorizationHeader);
        headers.put(DATE_HEADER_NAME, utc);
        if (bodyHash != null) {
            headers.put(BODY_HASH_HEADER_NAME, bodyHash);
        }
        return headers;
    }

    /**
     * Prepares and returns a string ready to be signed from the 11-paths specific HTTP headers received
     * @param xHeaders a non necessarily ordered map of the HTTP headers to be ordered without duplicates.
     * @return a String with the serialized headers, an empty string if no headers are passed, or null if there's a problem
     * such as non specific 11paths headers
     */
    private String getSerializedHeaders(Map<String, String> xHeaders) {
        if(xHeaders != null) {
            TreeMap<String,String> sortedMap = new TreeMap<String,String>();
            for(String key : xHeaders.keySet()) {
                if(!key.toLowerCase().startsWith(X_11PATHS_HEADER_PREFIX.toLowerCase())) {
                    //TODO: Log this better
                    Logger.error("Error serializing headers. Only specific " + X_11PATHS_HEADER_PREFIX + " headers need to be singed");
                }
                sortedMap.put(key.toLowerCase(), xHeaders.get(key));
            }
            StringBuilder serializedHeaders = new StringBuilder();
            for(String key : sortedMap.keySet()) {
                serializedHeaders.append(key).append(X_11PATHS_HEADER_SEPARATOR).append(sortedMap.get(key)).append(" ");
            }
            return serializedHeaders.toString().trim();
        } else {
            return "";
        }
    }

    /**
     * Prepares and returns a string ready to be signed from the params of an
     * HTTP request
     * <p>
     * The params must be only those included in the body of the HTTP request
     * when its content type is application/x-www-urlencoded and must be
     * urldecoded.
     * @param params The params of an HTTP request.
     * @return A serialized representation of the params ready to be signed.
     *         null if there are no valid params.
     * @throws java.io.UnsupportedEncodingException If {@value CHARSET_UTF_8} charset is
     *         not supported.
     */
    public String getSerializedParams(Map<String, String[]> params) throws UnsupportedEncodingException {
        if (params != null && !params.isEmpty()) {
            TreeMap<String, List<String>> sortedParams = new TreeMap<String, List<String>>();
            for (String key : params.keySet()) {
                if (key != null && params.get(key) != null) {
                    List<String> sortedValues = new ArrayList<String>();
                    for (String value : params.get(key)) {
                        if (value != null) {
                            sortedValues.add(value);
                        }
                    }
                    Collections.sort(sortedValues);
                    sortedParams.put(key, sortedValues);
                }
            }
            StringBuilder serializedParams = new StringBuilder();
            for (String key : sortedParams.keySet()) {
                List<String> sortedValues = sortedParams.get(key);
                for (String value : sortedValues) {
                    serializedParams.append(URLEncoder.encode(key, CHARSET_UTF_8));
                    serializedParams.append(PARAM_VALUE_SEPARATOR);
                    serializedParams.append(URLEncoder.encode(value, CHARSET_UTF_8));
                    serializedParams.append(PARAM_SEPARATOR);
                }
                if (serializedParams.length() > 0 && serializedParams.charAt(serializedParams.length() - 1) == '&') {
                    serializedParams.replace(serializedParams.length() - 1, serializedParams.length(), "");
                }
            }
            return serializedParams.length() > 0 ? serializedParams.toString() : null;
        } else {
            return null;
        }
    }

    public String parseQueryParams(Map<String, String[]> queryParams) throws UnsupportedEncodingException {
        if (queryParams == null || queryParams.isEmpty()) {
            return "";
        }
        String query = queryParams.size() > 0 ? "?" : "";
        TreeMap<String, String[]> sortedParams = new TreeMap<String, String[]>(queryParams);
        for (Map.Entry<String, String[]> param: sortedParams.entrySet()) {
            String key = param.getKey();
            List<String> values = Arrays.asList(param.getValue());
            Collections.sort(values);
            for (String value : values) {
                query += serializeParam(key, value) + PARAM_SEPARATOR;
            }
        }
        return (query.length() > 0 && query.charAt(query.length() - 1) == '&') ? query.substring(0, query.length() - 1) : query;
    }

    protected static String serializeParam(String key, String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(key, CHARSET_UTF_8) + PARAM_VALUE_SEPARATOR + URLEncoder.encode(value, CHARSET_UTF_8);
    }

    /**
     *
     * @return a string representation of the current time in UTC to be used in a Date HTTP Header
     */
    private final String getCurrentUTC() {
        final SimpleDateFormat sdf = new SimpleDateFormat(UTC_STRING_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());

    }
}
