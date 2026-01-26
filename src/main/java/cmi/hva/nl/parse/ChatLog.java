package cmi.hva.nl.parse;

import java.util.ArrayList;
import java.util.List;


/**
 * {@code ChatLog} represents a sequence of chat messages that belong to the
 * same conversational session or interaction.
 *
 * <p>
 * This class is a structural data model used during parsing and preprocessing
 * of chat datasets. It does not perform anonymization or pseudonymization by
 * itself, but it may contain personal data in the form of message content and
 * user references. Privacy-related transformations are handled in higher-level
 * packages such as {@code cmi.hva.nl.anon}.
 * </p>
 *
 * <p>
 * Each {@code ChatLog} is identified by a unique identifier and maintains an
 * ordered list of {@link ChatMessage} instances representing the conversation
 * flow.
 * </p>
 */
public class ChatLog {

    /**
     * Unique identifier of this chat log.
     */
    public final String id;

    /**
     * Indicates whether this log is internal and should typically be excluded
     * from downstream processing or anonymization pipelines.
     */
    public boolean isInternal;

    /**
     * Ordered list of messages belonging to this chat log.
     */
    public final List<ChatMessage> messages = new ArrayList<>();

    /**
     * Creates a new {@code ChatLog} with the given identifier.
     *
     * @param id unique identifier for the chat log
     */
    public ChatLog(String id) {
        this.id = id;
    }

    /**
     * Adds a message to this chat log.
     *
     * <p>
     * Messages are appended in the order in which this method is called.
     * </p>
     *
     * @param message the message to add
     */
    public void addMessage(ChatMessage message) {
        messages.add(message);
    }

    /**
     * Returns the number of messages in this chat log.
     *
     * @return the number of messages
     */
    public int length() {
        return messages.size();
    }

    /**
     * Computes a hash code based solely on the chat log identifier.
     *
     * @return the hash code of this chat log
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}