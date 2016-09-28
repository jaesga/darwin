package models.ticket;


import java.util.HashSet;
import java.util.Set;

public class Ticket {

    public enum Tag {

        CONTACT_US("Contact");

        private String value;

        Tag(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    protected String username;
    protected String email;
    protected String subject;
    protected String message;
    protected Set<String> tags;

    Ticket(String username, String email, String subject, String message, Set<String> tags) {
        this.username = username;
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.tags = tags;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        if (tags == null) {
            tags = new HashSet<String>();
        }
        tags.add(tag);
    }
}
