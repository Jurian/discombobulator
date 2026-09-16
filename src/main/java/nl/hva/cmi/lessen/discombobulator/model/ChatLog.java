package nl.hva.cmi.lessen.discombobulator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * An ordered sequence of {@link ChatMessage} instances belonging to the same
 * conversational session, identified by a unique {@code id}.
 *
 * <p>Logs marked {@code isInternal} are excluded from anonymized output.
 */
public class ChatLog {

    public final String id;
    public String name;
    public boolean isInternal;
    public final List<ChatMessage> messages = new ArrayList<>();

    public ChatLog(String id) {
        this.id = id;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
    }

    public int length() {
        return messages.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatLog other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
