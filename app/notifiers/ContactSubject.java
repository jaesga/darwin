package notifiers;

import play.Play;
import play.i18n.Messages;

import java.util.ArrayList;
import java.util.List;

public class ContactSubject {

    private static List<ContactSubject> subjects;

    private static String DEFAULT_INBOX = Play.configuration.getProperty("Contact.subject.inbox");
    private static String subjectInboxFormat = "Contact.subject.%d.inbox";
    private static String subjectMessageFormat = "Contact.subject.%d.message";

    static {
        subjects = new ArrayList<ContactSubject>();
        String inbox, message;
        int id = 1;
        message = loadSubjectFromConfiguration(id);
        while (message != null) {
            inbox = loadInboxFromConfiguration(id);
            subjects.add(new ContactSubject(id, inbox, message));
            message = Play.configuration.getProperty(String.format(subjectMessageFormat, ++id));
        }
    }

    public static String loadSubjectFromConfiguration(int id) {
        return Play.configuration.getProperty(String.format(subjectMessageFormat, id));
    }

    public static String loadInboxFromConfiguration(int id) {
        return Play.configuration.getProperty(String.format(subjectInboxFormat, id), DEFAULT_INBOX);
    }

    public static List<ContactSubject> getSubjects() {
        return subjects;
    }

    public static ContactSubject valueOf(Integer id) {
        if (id != null) {
            String inbox, message;
            inbox = Play.configuration.getProperty(String.format(subjectInboxFormat, id));
            message = Play.configuration.getProperty(String.format(subjectInboxFormat, id));
            if (message != null) {
                return new ContactSubject(id, inbox, message);
            }
        }
        return null;
    }

    protected int id;
    protected String inbox;
    protected String message;

    public ContactSubject(int id, String inbox, String message) {
        this.id = id;
        this.inbox = inbox;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public String getInbox() {
        return inbox;
    }

    public String getMessage() {
        return Messages.get(message);
    }
}