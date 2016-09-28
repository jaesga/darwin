package models.roles.permission;

import play.Play;

import java.util.*;

public class UserPermission {

    private static final java.lang.String FIELD_DELIMITER = ",";
    private static final String[] DEFAULT_PERMISSIONS = {"USER_READ","USER_DELETE","USER_ACTIVATE", "USER_SELF_DELETE", "USER_READ_PROFILES","ADMIN","API_CLIENTS"};

    private static final Set<String> permissions = new HashSet<String>();

    static {
        permissions.addAll(Arrays.asList(DEFAULT_PERMISSIONS));
        permissions.addAll(getApplicationPermissions());
    }

    private static List<String> getApplicationPermissions() {
        String permissions = Play.configuration.getProperty("permissions");
        List<String> applicationPermissions = new ArrayList<String>();
        if (permissions != null && !permissions.isEmpty()) {
            permissions = permissions.toUpperCase();
            applicationPermissions = Arrays.asList(permissions.split(FIELD_DELIMITER));
        }
        return applicationPermissions;
    }

    public static Set<String> getPermissions() {
        Set<String> permissionsCopy = new HashSet<String>();
        for (String permission : permissions) {
            permissionsCopy.add(permission);
        }
        return permissionsCopy;
    }

    public static void add(String permission) {
        permissions.add(permission.toUpperCase());
    }

    public static void add(Object[] perms){
        for (Object perm : perms){
            add(perm.toString());
        }
    }

    public static boolean exist(String permission) {
        return permissions.contains(permission.toUpperCase());
    }
}
