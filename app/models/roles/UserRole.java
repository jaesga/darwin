package models.roles;


import models.roles.permission.UserPermission;
import models.utils.AuthUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class UserRole {

    protected static final int USER_ROLE_ID_LENGTH = 20;

    protected String id;
    protected String name;
    protected Set<String> permissions = new HashSet<String>();

    protected UserRole() {}

    protected UserRole(String name) {
        this.name = name;
        this.id = AuthUtils.generateToken(USER_ROLE_ID_LENGTH);
        this.permissions = new HashSet<String>();
    }

    protected UserRole(String id, String name, Set<String> permissions) {
        this.id = id;
        this.name = name;
        this.permissions = permissions;
    }

    public abstract void save();
    public abstract void remove();

    public List<String> getPermissionsAsList() {
        List<String> perms = new ArrayList<String>();
        for (String permission : permissions){
            perms.add(permission);
        }
        return perms;
    }

    public void addPermission(String permission) {
        permission = permission.toUpperCase();
        if (UserPermission.exist(permission)) {
            permissions.add(permission);
        } else {
            throw new IllegalArgumentException("There is no permission role matching name: " + permission);
        }
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission.toUpperCase());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = new HashSet<String>();
        if (permissions != null){
            for (String permission : permissions){
                addPermission(permission);
            }
        }
    }
}
