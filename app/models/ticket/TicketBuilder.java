package models.ticket;

import java.util.Set;
import java.util.TreeSet;

public class TicketBuilder {

    private String username;
    private String email;
    private String subject;
    private String message;
    private Set<String> tags;

    public TicketBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    public TicketBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public TicketBuilder setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public TicketBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public TicketBuilder setTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public TicketBuilder addTag(String tag) {
        if (this.tags == null) {
            this.tags = new TreeSet<String>();
        }
        this.tags.add(tag);
        return this;
    }

    public Ticket createTicket() {
        return new Ticket(username, email, subject, message, tags);
    }
}