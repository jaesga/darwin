package controllers;

import notifiers.ContactSubject;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import models.Config;
import models.exception.DarwinErrorException;
import models.exception.PasswordConstraintViolationException;
import models.factory.DarwinFactory;
import models.token.Token;
import models.token.TokenTypeBase;
import models.user.ActivationType;
import models.user.User;
import models.validation.Password;
import net.sf.oval.constraint.Email;
import notifiers.DarwinMailer;
import play.Logger;
import play.data.validation.Required;
import play.exceptions.MailException;
import play.i18n.Messages;


public class PublicContentBase extends WebController {

    public static void register(String token, String email){
        token = Security.cleanInput(token);
        email = Security.cleanInput(email);
        render(token, email);
    }

    public static void processRegister(@Required(message = "Public.register.validation.name") String name,
                                       @Required(message = "Public.register.validation.email")
                                       @Email(message = "Public.register.validation.invalidFormat") String email,
                                       @Password @Required(message = "Public.register.validation.password") String password,
                                       @Required(message = "Public.register.validation.passwordConfirmation") String passwordCheck,
                                       String token) {

        checkAuthenticity();

        ActivationType activationType = Config.getUserActivationType();
        if (ActivationType.INVITATION.equals(activationType) && !Config.isAutoActivatedUser(email)) {
            validation.required(token);
        }

        email = email.trim().toLowerCase();
        validation.equals(password, passwordCheck).message("Public.register.validation.passwordMismatch");
        name = Security.cleanInput(name);
        validation.required(name).message("Public.register.validation.invalidOrEmpty");
        if(validation.hasErrors()) {
            params.flash();
            validation.keep();
            register(token, email);
        }

        User user = DarwinFactory.getInstance().buildUser(name, email, password);
        boolean existingUser = user.isExistingUser();
        validation.isTrue(!existingUser).message("Public.register.validation.existingAccount").key("name");
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            register(token, email);
        } else {
            if (user.isUserActivable() || !ActivationType.INVITATION.equals(activationType)) {
                if (ActivationType.NONE.equals(activationType)){
                    user.setActive(true);
                }
                user.save();
                user.createDefaultAPIClient();
            }
            processActivateUser(user, token);
        }

