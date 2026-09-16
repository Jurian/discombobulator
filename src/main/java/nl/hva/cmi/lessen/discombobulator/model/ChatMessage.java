package nl.hva.cmi.lessen.discombobulator.model;

import java.time.LocalDateTime;

/**
 * A single message within a chat log, with its timestamp, sender, direction,
 * and text content.
 *
 * <p>{@code content} is mutable: it starts empty and is written by the parser,
 * then updated in-place by the anonymization pipeline.
 */
public class ChatMessage {

    public enum CHAT_TYPE {
        FROM,
        TO
    }

    public final LocalDateTime datetime;
    public final User user;
    /** Direction of the message. {@code null} for group chats where TO/FROM does not apply. */
    public final CHAT_TYPE type;
    public String content = "";

    public ChatMessage(LocalDateTime datetime, User user, CHAT_TYPE type) {
        this.datetime = datetime;
        this.user = user;
        this.type = type;
    }
}
