package cmi.hva.nl.parse;

import java.time.LocalDateTime;


/**
 * {@code ChatMessage} represents a single message within a chat log.
 *
 * <p>
 * This class is part of the parsing and data-model layer and captures the
 * structural properties of a message, such as timestamp, sender/recipient,
 * and textual content. It does not perform any privacy-related transformations
 * itself, but its {@link #content} field may contain personal data that is
 * processed by higher-level anonymization components.
 * </p>
 *
 * <p>
 * A message is associated with a {@link User} and has a direction
 * ({@link CHAT_TYPE#FROM} or {@link CHAT_TYPE#TO}) indicating whether the
 * message was sent by or addressed to that user.
 * </p>
 */
public class ChatMessage {

    /**
     * Indicates the direction of the chat message relative to the user.
     */
    public enum CHAT_TYPE {
        /** Message sent by the user. */
        FROM,
        /** Message sent to the user. */
        TO;
    }

    /**
     * Marker line used by some log formats to indicate the end of a message block.
     */
    static final String MESSAGE_END = "----------------------------------------------------";

    /**
     * Line index containing the log identifier in the parsed input format.
     */
    static final int LINE_LOG_ID = 0;

    /**
     * Line index containing metadata in the parsed input format.
     */
    static final int LINE_METADATA = 1;

    /**
     * Timestamp associated with this message.
     */
    public final LocalDateTime datetime;

    /**
     * Textual content of the message.
     *
     * <p>
     * Content is accumulated during parsing and may later be normalized or
     * pseudonymized by anonymization rules.
     * </p>
     */
    public String content = "";

    /**
     * User associated with this message.
     */
    public final User user;

    /**
     * Direction of the message relative to the associated user.
     */
    public final CHAT_TYPE type;

    /**
     * Appends a line of text to the message content.
     *
     * <p>
     * A single space character is added after the line to preserve word
     * separation when multiple lines are concatenated.
     * </p>
     *
     * @param line the line of text to append
     */
    public void addContent(String line) {
        this.content += line + " ";
    }

    /**
     * Creates a new {@code ChatMessage}.
     *
     * @param datetime     timestamp of the message
     * @param user         user associated with the message
     * @param isMessageTo  {@code true} if the message direction is {@link CHAT_TYPE#TO},
     *                     {@code false} if it is {@link CHAT_TYPE#FROM}
     */
    public ChatMessage(LocalDateTime datetime, User user, boolean isMessageTo) {
        this.datetime = datetime;
        this.user = user;
        this.type = isMessageTo ? CHAT_TYPE.TO : CHAT_TYPE.FROM;
    }
}

