package notifiers;

import org.apache.commons.lang.StringUtils;

import controllers.DarwinHooks;
import models.Config;
import models.notifiers.TranslatedTemplatePathResolver;
import models.ticket.Ticket;
import models.ticket.TicketBuilder;
import models.ticket.ZendeskApi;
import models.user.User;
import play.Play;
import play.i18n.Lang;
import play.i18n.Messages;
import play.mvc.Mailer;
import play.mvc.Scope;

public class DarwinMailer extends Mailer {

    /*
     * Subjects are taken from the internationalization file using the following keys.
     */
    protected static final String SUBJECT_PASSWORD_RESET = "Mailer.subject.passwordReset";
    protected static final String SUBJECT_ACTIVATION = "Mailer.subject.activation";
    protected static final String SUBJECT_INVITE = "Mailer.subject.invitation";
    protected static final String SUBJECT_CONTACT = "Mailer.subject.contactUs";

    protected static final String UTF_8 = "utf-8";
    protected static final String DEFAULT_EMAIL_SENDER = Play.configuration.getProperty("mailer.email.sender", "user@example.com");
    protected static final String CONTACT_EMAIL = Play.configuration.getProperty("mailer.contactUs.recipient", "");
    protected static final String DEFAULT_NAME_SENDER = Play.configuration.getProperty("mailer.name.sender", "Darwin");

    protected static final String RENDER_ARGS_PARAMETER_FORMAT = "render.mailer.%s";
    protected static final String EMAIL_SENDER_RENDER_ARGS = "email.sender";
    protected static final String NAME_SENDER_RENDER_ARGS = "name.sender";
    protected static final String BASEURL_RENDER_ARGS = "baseUrl";


    public static void passwordReset(User user, String token) {
        String email = user.getEmail();
        configureEmailSettings(email, SUBJECT_PASSWORD_RESET);
        String baseUrl = getBaseUrl();
        String appName = Config.getApplicationName();
        Scope.RenderArgs renderArgs = Scope.RenderArgs.current();
        String userLang = user.getPreferredLang();
        send(getTemplatePath("passwordReset", userLang), userLang, renderArgs, appName, user, token, baseUrl);
    }

    public static void activateAccount(User user, String token) {
        String email = user.getEmail();
        configureEmailSettings(email, SUBJECT_ACTIVATION);
        String baseUrl = getBaseUrl();
        String appName = Config.getApplicationName();
        Scope.RenderArgs renderArgs = Scope.RenderArgs.current();
        String userLang = user.getPreferredLang();
        send(getTemplatePath("activateAccount", userLang), userLang, renderArgs, appName, user, token, baseUrl);
    }

    public static void adminActivateAccount(User user) {
        Lang.set(user.getPreferredLang());
        String email = user.getEmail();
        configureEmailSettings(email, SUBJECT_ACTIVATION);
        String baseUrl = getBaseUrl();
        String appName = Config.getApplicationName();
        Scope.RenderArgs renderArgs = Scope.RenderArgs.current();
        String userLang = user.getPreferredLang();
        send(getTemplatePath("adminActivateAccount", userLang), userLang, renderArgs, appName, user, baseUrl);
    }

    public static void inviteAccount(String email, String token, String userLang) {
        configureEmailSettings(email, SUBJECT_INVITE);
        String baseUrl = getBaseUrl();
        String appName = Config.getApplicationName();
        Scope.RenderArgs renderArgs = Scope.RenderArgs.current();
        send(getTemplatePath("inviteAccount", userLang), userLang, renderArgs, appName, email, token, baseUrl);
    }

    public static void contactUs(String name, String email, String message) {
        ContactSubject subject = new ContactSubject(0, CONTACT_EMAIL, Messages.get(SUBJECT_CONTACT));
        contactUs(name, email, message, subject);
    }

