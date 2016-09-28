package models.user;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.elevenpaths.latch.LatchResponse;

import models.Constants;
import models.exception.PasswordConstraintViolationException;
import models.factory.DarwinFactory;
import models.latch.LatchSDK;
import models.logger.Logger;

public class User {

    public static final String FIELD_DELIMITER = ",";

    public static boolean isEnabledLatchAlertMessage(String username) {
        User user = DarwinFactory.getInstance().loadUser(username);
        return user != null && user.isEnabledLatchAlertMessage();
    }

    public static boolean checkPasswordExpiration(String username) {
        User user = DarwinFactory.getInstance().loadUser(username);
        return user != null && user.checkPasswordExpiration();
    }

    protected UserImpl impl;

    public User(){}

    public User(UserImpl impl) {
        this.impl = impl;
    }

    public void save() {
        boolean created = this.impl.save();
        if (created) {
            logCreateAction();
        }
    }

    public void remove() {
        this.impl.remove();
        logRemoveAction();
    }

    public boolean isExistingUser() {
        return this.impl.isExistingUser();
    }

    public boolean authenticate(String password) {
        boolean authenticateOk = this.impl.authenticate(password);
        if (!authenticateOk) {
            if (!authenticationsAttemptsExceeded()) {
                increaseAuthenticationAttempts();
                if (authenticationsAttemptsExceeded()) {
                    lockLatch();
                }
                this.impl.save();
            }
        } else {
            if (getAuthenticationAttempts() > 0) {
                resetAuthenticationAttempts();
                this.impl.save();
            }
        }
        return  authenticateOk;
    }

    public boolean authenticationsAttemptsExceeded() {
        return getAuthenticationAttempts() >= Constants.User.MAX_AUTHENTICATION_ATTEMPTS;
    }

    public void lockLatch() {
        lockLatch(null);
    }

    public void lockLatch(String operationId) {
        String latchId = getLatchId();
        if (latchId != null && !latchId.isEmpty()) {
            LatchSDK latchSDK = LatchSDK.getLatchAPI();
            if (latchSDK != null) {
                LatchResponse response = operationId == null ? latchSDK.lock(latchId) : latchSDK.lock(latchId, operationId);
                if (response.getError() != null) {
                    play.Logger.error("LatchSDK lock: " + response.getError().getMessage());
                }
            }
        }
    }

    public void unlockLatch() {
        unlockLatch(null);
    }

    public void unlockLatch(String operationId) {
        String latchId = getLatchId();
        if (latchId != null && !latchId.isEmpty()) {
            LatchSDK latchSDK = LatchSDK.getLatchAPI();
            if (latchSDK != null) {
                LatchResponse response = operationId == null ? latchSDK.unlock(latchId) : latchSDK.unlock(latchId, operationId);
                if (response.getError() != null) {
                    play.Logger.error("LatchSDK unlock: " + response.getError().getMessage());
                }
            }
        }
    }

    public void changePassword(String password) throws PasswordConstraintViolationException {
        this.impl.changePassword(password);
        logPasswordChangeAction();
    }

    public void createDefaultAPIClient() {
        this.impl.createDefaultAPIClient();
    }

    public boolean isUserActivable() {
        return this.impl.isUserActivable();
    }

    public boolean isAdminUser() {
        return this.impl.isAdminUser();
    }

    public String getId() {
        return this.impl.getId();
    }

    public String getName() {
        return this.impl.getName();
    }

    public String getEmail() {
        return this.impl.getEmail();
    }

    public String getPassword() {
        return this.impl.getPassword();
    }

    public boolean isActive() {
        return this.impl.isActive();
    }

    public Date getPasswordChange() {
        return this.impl.getPasswordChange();
    }

    public int getAuthenticationAttempts() {
        return this.impl.getAuthenticationAttempts();
    }

    public void increaseAuthenticationAttempts() {
        this.impl.increaseAuthenticationAttempts();
    }

    public void resetAuthenticationAttempts() {
        this.impl.resetAuthenticationAttempts();
    }

    public Date getCreated() {
        return this.impl.getCreated();
    }

    public String getLatchId() {
        return this.impl.getLatchId();
    }

    public String getLatchOtp() {
        return this.impl.getLatchOtp();
    }

