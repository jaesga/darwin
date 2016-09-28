package models.user;

import java.util.Date;
import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import controllers.WebController;
import models.Constants;
import models.exception.PasswordConstraintViolationException;
import models.utils.AuthUtils;

public abstract class UserImpl {

    protected String name;
    protected String email;
    protected String password;
    protected boolean active;
    protected Date passwordChange;
    protected int authenticationAttempts;
    protected Date created;
    protected String preferredLang;
    protected Queue<String> oldPasswords;
    protected boolean changelogRead;
    protected boolean ignoreChangelog;

    protected String latchId;
    protected String latchOtp;
    protected int latchOtpAttempts;
    protected boolean enabledLatchAlertMessage;
    protected String roleId;

    protected String mobileConnectId;

    protected UserImpl() {}

    protected UserImpl(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = AuthUtils.hashPassword(password, email);
        this.created = new Date();
        this.passwordChange = new Date();
        this.authenticationAttempts = 0;
        this.active = isUserActivable();
        this.preferredLang = WebController.getLanguage();
        this.roleId = isAdminUser() ? Constants.UserRole.SUPER_ADMIN : Constants.UserRole.DEFAULT_ROLE;
        this.enabledLatchAlertMessage = true;
        if (Constants.User.MAX_OLD_PASSWORDS_STORED > 1) {
            this.oldPasswords = new CircularFifoQueue<String>(Constants.User.MAX_OLD_PASSWORDS_STORED - 1);
        } else {
            this.oldPasswords = new CircularFifoQueue<String>();
        }
        this.changelogRead = false;
        this.ignoreChangelog = false;
        this.latchOtpAttempts = 0;
    }

    public abstract boolean save();
    public abstract void remove();
    public abstract boolean isExistingUser();
    public abstract boolean authenticate(String password);
    public abstract void changePassword(String password) throws PasswordConstraintViolationException;
    public abstract void createDefaultAPIClient();
    public abstract boolean isUserActivable();
    public abstract boolean isAdminUser();
    public abstract String getId();
    public abstract boolean checkPasswordUsedInThePast(String newPassword);

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isLatched(){
        return latchId != null && !latchId.isEmpty();
    }

    public Date getPasswordChange() {
        return passwordChange;
    }

    public Date getCreated() {
        return created;
    }

    public String getLatchId() {
        return latchId;
    }

    public String getLatchOtp() {
        return latchOtp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
        setPasswordChange(new Date());
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setPasswordChange(Date passwordChange) {
        this.passwordChange = passwordChange;
    }

    public int getAuthenticationAttempts() {
        return authenticationAttempts;
    }

    public void increaseAuthenticationAttempts() {
        this.authenticationAttempts += 1;
    }

    public void resetAuthenticationAttempts() {
        this.authenticationAttempts = 0;
    }

    public void setLatchId(String latchId) {
        this.latchId = latchId;
    }

    public void setLatchOtp(String latchOtp) {
        this.latchOtp = latchOtp;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getPreferredLang() {
        return preferredLang;
    }

    public void setPreferredLang(String preferredLang) {
        this.preferredLang = preferredLang;
    }

    public String getMobileConnectId() {
        return mobileConnectId;
    }

    public void setMobileConnectId(String mobileConnectId) {
        this.mobileConnectId = mobileConnectId;
    }

    public boolean isEnabledLatchAlertMessage() {
        return enabledLatchAlertMessage && (latchId == null || latchId.isEmpty());
    }

    public void setEnabledLatchAlertMessage(boolean enabledLatchAlertMessage) {
        this.enabledLatchAlertMessage = enabledLatchAlertMessage;
    }

    public Queue<String> getOldPasswords() {
        return oldPasswords;
    }

    public boolean isChangelogRead() {
        return changelogRead;
    }

    public void setChangelogRead(boolean changelogRead) {
        this.changelogRead = changelogRead;
    }

    public boolean isIgnoreChangelog() {
        return ignoreChangelog;
    }

    public void setIgnoreChangelog(boolean ignoreChangelog) {
        this.ignoreChangelog = ignoreChangelog;
    }

    public int getLatchOtpAttempts() {
        return latchOtpAttempts;
    }

    public void setLatchOtpAttempts(int latchOtpAttempts) {
        this.latchOtpAttempts = latchOtpAttempts;
    }

    public void resetLatchOtpAttempts() {
        this.latchOtpAttempts = 0;
    }

    public void increaseLatchOtpAttempts() {
        this.latchOtpAttempts += 1;
    }
}