    public static void contactUs(String name, String email, String message, ContactSubject subject) {
        if (subject == null) {
            subject = new ContactSubject(0, CONTACT_EMAIL, Messages.get(SUBJECT_CONTACT));
        }
        if (Config.isZendeskEnabled()) {
            Ticket ticket = new TicketBuilder()
                    .setUsername(name)
                    .setEmail(email)
                    .setSubject(subject.getMessage())
                    .addTag(Ticket.Tag.CONTACT_US.getValue())
                    .setMessage(message)
                    .createTicket();
            ZendeskApi.sendTicket(ticket);
        } else {
            String from = name + "<" + email + ">";
            configureEmailSettings(from, subject.getInbox(), subject.getMessage());
            String userLang = "en";
            send(getTemplatePath("contact.html", userLang), userLang, name, email, message);
        }

    }

    protected static String getBaseUrl() {

        String rv = getMailerParameterFromRenderArgs(BASEURL_RENDER_ARGS);

        if (rv == null) {
            rv = (String) DarwinHooks.AppHooks.invoke("onUrlBase");
        }

        if (rv == null) {
            rv = Play.configuration.getProperty("application.baseUrl");
        }

        return rv;

    }

    protected static void configureEmailSettings(String email, String subject) {
        String from = getNameSender() + "<" + getEmailSender() + ">";
        configureEmailSettings(from, email, subject);
    }

    protected static void configureEmailSettings(String from, String to, String subject) {
        setCharset(UTF_8);
        setFrom(from);
        setSubject(Messages.get(subject));
        addRecipient(to);
    }

    protected static String getEmailSender() {
        String emailSender = getEmailSenderFromRenderArgs();
        if (emailSender == null) {
            emailSender = getEmailSenderFromConfiguration();
        }
        return emailSender;
    }

    protected static String getEmailSenderFromConfiguration() {
        String sender = Play.configuration.getProperty("mailer.email.sender");
        return StringUtils.isBlank(sender) ? DEFAULT_EMAIL_SENDER : sender;
    }

    protected static String getEmailSenderFromRenderArgs() {
        return getMailerParameterFromRenderArgs(EMAIL_SENDER_RENDER_ARGS);
    }

    public static void setEmailSenderToRenderArgs(String email) {
        setMailerParameterToRenderArgs(EMAIL_SENDER_RENDER_ARGS, email);
    }

    protected static String getNameSender() {
        String nameSender = getNameSenderFromRenderArgs();
        if (nameSender == null) {
            nameSender = getNameSenderFromConfiguration();
        }
        return nameSender;
    }

    protected static String getNameSenderFromConfiguration() {
        String sender = Play.configuration.getProperty("mailer.name.sender");
        return StringUtils.isBlank(sender) ? DEFAULT_NAME_SENDER : sender;
    }

    protected static String getNameSenderFromRenderArgs() {
        return getMailerParameterFromRenderArgs(NAME_SENDER_RENDER_ARGS);
    }

    public static void setNameSenderToRenderArgs(String name) {
        setMailerParameterToRenderArgs(NAME_SENDER_RENDER_ARGS, name);
    }

    protected static String getMailerParameterFromRenderArgs(String key) {
        Scope.RenderArgs renderArgs = Scope.RenderArgs.current();
        if (renderArgs != null) {
            String parameter = renderArgs.get(String.format(RENDER_ARGS_PARAMETER_FORMAT, key), String.class);
            if (StringUtils.isNotEmpty(parameter)) {
                return parameter;
            }
        }
        return null;
    }

    protected static String setMailerParameterToRenderArgs(String key, String value) {
        Scope.RenderArgs renderArgs = Scope.RenderArgs.current();
        if (renderArgs != null && StringUtils.isNotEmpty(value)) {
            renderArgs.put(String.format(RENDER_ARGS_PARAMETER_FORMAT, key), value);
        }
        return null;
    }

    protected static String getTemplatePath(String template, String lang) {
        TranslatedTemplatePathResolver pathResolver = new TranslatedTemplatePathResolver(lang);
        return pathResolver.getEmailContent(template);
    }

}