    public void setName(String name) {
        this.impl.setName(name);
    }

    public void setEmail(String email) {
        this.impl.setEmail(email);
    }

    public void setPassword(String password) {
        this.impl.setPassword(password);
        this.impl.setPasswordChange(new Date());
    }

    public void setActive(boolean active) {
        this.impl.setActive(active);
    }

    public void setPasswordChange(Date passwordChange) {
        this.impl.setPasswordChange(passwordChange);
    }

    public void setLatchId(String latchId) {
        this.impl.setLatchId(latchId);
    }

    public void setLatchOtp(String latchOtp) {
        this.impl.setLatchOtp(latchOtp);
        this.impl.resetLatchOtpAttempts();
    }

    public String getRoleId() {
        return this.impl.getRoleId();
    }

    public void setRoleId(String roleId) {
        this.impl.setRoleId(roleId);
    }

    public String getPreferredLang() {
        return this.impl.getPreferredLang();
    }

    public void setPreferredLang(String preferredLang) {
        this.impl.setPreferredLang(preferredLang);
    }

    public String getMobileConnectId() {
        return this.impl.getMobileConnectId();
    }

    public void setMobileConnectId(String mobileConnectId) {
        this.impl.setMobileConnectId(mobileConnectId);
    }

    public boolean isLatched(){
        return this.impl.isLatched();
    }
    public boolean isEnabledLatchAlertMessage() {
        return this.impl.isEnabledLatchAlertMessage();
    }

    public void setEnabledLatchAlertMessage(boolean enabledLatchAlertMessage) {
        this.impl.setEnabledLatchAlertMessage(enabledLatchAlertMessage);
    }

    public boolean checkPasswordExpiration() {

        boolean rv = false;

        if (Constants.User.PASSWORD_EXPIRATION_MILLIS != 0) {
            rv = (Constants.User.PASSWORD_EXPIRATION_MILLIS < (new Date().getTime() - this.impl.getPasswordChange().getTime()));
        }

        return rv;

    }

    public boolean checkPasswordUsedInThePast(String newPassword) {
        if (Constants.User.MAX_OLD_PASSWORDS_STORED > 0) {
            return this.impl.checkPasswordUsedInThePast(newPassword);
        } else {
            return false;
        }
    }

    public boolean isChangelogRead() {
        return this.impl.isChangelogRead();
    }

    public void setChangelogRead(boolean changelogRead) {
        this.impl.setChangelogRead(changelogRead);
    }

    public boolean isIgnoreChangelog() {
        return this.impl.isIgnoreChangelog();
    }

    public void setIgnoreChangelog(boolean ignoreChangelog) {
        this.impl.setIgnoreChangelog(ignoreChangelog);
    }

    public int getLatchOtpAttempts() {
        return this.impl.getLatchOtpAttempts();
    }

    public void setLatchOtpAttempts(int latchOtpAttempts) {
        this.impl.setLatchOtpAttempts(latchOtpAttempts);
    }

    public void resetLatchOtpAttempts() {
        this.impl.resetLatchOtpAttempts();
    }

    public void increaseLatchOtpAttempts() {
        this.impl.increaseLatchOtpAttempts();
    }

    public void logPasswordChangeAction() {
        Logger logger = DarwinFactory.getInstance().buildLogger();
        Map<String, String> params = new HashMap<String, String>();
        params.put("email", this.getEmail());
        logger.log(Constants.Logger.PASSWORD_CHANGE_ACTION, params);
    }

    public void logRemoveAction() {
        Logger logger = DarwinFactory.getInstance().buildLogger();
        Map<String, String> params = new HashMap<String, String>();
        params.put("email", this.getEmail());
        params.put("name", this.getName());
        logger.log(Constants.Logger.REMOVE_USER_ACTION, params);
    }

    public void logCreateAction() {
        Logger logger = DarwinFactory.getInstance().buildLogger();
        HashMap<String, String> params  = new HashMap<String, String>();
        params.put("email", this.getEmail());
        params.put("name", this.getName());
        logger.log(Constants.Logger.CREATE_USER_ACTION, params);
    }

    public UserImpl getImplementation(){
        return this.impl;
    }

}
