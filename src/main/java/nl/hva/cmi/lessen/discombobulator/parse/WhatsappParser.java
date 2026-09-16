package nl.hva.cmi.lessen.discombobulator.parse;

import nl.hva.cmi.lessen.discombobulator.config.InputConfig;
import nl.hva.cmi.lessen.discombobulator.model.ChatDataset;
import nl.hva.cmi.lessen.discombobulator.model.ChatLog;
import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * WhatsappParser reads a text-based chat export and converts it into structured
 * {@link ChatLog}, {@link ChatMessage}, and {@link User} objects.
 *
 * <p>The parser processes a log file line-by-line and reconstructs:
 * <ul>
 *   <li>Chat sessions (identified by a log ID)</li>
 *   <li>Message metadata such as timestamp, direction (“to” / “from”), and sender</li>
 *   <li>Message content spread across multiple lines</li>
 *   <li>User identities derived from names, phone numbers, or notifications</li>
 * </ul>
 *
 * <p>The parser maintains two internal maps:
 * <ul>
 *   <li>{@code users} — all known users, keyed by their resolved userId
 *       (name or phone number). Users are merged intelligently when only
 *       partial information (e.g., phone number only) was known earlier.</li>
 *   <li>{@code logs} — all chat logs, keyed by their log ID</li>
 * </ul>
 *
 * <p>A chat entry in the file is structured as:
 * <ol>
 *   <li>Log ID</li>
 *   <li>Metadata (timestamp + sender line)</li>
 *   <li>One or more message content lines</li>
 *   <li>A terminating {@code ChatMessage.MESSAGE_END} marker</li>
 * </ol>
 *
 * <p>User resolution follows these rules:
 * <ul>
 *   <li>If a userId is unknown, the parser checks for an earlier user identified
 *       only by phone number and merges them.</li>
 *   <li>A new user is created otherwise.</li>
 *   <li>User fields such as name and phone number are only filled once and never overwritten.</li>
 *   <li>A user is marked as a customer unless the chat log ID matches a known
 *       internal employee group.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * LogParser parser = new LogParser();
 * parser.parse("chatlog.txt");
 *
 * // Access results:
 * Map<String, ChatLog> logs = parser.logs;
 * Map<String, User> users = parser.users;
 * }</pre>
 *
 * <p>After parsing:
 * <ul>
 *   <li>{@code logs} contains every conversation with full message history.</li>
 *   <li>{@code users} contains every resolved participant.</li>
 * </ul>
 *
 * <p>This class performs no validation or error recovery beyond basic line parsing;
 * malformed input may produce incomplete logs or users.
 */
public class WhatsappParser implements Parser<ChatDataset> {

    /**
     * Formatter used to parse timestamp strings found in metadata lines.
     * Expected format: {@code yyyy-MM-dd HH:mm:ss}.
     */
    private static final String MESSAGE_END =
            "----------------------------------------------------";
    private static final int LINE_LOG_ID   = 0;
    private static final int LINE_METADATA = 1;

    private static final String USER_NOTIFICATION = "notification";
    private static final String ROLE_CUSTOMER  = "customer";
    private static final String ROLE_EMPLOYEE  = "employee";
    private static final String ROLE_SYSTEM    = "system";

    final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Pattern NORMALIZE_PATTERN = Pattern.compile("\\s+");

    private final Set<String> internalGroupChats;

    public WhatsappParser(Set<String> groupChats) {
        this.internalGroupChats = groupChats;
    }

    public static ChatDataset fromConfig(InputConfig config) throws IOException {
        Set<String> groupIds = Set.of();
        if (config.groupsFile != null) {
            groupIds = new TextFileParser<>(line -> line).parse(config.groupsFile);
        }
        return new WhatsappParser(groupIds).parse(config.file);
    }

