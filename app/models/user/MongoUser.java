package models.user;


import com.mongodb.*;
import controllers.WebController;
import models.Config;
import models.Constants;
import models.api.APIClient;
import models.factory.DarwinFactory;
import models.factory.MongoFactory;
import models.utils.AuthUtils;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import play.Play;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MongoUser extends UserImpl {

    public MongoUser(String name, String email, String password) {
        super(name, email, password);
    }

    public MongoUser(DBObject u) {
        this.name = u.containsField(Constants.User.FIELD_NAME) ? u.get(Constants.User.FIELD_NAME).toString() : "";
        this.email = u.containsField(Constants.User.FIELD_EMAIL) ? u.get(Constants.User.FIELD_EMAIL).toString() : "";
        this.password = u.containsField(Constants.User.FIELD_PASSWORD) ? u.get(Constants.User.FIELD_PASSWORD).toString() : "";
        this.passwordChange = u.containsField(Constants.User.FIELD_PASSWORD_CHANGE) ? (Date)u.get(Constants.User.FIELD_PASSWORD_CHANGE) : new Date();
        this.authenticationAttempts = u.containsField(Constants.User.FIELD_AUTHENTICATION_ATTEMPTS) && u.get(Constants.User.FIELD_AUTHENTICATION_ATTEMPTS) != null ? (Integer) u.get(Constants.User.FIELD_AUTHENTICATION_ATTEMPTS) : 0;
        this.created = u.containsField(Constants.User.FIELD_CREATED) ? (Date)u.get(Constants.User.FIELD_CREATED) : new Date();
        this.active = u.containsField(Constants.User.FIELD_ACTIVE) ? (Boolean)u.get(Constants.User.FIELD_ACTIVE) : false;
        this.preferredLang = u.containsField(Constants.User.FIELD_PREFERRED_LANG) ? u.get(Constants.User.FIELD_PREFERRED_LANG).toString() : WebController.getLanguage();
        this.latchId = u.containsField(Constants.User.FIELD_LATCH_ID) && u.get(Constants.User.FIELD_LATCH_ID) != null ? u.get(Constants.User.FIELD_LATCH_ID).toString() : "";
        this.latchOtp = u.containsField(Constants.User.FIELD_LATCH_OTP) && u.get(Constants.User.FIELD_LATCH_OTP) != null ? u.get(Constants.User.FIELD_LATCH_OTP).toString() : "";
        this.latchOtpAttempts = u.containsField(Constants.User.FIELD_LATCH_OTP_ATTEMPTS)  ? (Integer) u.get(Constants.User.FIELD_LATCH_OTP_ATTEMPTS) : 0;
        this.roleId = u.containsField(Constants.User.FIELD_ROLE) ? u.get(Constants.User.FIELD_ROLE).toString() : "";
        this.mobileConnectId = u.containsField(Constants.User.FIELD_MOBILE_CONNECT_ID) && u.get(Constants.User.FIELD_MOBILE_CONNECT_ID) != null ? u.get(Constants.User.FIELD_MOBILE_CONNECT_ID).toString() : "";
        this.enabledLatchAlertMessage = u.containsField(Constants.User.FIELD_ENABLED_ALERT_MESSAGE) && u.get(Constants.User.FIELD_ENABLED_ALERT_MESSAGE) != null ? (Boolean) u.get(Constants.User.FIELD_ENABLED_ALERT_MESSAGE) : true;
        this.changelogRead = u.containsField(Constants.User.FIELD_CHANGELOG_READ) ? (Boolean) u.get(Constants.User.FIELD_CHANGELOG_READ) : false;
        this.ignoreChangelog = u.containsField(Constants.User.FIELD_IGNORE_CHANGELOG) ? (Boolean) u.get(Constants.User.FIELD_IGNORE_CHANGELOG) : false;

        if (Constants.User.MAX_OLD_PASSWORDS_STORED > 1) {
            this.oldPasswords = new CircularFifoQueue<String>(Constants.User.MAX_OLD_PASSWORDS_STORED - 1);
            if (u.containsField(Constants.User.FIELD_OLD_PASSWORDS) && u.get(Constants.User.FIELD_OLD_PASSWORDS) != null) {
                BasicDBList basicDBList = (BasicDBList) u.get(Constants.User.FIELD_OLD_PASSWORDS);
                String[] oldPasswordsList = basicDBList.toArray(new String[basicDBList.size()]);
                this.oldPasswords.addAll(Arrays.asList(oldPasswordsList));
            }
        } else {
            this.oldPasswords = new CircularFifoQueue<String>();
        }

    }

    protected BasicDBObject beforeGetAsDBObject(BasicDBObject mongoUserParams) {
        return mongoUserParams;
    }

    protected BasicDBObject getAsDBObject() {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.append(Constants.User.FIELD_NAME, this.name);
        basicDBObject.append(Constants.User.FIELD_EMAIL, this.email);
        basicDBObject.append(Constants.User.FIELD_PASSWORD, this.password);
        basicDBObject.append(Constants.User.FIELD_PASSWORD_CHANGE, this.passwordChange);
        basicDBObject.append(Constants.User.FIELD_CREATED, this.created);
        basicDBObject.append(Constants.User.FIELD_AUTHENTICATION_ATTEMPTS, this.authenticationAttempts);
        basicDBObject.append(Constants.User.FIELD_ACTIVE, this.active);
        basicDBObject.append(Constants.User.FIELD_PREFERRED_LANG, this.preferredLang);
        basicDBObject.append(Constants.User.FIELD_LATCH_ID, this.latchId);
        basicDBObject.append(Constants.User.FIELD_LATCH_OTP, this.latchOtp);
        basicDBObject.append(Constants.User.FIELD_LATCH_OTP_ATTEMPTS, this.latchOtpAttempts);
        basicDBObject.append(Constants.User.FIELD_ROLE, this.roleId);
        basicDBObject.append(Constants.User.FIELD_MOBILE_CONNECT_ID, this.mobileConnectId);
        basicDBObject.append(Constants.User.FIELD_ENABLED_ALERT_MESSAGE, this.enabledLatchAlertMessage);
        basicDBObject.append(Constants.User.FIELD_CHANGELOG_READ, this.changelogRead);
        basicDBObject.append(Constants.User.FIELD_IGNORE_CHANGELOG, this.ignoreChangelog);
        basicDBObject.append(Constants.User.FIELD_OLD_PASSWORDS, this.oldPasswords);
        basicDBObject = beforeGetAsDBObject(basicDBObject);
        return basicDBObject;
    }

    protected BasicDBObject beforeSave(BasicDBObject mongoUserParams) {
        return mongoUserParams;
    }

    @Override
    public boolean save() {
        DB db = MongoFactory.getDB();
        DBCollection appsCollection = db.getCollection(getCollectionName());
        BasicDBObject updatedValues = getAsDBObject();
        updatedValues = beforeSave(updatedValues);
        WriteResult saveResult = appsCollection.update(getIdQuery(), new BasicDBObject().append("$set", updatedValues), true, false);
        return !saveResult.isUpdateOfExisting();
    }

    @Override
    public void remove() {
        DB db = MongoFactory.getDB();
        String userCollectionName = getCollectionName();
        DBCollection users = db.getCollection(userCollectionName);

        // Backup user before removing it
        String removedUserCollectionName = getRemovedUserCollectionName();
        DBCollection usersRemoved = db.getCollection(removedUserCollectionName);
        usersRemoved.insert(new BasicDBObject("date", new Date()).append("user", getAsDBObject()));
        // Remove the account itself
        BasicDBObject user = getIdQuery();
        users.remove(user);
    }

    @Override
    public boolean isExistingUser() {
        DB db = MongoFactory.getDB();
        String userCollectionName = getCollectionName();
        DBCollection users = db.getCollection(userCollectionName);
        BasicDBObject userQuery = getIdQuery();
        DBObject user = users.findOne(userQuery);
        return user != null;
    }

    @Override
    public boolean authenticate(String password) {
        return active && this.password.equals(AuthUtils.hashPassword(password, getId().toLowerCase()));
    }

    @Override
    public void changePassword(String password) {
        oldPasswords.add(this.password);
        setPassword(AuthUtils.hashPassword(password, getId()));
        save();
    }

    @Override
    public void createDefaultAPIClient() {
        if (Config.isActiveCreateDefaultAPIClient() && active) {
            APIClient apiClient = DarwinFactory.getInstance().buildAPIClient(name, email);
            if (!apiClient.isExistingAPIClient()) {
                apiClient.save();
            }
        }
    }

    @Override
    public boolean isUserActivable() {
        String autoActivatedUsers = Play.configuration.getProperty("auto_activated_users");
        if (autoActivatedUsers != null) {
            List<String> users = Arrays.asList(autoActivatedUsers.split(User.FIELD_DELIMITER));
            return users.contains(getId());
        } else {
            return false;
        }
    }

    @Override
    public boolean isAdminUser() {
        String autoAdminUsers = Play.configuration.getProperty("auto_admin_users");
        if (autoAdminUsers != null) {
            List<String> users = Arrays.asList(autoAdminUsers.split(User.FIELD_DELIMITER));
            return Constants.UserRole.SUPER_ADMIN.equals(this.roleId) || users.contains(getId());
        } else {
            return false;
        }
    }

    protected BasicDBObject getIdQuery() {
        return new BasicDBObject(Constants.User.FIELD_EMAIL, this.email);
    }

    public String getId() {
        return this.email;
    }

    protected String getCollectionName() {
        return Constants.User.COLLECTION_NAME;
    }

    protected String getRemovedUserCollectionName() {
        return Constants.RemovedUser.COLLECTION_NAME;
    }

    public boolean checkPasswordUsedInThePast(String newPassword) {
        newPassword = AuthUtils.hashPassword(newPassword, getId().toLowerCase());
        if (Constants.User.MAX_OLD_PASSWORDS_STORED == 1) {
            return newPassword != null && getPassword().equals(newPassword);
        } else if (Constants.User.MAX_OLD_PASSWORDS_STORED > 1) {
            return newPassword != null && (getPassword().equals(newPassword) || getOldPasswords().contains(newPassword));
        } else {
            return false;
        }
    }

}
