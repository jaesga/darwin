package models;


import play.Play;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {

    public static class APIClient {
        public static final String COLLECTION_NAME = Play.configuration.getProperty("Constants.APIClient.collectionName", "api_clients" );
        public static final String FIELD_NAME = Play.configuration.getProperty("Constants.APIClient.fieldName", "name");
        public static final String FIELD_SECRET = Play.configuration.getProperty("Constants.APIClient.fieldSecret",  "secret");
        public static final String FIELD_EMAIL = Play.configuration.getProperty("Constants.APIClient.fieldEmail", "email");
        public static final String FIELD_ID = Play.configuration.getProperty("Constants.APIClient.fieldId", "clientId");
    }

    public static class User {
        public static final String COLLECTION_NAME = Play.configuration.getProperty("Constants.User.collectionName", "users");
        public static final String FIELD_NAME = Play.configuration.getProperty("Constants.User.fieldName", "name");
        public static final String FIELD_EMAIL = Play.configuration.getProperty("Constants.User.fieldEmail", "email");
        public static final String FIELD_CREATED = Play.configuration.getProperty("Constants.User.fieldCreated", "t");
        public static final String FIELD_PASSWORD = Play.configuration.getProperty("Constants.User.fieldPassword", "password");
        public static final String FIELD_PASSWORD_CHANGE = Play.configuration.getProperty("Constants.User.fieldPasswordChange", "passChange");
        public static final String FIELD_AUTHENTICATION_ATTEMPTS = Play.configuration.getProperty("Constants.User.fieldAuthenticationAttempts", "authenticationAttempts");
        public static final String FIELD_ACTIVE = Play.configuration.getProperty("Constants.User.fieldActive", "active");
        public static final String FIELD_PREFERRED_LANG = Play.configuration.getProperty("Constants.User.fieldPreferredLang", "preferredLang");
        public static final String FIELD_LATCH_ID = Play.configuration.getProperty("Constants.User.fieldLatchId", "latchId");
        public static final String FIELD_LATCH_OTP = Play.configuration.getProperty("Constants.User.fieldLatchOtp", "latchOtp");
        public static final String FIELD_LATCH_OTP_ATTEMPTS = Play.configuration.getProperty("Constants.User.fieldLatchOtpAttempts", "latchOtpAttempts");
        public static final String FIELD_ROLE = Play.configuration.getProperty("Constants.User.fieldRole", "role");
        public static final String FIELD_MOBILE_CONNECT_ID = Play.configuration.getProperty("Constants.User.mobileConnectId", "mobileConnectId");
        public static final String FIELD_ENABLED_ALERT_MESSAGE = Play.configuration.getProperty("Constants.User.enabledAlertMessage", "enabledAlertMessage");
        public static final double PASSWORD_EXPIRATION_DAYS = Play.configuration.getProperty("Constants.User.passwordExpirationDays") != null ? Double.parseDouble(Play.configuration.getProperty("Constants.User.passwordExpirationDays")) : (double) 180;
        public static final double PASSWORD_EXPIRATION_MILLIS = 24 * 60 * 60 * 1000 * PASSWORD_EXPIRATION_DAYS;
        public static final String FIELD_OLD_PASSWORDS = "oldPasswords";
        public static final int MAX_OLD_PASSWORDS_STORED = Integer.parseInt(Play.configuration.getProperty("Constants.User.maxOldPasswordsStored", "0"));
        public static final int MAX_AUTHENTICATION_ATTEMPTS = Integer.parseInt(Play.configuration.getProperty("Constants.User.maxAuthenticationAttempts", "3"));
        public static final int MIN_PASSWORD_LENGTH = Integer.parseInt(Play.configuration.getProperty("Constants.User.minPasswordLength", "8"));
        public static final String FIELD_CHANGELOG_READ = "changelogRead";
        public static final String FIELD_IGNORE_CHANGELOG = "ignoreChangeLog";
    }

    public static class UserRole {
        public static final String COLLECTION_NAME = Play.configuration.getProperty("Constants.UserRole.collectionName", "user_role");
        public static final String FIELD_ID = Play.configuration.getProperty("Constants.UserRole.fieldId", "id");
        public static final String FIELD_NAME = Play.configuration.getProperty("Constants.UserRole.fieldName", "name");
        public static final String FIELD_PERMISSIONS = Play.configuration.getProperty("Constants.UserRole.fieldPermissions", "permissions");
        public static final String SUPER_ADMIN = Play.configuration.getProperty("Constants.UserRole.superAdmin", "SuperAdmin");
        public static final String DEFAULT_ROLE = Play.configuration.getProperty("Constants.UserRole.default", "RegularUser");
        public static Map<String, Boolean> specialRole = new HashMap<String, Boolean>() {{
            put(SUPER_ADMIN, false);
            put(DEFAULT_ROLE, true);
        }};
    }

    public static class RemovedUser {
        public static final String COLLECTION_NAME = Play.configuration.getProperty("Constants.RemovedUser.collectionName", "removed_users");
    }

    public static class Token {
        public static final String RESET_PASSWORD_COLLECTION_NAME = Play.configuration.getProperty("Constants.Token.resetPasswordCollectionName", "resetPasswordTokens");
        public static final String ACTIVATE_ACCOUNT_COLLECTION_NAME = Play.configuration.getProperty("Constants.Token.activateAccountCollectionName", "activateAccountTokens");
        public static final String INVITE_USER_COLLECTION_NAME = Play.configuration.getProperty("Constants.Token.inviteUserCollectionName", "invitationUserTokens");
        public static final String FIELD_EMAIL = Play.configuration.getProperty("Constants.Token.fieldEmail", "email");
        public static final String FIELD_TOKEN = Play.configuration.getProperty("Constants.Token.fieldToken", "token");
        public static final String FIELD_GENERATED = Play.configuration.getProperty("Constants.Token.fieldGenerated", "generated");
        public static final String FIELD_DATA = Play.configuration.getProperty("Constants.Token.fieldData", "data");
        public static final String DATA_LANG = "lang";
    }

    public static class Logger {
        public static final String COLLECTION_NAME = Play.configuration.getProperty("Constants.Logger.collectionName", "action_log");
        public static final String CREATE_USER_ACTION = Play.configuration.getProperty("Constants.Logger.createUserAction", "user_create");
        public static final String REMOVE_USER_ACTION = Play.configuration.getProperty("Constants.Logger.removeUserAction", "user_remove");
        public static final String PASSWORD_CHANGE_ACTION = Play.configuration.getProperty("Constants.Logger.passwordChangeAction", "user_setpassword");
        public static final String FIELD_ACTION = Play.configuration.getProperty("Constants.Logger.fieldAction", "a");
        public static final String FIELD_TIMESTAMP = Play.configuration.getProperty("Constants.Logger.fieldTimestamp", "t");
        public static final String FIELD_PARAMETERS = Play.configuration.getProperty("Constants.Logger.fieldParameters", "p");
    }

    public static class MobileConnect {

        public static final String SESSION = "MobileConnect";
        public static final String ACTION_LOGIN = "login";
        public static final String ACTION_PAIR = "pair";
        public static final String CONSUMER_ID = Play.configuration.getProperty("mobileConnect.consumerId", "");
        public static final String CONSUMER_SECRET = Play.configuration.getProperty("mobileConnect.consumerSecret", "");
        public static final String REDIRECT_URI = Play.configuration.getProperty("application.baseUrl") != null && Play.configuration.getProperty("mobileConnect.redirectUri") != null ?
                                                        Play.configuration.getProperty("application.baseUrl") + Play.configuration.getProperty("mobileConnect.redirectUri") : "";

        public static class Token {
            public static final String GRANT_TYPE = Play.configuration.getProperty("mobileConnect.token.grantType", "authorization_code");
        }

        public static class Authorize {
            public static final String RESPONSE_TYPE = Play.configuration.getProperty("mobileConnect.authorize.responseType", "code");
            public static final String SCOPE = Play.configuration.getProperty("mobileConnect.authorize.scope", "openid");
            public static final String ACR_VALUES = Play.configuration.getProperty("mobileConnect.authorize.acrValues", "3");
        }

        public static class Error {
            public static final String USER_CANCEL = "USER_CANCEL";
        }
    }

    public static class Utils {
        public static final int MAX_RESULTS_PAGE = Integer.parseInt(Play.configuration.getProperty("utils.pagination.maxResults", "10"));
    }

    public static class ChangeLogPoint {
        public static final String COLLECTION_NAME = "changelog";
        public static final Integer ID_LENGTH = 20;
        public static final String FIELD_ID = "id";
        public static final String FIELD_VERSION = "version";
        public static final String FIELD_MESSAGE = "message";
        public static final String FIELD_TITLE = "title";
        public static final String FIELD_CONTENT = "content";
        public static final String DEFAULT_LANGUAGE = "en";
    }

    public static class ZendeskApi {
        public static final String SERVICE_URL = Play.configuration.getProperty("zendesk.url");
        public static final String SERVICE_USER = Play.configuration.getProperty("zendesk.user");
        public static final String SERVICE_KEY = Play.configuration.getProperty("zendesk.key");
        public static final String FIELD_NAME = "name";
        public static final String FIELD_EMAIL = "email";
        public static final String FIELD_REQUESTER = "requester";
        public static final String FIELD_SUBJECT = "subject";
        public static final String FIELD_COMMENT = "comment";
        public static final String FIELD_TAGS = "tags";
        public static final String FIELD_TICKET = "ticket";
    }
}