    public ChatDataset parse(String fileName) throws IOException {

        /*
         * Map of all known users encountered in the log file.
         * <p>
         * Keys are the resolved canonical user IDs (name or phone number),
         * and values are the corresponding {@link User} objects. Users may
         * be merged when additional identifying information is discovered.
         */
        final Map<String, User> users = new HashMap<>();

        /*
         * Map of all chat logs parsed from the input file.
         * <p>
         * Keys are normalized log IDs, and values are {@link ChatLog} objects
         * representing complete conversations grouped under those IDs.
         */
        final Map<String, ChatLog> logs = new HashMap<>();

        String line;

        LocalDateTime datetime = null;
        ChatLog log = null;
        ChatMessage message = null;
        boolean skipMessage = false;

        int lineCounter = 0;  // Tracks position inside the current message block

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {

            while ((line = reader.readLine()) != null) {

                line = NORMALIZE_PATTERN.matcher(line).replaceAll(" ").trim();

                // End marker: finalize the current message (if any)
                if(line.equals(MESSAGE_END)) {

                    if(log != null && message != null && !skipMessage) {
                        log.addMessage(message);   // Add completed message to log
                    }

                    // Reset for next block
                    lineCounter = 0;
                    log = null;
                    message = null;
                    skipMessage = false;
                    continue;
                };

                // -------------------------------
                // Line 0: ChatLog ID
                // -------------------------------
                if(lineCounter == LINE_LOG_ID) {

                    if(line.charAt(0) == '+') {
                        line = line.replace(" ", "");          // Normalize phone-number log IDs
                    } else {
                        line = line.trim();  // Collapse whitespace for normal IDs
                    }

                    log = logs.computeIfAbsent(line, ChatLog::new);  // Get or create chat log by ID
                }

                // -------------------------------
                // Line 1: Metadata (timestamp + sender)
                // -------------------------------
                else if(lineCounter == LINE_METADATA) {

                    datetime = LocalDateTime.parse(line.substring(0, 19), formatter);  // Parse timestamp
                    line = line.substring(20);                                // Remove timestamp text

                    String userId = null;
                    String name = null , phoneNumber = null;
                    boolean messageTo;   // Direction: TO = outgoing, FROM = incoming

                    boolean isSystemMessage = false;

                    // Special case: system-generated notification
                    if(line.equals(USER_NOTIFICATION)) {
                        userId = USER_NOTIFICATION;
                        messageTo = false;
                        isSystemMessage = true;
                    } else {

                        // Strip trailing status (e.g. " - Read")
                        int dashIndex = line.indexOf(" - ");
                        if (dashIndex >= 0) {
                            line = line.substring(0, dashIndex).trim();
                        }

                        // Determine direction: "to" or "from"
                        if(line.startsWith("to")) {
                            messageTo = true;
                            line = line.substring(3); // Skip "to "
                        } else {
                            messageTo = false;
                            line = line.substring(5); // Skip "from "
                        }

                        // Special case: group chat wide notification
                        if(internalGroupChats.contains(line)) {
                            log.isInternal = true;
                        }

                        // Extract phone number or name (with optional phone in parentheses)
                        if(line.charAt(0) == '+'){ // We found a phone number instead of a name
                            phoneNumber = line.replace(" ", "");  // Normalize phone number
                            userId = phoneNumber;
                        } else {
                            // There is a name and optionally a phone number

                            int i = line.indexOf('('); // Search for occurrence of phone number after name

                            if(i == -1)  { // Name only
                                name = line.trim();
                            } else { // Name with phone number
                                name = line.substring(0, i).trim();  // Name before parentheses

                                if(line.charAt(i+1) == '+') {
                                    phoneNumber = line.substring(i+1, i+13).trim();  // Extract phone
                                }
                            }

                            // Normalize userId (names may have double spaces)
                            userId = name.trim();
                        }
                    }

                    // Retrieve known user or merge with phone number entry
                    User user = users.get(userId); // Find if user already exists by name or phone number

                    if (user == null) { // User not found

                        // User could have been added under phone number while currently userId is a name
                        // Try to resolve user created earlier under their phone number
                        user = users.remove(phoneNumber);

                        if (user == null) {
                            // Create a new user entry
                            user = new User(userId);
                            user.role = isSystemMessage             ? ROLE_SYSTEM
                                      : internalGroupChats.contains(log.id) ? ROLE_EMPLOYEE
                                      : ROLE_CUSTOMER;
                        }

                        // First appearance of this userId: fill all fields
                        user.name = name;
                        user.phoneNumber = phoneNumber;

                        users.put(userId, user);   // Store canonical user entry

                    } else {
                        // Update missing user fields when encountering new information
                        if (user.name == null) {
                            user.name = name;
                        }
                        if (user.phoneNumber == null) {
                            user.phoneNumber = phoneNumber;
                        }
                    }
                    skipMessage = isSystemMessage;
                    // Create new ChatMessage for this metadata line
                    message = new ChatMessage(datetime, user,
                            messageTo ? ChatMessage.CHAT_TYPE.TO : ChatMessage.CHAT_TYPE.FROM);
                }

                // -------------------------------
                // Line ≥ 2: message content
                // -------------------------------
                else {
                    if(!line.isEmpty() && message != null) {
                        message.content += line + " ";   // Append message text
                    }
                }

                lineCounter++;   // Move to next line within the message block
            }
        }

        return new ChatDataset(users, logs);
    }
}