        flash.put("successfulRegister", true);
        redirect("Secure.login");
    }

    protected static void processActivateUser(User user, String token) {
        if (!user.isActive()) {
            ActivationType activationType = Config.getUserActivationType();
            switch (activationType){
                case ADMIN:
                    adminActivationUser();
                    break;
                case TOKEN:
                    tokenActivationUser(user);
                    break;
                case INVITATION:
                    invitationActivationUser(user, token);
                    break;
                case NONE:
                    redirect("Secure.login");
                    break;
            }
        } else {
            redirect("Secure.login");
        }
    }

    private static void adminActivationUser() {
        flash.put("adminActivationUser", true);
        redirect("Secure.login");
    }

    private static void tokenActivationUser(User user) {
        Token token = DarwinFactory.getInstance().buildToken(user.getEmail(), TokenTypeBase.ACTIVATE_ACCOUNT);
        token.save();
        DarwinMailer.activateAccount(user, token.getToken());

        redirect("PublicContentBase.activate");
    }

    private static void invitationActivationUser(User user, String token) {
        try {
            Token invitationToken = DarwinFactory.getInstance().loadToken(token, TokenTypeBase.INVITE_USER);
            if (invitationToken != null && invitationToken.getEmail().equals(user.getEmail()) && invitationToken.isValid()) {
                user.setActive(true);
                user.save();
                user.createDefaultAPIClient();
                invitationToken.remove();
            } else {
                register(token, user.getEmail());
            }
        } catch (DarwinErrorException e) {
            Logger.error(e.getMessage());
        }

        redirect("Secure.login");
    }

    public static void activate(String token) {
        if (token != null && !token.isEmpty()) {
            try {
                User user = DarwinFactory.getInstance().loadUserByActivationToken(token);
                if (user != null && !user.isActive()) {
                    user.setActive(true);
                    user.save();
                    user.createDefaultAPIClient();
                    Token activateToken = DarwinFactory.getInstance().loadToken(token, TokenTypeBase.ACTIVATE_ACCOUNT);
                    activateToken.remove();
                    flash.put("successActiveAccount", true);
                } else {
                    flash.put("invalidActivationToken", true);
                }
            } catch (DarwinErrorException e) {
                Logger.error(e.getMessage());
                flash.put("invalidActivationToken", true);
                render();
            }
        }

        render();
    }

    public static void requestPasswordReset() {
        if (session.get("username") != null && !session.get("username").isEmpty()) {
            passwordReset(null);
        }
        render();
    }

    public static void processRequestPasswordReset(@Required(message = "Public.processRequestPasswordReset.validation.enterAccount")
                                                   @Email(message = "Public.processRequestPasswordReset.validation.invalidFormat") String email) {

        checkAuthenticity();
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            requestPasswordReset();
        }

        doProcessRequestPasswordReset(email);
    }

    protected static void doProcessRequestPasswordReset(String email){
        try {
            User user = DarwinFactory.getInstance().loadUser(email);
            if (user != null) {
                Token token = DarwinFactory.getInstance().buildToken(email, TokenTypeBase.RESET_PASSWORD);
                token.save();
                DarwinMailer.passwordReset(user, token.getToken());
            }
        } catch (MailException e) {
            Logger.info("Error sending email notification");
        }

        renderArgs.put("email", (StringUtils.isEmpty(email)) ? Messages.get("Public.activate.yourEmail") : StringEscapeUtils.escapeHtml(email));
        render("PublicContentBase/processRequestPasswordReset.html");
    }

    public static void passwordReset(String token) {
        if (token != null && !token.isEmpty()) {
            token = Security.cleanInput(token);
            render(token);
        } else {
            getCurrentUser();
            render();
        }
    }

    public static void processPasswordReset(@Required(message = "Public.passwordReset.validation.enterEmailToken") String token,
                                            @Password @Required(message = "Public.register.validation.password") String password,
                                            @Required(message = "Public.register.validation.passwordConfirmation") String passwordCheck) {

        checkAuthenticity();
        validation.equals(password, passwordCheck).message("Public.register.validation.passwordMismatch");
        if (validation.hasErrors()) {
            validation.keep();
            passwordReset(token);
        }
        try {
            User user  = DarwinFactory.getInstance().loadUserByResetToken(token);
            String flashKey = "expiredToken";
            if (user != null) {
                if (!user.checkPasswordUsedInThePast(password)) {
                    try {
                        user.changePassword(password);
                        Token resetPasswordToken = DarwinFactory.getInstance().loadToken(token, TokenTypeBase.RESET_PASSWORD);
                        resetPasswordToken.remove();
                        flash.put("success", Messages.get("Public.passwordReset.ok"));
                        redirect("Secure.login");
                    } catch (PasswordConstraintViolationException e) {
                        Logger.info("Unable to change user's password (" + e.getMessage() + ")");
                    }
                }
                flashKey = "passwordUsedInThePast";
            }
            flash.put(flashKey, true);
            passwordReset(token);
        } catch (DarwinErrorException e) {
            Logger.error(e.getMessage());
            flash.put("expiredToken", true);
            passwordReset(token);
        }
    }

    public static void processPasswordResetUserAuth(@Required(message = "Public.register.validation.password") String oldPassword,
                                                    @Password @Required(message = "Public.register.validation.password") String password,
                                                    @Required(message = "Public.register.validation.passwordConfirmation") String passwordCheck) {
        checkAuthenticity();
        boolean userAuthenticated = session.contains("username") && !session.get("username").isEmpty();
        validation.equals(password, passwordCheck).message("Public.register.validation.passwordMismatch");
        if (validation.hasErrors() || !userAuthenticated) {
            validation.keep();
            passwordReset(null);
        }

        User user  = getCurrentUser();
        String flashKey = "invalidOldPassword";
        if (user != null && user.authenticate(oldPassword)) {
            if (!user.checkPasswordUsedInThePast(password)) {
                try {
                    user.changePassword(password);
                    flash.put("message", Messages.get("Public.passwordReset.ok"));
                    redirect("Profile.index");
                } catch (PasswordConstraintViolationException e) {
                    Logger.info("Unable to change user's password (" + e.getMessage() + ")");
                }
            }
            flashKey = "passwordUsedInThePast";
        }
        flash.put(flashKey, true);
        passwordReset(null);
    }

    public static void contact(){
        render();
    }

    public static void contactProcess(@Required(message = "Contact.errors.invalid.name") String name, @Required(message = "Contact.errors.invalid.email") String email, Integer subject, @Required(message = "Contact.errors.invalid.message") String message) {
        checkAuthenticity();

        ContactSubject subj = ContactSubject.valueOf(subject);
        if (!ContactSubject.getSubjects().isEmpty()) {
            validation.isTrue(subj != null).message("Contact.errors.invalid.subject").key("subject");
        }
        if(validation.hasErrors()){
            validation.keep();
            params.flash();
        } else {
            DarwinMailer.contactUs(name, email, message, subj);
            flash.put("contactUsSend", true);
            flash.keep();
        }
        contact();
    }
}
