package models.roles;

import com.mongodb.*;
import models.Constants;
import models.factory.MongoFactory;

import java.util.HashSet;
import java.util.Set;

public class MongoUserRole extends UserRole {

    public MongoUserRole(String name) {
        super(name);
    }

    public MongoUserRole(String id, String name, Set<String> permissions) {
        super(id, name, permissions);
    }

    public MongoUserRole(DBObject u) {
        this.id = u.containsField(Constants.UserRole.FIELD_ID) ? u.get(Constants.UserRole.FIELD_ID).toString() : "";
        this.name = u.containsField(Constants.UserRole.FIELD_NAME) ? u.get(Constants.UserRole.FIELD_NAME).toString() : "";
        if(u.containsField(Constants.UserRole.FIELD_PERMISSIONS)) {
            BasicDBList userPermissionList = (BasicDBList) u.get(Constants.UserRole.FIELD_PERMISSIONS);
            permissions = new HashSet<String>();
            for (Object userPermission : userPermissionList){
                addPermission(userPermission.toString());
            }

        }
    }

    @Override
    public void save() {
        DB db = MongoFactory.getDB();
        String rolesCollectionName = getCollectionName();
        DBCollection rolesCollection = db.getCollection(rolesCollectionName);
        BasicDBObject updatedValues = new BasicDBObject();
        updatedValues.append(Constants.UserRole.FIELD_ID, this.id);
        updatedValues.append(Constants.UserRole.FIELD_NAME, this.name);
        updatedValues.append(Constants.UserRole.FIELD_PERMISSIONS, getPermissionsAsList());
        updatedValues = beforeSave(updatedValues);
        rolesCollection.update(new BasicDBObject(Constants.UserRole.FIELD_ID, this.id), new BasicDBObject().append("$set", updatedValues), true, false);
    }

    protected BasicDBObject beforeSave(BasicDBObject userRoleParams) {
        return userRoleParams;
    }

    @Override
    public void remove() {
        DB db = MongoFactory.getDB();
        String rolesCollectionName = getCollectionName();
        DBCollection roles = db.getCollection(rolesCollectionName);
        roles.remove(new BasicDBObject(Constants.UserRole.FIELD_ID, this.id));
    }


    protected String getCollectionName() {
        return Constants.UserRole.COLLECTION_NAME;
    }
}
