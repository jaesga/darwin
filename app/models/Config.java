package models;

import models.user.ActivationType;
import play.Logger;
import play.Play;

import java.util.Arrays;
import java.util.List;

public class Config {

    private static final String DB_NAME_DEFAULT = Play.configuration.getProperty("Config.dbNameDefault") != null ? Play.configuration.getProperty("Config.dbNameDefault") : "play_module" ;
    private static final String DB_NAME_PROPERTY_PREFIX = Play.configuration.getProperty("Config.dbNamePropertyPrefix") != null ? Play.configuration.getProperty("Config.dbNamePropertyPrefix") : "play_module.db." ;
    private static final String DB_NAME_PROPERTY_SUFFIX = Play.configuration.getProperty("Config.dbNamePropertySuffix") != null ? Play.configuration.getProperty("Config.dbNamePropertySuffix") : ".name" ;

    private static final ActivationType ACTIVATION_USER_MODE = setActivationType();
    private static final Boolean CREATE_DEFAULT_API_CLIENT = Play.configuration.getProperty("Config.createDefaultAPIClient") != null ? (Boolean) Boolean.valueOf(Play.configuration.getProperty("Config.createDefaultAPIClient")) : true ;

    private static final boolean MOBILE_CONNECT_ACTIVE = Play.configuration.getProperty("mobileConnect.active") != null ? (Boolean) Boolean.valueOf(Play.configuration.getProperty("mobileConnect.active")) : true ;
    private static final boolean MOBILE_CONNECT_WITH_LATCH_ACTIVE = Play.configuration.getProperty("mobileConnect.latch.active") != null ? (Boolean) Boolean.valueOf(Play.configuration.getProperty("mobileConnect.latch.active")) : false ;

    private static final boolean LATCH_ACTIVE = Play.configuration.getProperty("latch.active") != null ? (Boolean) Boolean.valueOf(Play.configuration.getProperty("latch.active")) : false ;
    private static final String LATCH_MODE_NONE = "none";
    private static final String LATCH_MODE_ALERT = "alert";
    private static final String LATCH_MODE_MANDATORY = "pair_mandatory";
    private static final String LATCH_MODE = Play.configuration.getProperty("latch.mode") != null ? Play.configuration.getProperty("latch.mode") : LATCH_MODE_NONE;

    private static final String RESET_PASSWORD_POLICY_REQUEST = "request";
    private static final String RESET_PASSWORD_POLICY_EXPIRATION = "expiration";
    private static final String RESET_PASSWORD_POLICY_FORBIDDEN_N_LAST_USED = "n_forbidden";
    private static final String RESET_PASSWORD_POLICY_BOTH = "both";
    private static final String RESET_PASSWORD_POLICY = RESET_PASSWORD_POLICY_BOTH;

    private static final String FIELD_DELIMITER = ",";
    private static final boolean CHANGE_LOG_ACTIVE = Boolean.parseBoolean(Play.configuration.getProperty("changelog.enabled", "false"));

    private static final boolean ZENDESK_ENABLED = Boolean.parseBoolean(Play.configuration.getProperty("zendesk.enabled", "false"));

    /**
     * Get the default database name
     * @return the default database name
     */
    public static String getDBName() {
        return getDBName(DB_NAME_DEFAULT);
    }

    /**
     * Get the database name
     * @param alias the database alias
     * @return the database name
     */
    public static String getDBName(String alias) {

        String rv = alias;
        String propertyName = DB_NAME_PROPERTY_PREFIX + alias + DB_NAME_PROPERTY_SUFFIX;

        if (Play.configuration.getProperty(propertyName) != null && !Play.configuration.getProperty(propertyName).isEmpty()) {
            rv = Play.configuration.getProperty(propertyName);
        }

        return rv;
    }

    public static boolean isActiveCreateDefaultAPIClient() {
        return CREATE_DEFAULT_API_CLIENT;
    }

    public static boolean isAutoActivatedUser(String email) {
        String autoActivatedUsers = Play.configuration.getProperty("auto_activated_users");
        if (autoActivatedUsers != null) {
            List<String> users = Arrays.asList(autoActivatedUsers.split(FIELD_DELIMITER));
            return users.contains(email);
        } else {
            return false;
        }
    }

    public static ActivationType getUserActivationType(){
        return ACTIVATION_USER_MODE;
    }

    public static boolean isMobileConnectActive() {
        return MOBILE_CONNECT_ACTIVE;
    }

    public static boolean isMobileConnectWithLatchActive() {
        return MOBILE_CONNECT_WITH_LATCH_ACTIVE;
    }

    public static boolean isLatchActive() {
        return LATCH_ACTIVE;
    }

    public static boolean isLatchModeAlert() {
        return LATCH_ACTIVE && LATCH_MODE.equals(LATCH_MODE_ALERT);
    }

    public static boolean isLatchModeMandatory() {
        return LATCH_ACTIVE && LATCH_MODE.equals(LATCH_MODE_MANDATORY);
    }

    public static boolean isResetPasswordPolicyExpirationActive() {
        return RESET_PASSWORD_POLICY.equals(RESET_PASSWORD_POLICY_EXPIRATION) || RESET_PASSWORD_POLICY.equals(RESET_PASSWORD_POLICY_BOTH);
    }

    public static boolean isResetPasswordPolicyForbiddenNLastUsedActive() {
        return RESET_PASSWORD_POLICY.equals(RESET_PASSWORD_POLICY_FORBIDDEN_N_LAST_USED) || RESET_PASSWORD_POLICY.equals(RESET_PASSWORD_POLICY_BOTH);
    }

    public static String getApplicationName(){
        if (Play.configuration.getProperty("Config.ApplicationName") != null && !Play.configuration.getProperty("Config.ApplicationName").isEmpty()) {
            return Play.configuration.getProperty("Config.ApplicationName");
        }else{
            return "Darwin";
        }
    }

    private static ActivationType setActivationType(){
        ActivationType activationType = ActivationType.TOKEN;
        String aTypeStr = Play.configuration.getProperty("Config.userActivation", "token");
        if (aTypeStr != null){
            try{
                activationType = ActivationType.valueOf(aTypeStr.toUpperCase());
            }catch (IllegalArgumentException e){
                Logger.error("Activation type is 'token' by default due to malformed application.conf");
            }
        }
        return activationType;
    }

    public static boolean isChangelogActivated() {
        return CHANGE_LOG_ACTIVE;
    }

    public static boolean isZendeskEnabled() {
        return ZENDESK_ENABLED;
    }
}
