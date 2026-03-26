package nl.hva.cmi.lessen.discombobulator.anon;

import nl.hva.cmi.lessen.discombobulator.model.ChatLog;
import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.User;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code DatasetAnonymizer} creates a pseudonymized copy of a chat dataset,
 * including user records and chat logs.
 *
 * <p>
 * From a GDPR perspective, this class supports <em>pseudonymization</em>
 * (Article&nbsp;4(5)) by:
 * </p>
 * <ul>
 *   <li>replacing user identifiers with role-based synthetic identifiers
 *       (e.g. {@code Customer_1}, {@code Employee_1}), and</li>
 *   <li>applying a {@link TextAnonymizer} pipeline to message content to reduce
 *       exposure of directly and indirectly identifying personal data.</li>
 * </ul>
 *
 * <p>
 * The produced dataset is intended to reduce identifiability for secondary use
 * cases such as analysis, quality monitoring, and AI experimentation.
 * This does <strong>not</strong> claim irreversible anonymization: the output
 * may still contain identifying information due to residual context, undetected
 * identifiers, or linkage with external information.
 * </p>
 *
 * <p>
 * Synthetic identifiers are assigned using sequential counters and therefore
 * depend on the iteration order of the input maps. Unless the input collections
 * have a stable iteration order (e.g. {@link java.util.LinkedHashMap}),
 * the resulting identifiers are <em>not guaranteed to be reproducible</em>
 * across multiple runs on the same logical dataset.
 * </p>
 *
 * <p>
 * Chat logs marked as internal ({@code ChatLog.isInternal}) are excluded from
 * the anonymized output.
 * </p>
 */
public final class DatasetAnonymizer {

    /**
     * Text pseudonymization pipeline applied to each message.
     */
    private final TextAnonymizer textAnonymizer;

    /**
     * Creates a new {@code DatasetAnonymizer} using the provided {@link TextAnonymizer}
     * for message content pseudonymization.
     *
     * @param textAnonymizer the text anonymizer to apply to message content
     */
    public DatasetAnonymizer(TextAnonymizer textAnonymizer) {
        this.textAnonymizer = textAnonymizer;
    }

    /**
     * Creates a pseudonymized copy of the given users and chat logs.
     *
     * <p>
     * Processing steps:
     * </p>
     * <ol>
     *   <li>Users are copied and assigned synthetic identifiers:
     *       {@code Customer_N} for customers and {@code Employee_N} for employees.</li>
     *   <li>A temporary mapping from original user IDs to synthetic user IDs is built.</li>
     *   <li>Chat logs are copied to new {@link ChatLog} instances with synthetic IDs
     *       {@code Log_N}. Logs marked {@code isInternal} are skipped.</li>
     *   <li>Each {@link ChatMessage} is copied and its {@code content} is processed
     *       in-place by {@link TextAnonymizer#anonymize(ChatMessage)}.</li>
     * </ol>
     *
     * <p>
     * Metadata such as message timestamps and directionality are preserved.
     * Role flags on users ({@code isCustomer}, {@code isSystem}) are copied through.
     * </p>
     *
     * @param originalUsers map of original user ID to user
     * @param originalLogs  map of original log ID to chat log
     * @return an {@link AnonymizedData} instance containing pseudonymized users and logs
     */
    public AnonymizedData anonymize(Map<String, User> originalUsers,
                                    Map<String, ChatLog> originalLogs) {

        int customerIdCounter = 1, employeeIdCounter = 1, logIdCounter = 1;
        // 1. Copy and anonymize users
        Map<String, User> anonUsers = new HashMap<>();

        Map<String, String> userIdMap = new HashMap<>();

        for (Map.Entry<String, User> entry : originalUsers.entrySet()) {

            User u = entry.getValue();
            User copy;

            if(u.isCustomer) {
                copy = new User("Customer_" + customerIdCounter++);
            } else {
                copy = new User("Employee_" + employeeIdCounter++);
            }

            copy.isCustomer = u.isCustomer;
            copy.isSystem = u.isSystem;

            userIdMap.put(u.id, copy.id);
            anonUsers.put(copy.id, copy);
        }

        // 2. Copy and anonymize logs + messages
        Map<String, ChatLog> anonLogs = new LinkedHashMap<>();
        for (Map.Entry<String, ChatLog> entry : originalLogs.entrySet()) {

            ChatLog originalLog = entry.getValue();

            if(originalLog.isInternal) continue;

            final String logId = "log_" + logIdCounter++;

            ChatLog anonLog = new ChatLog(logId);

            for (ChatMessage m : originalLog.messages) {
                // Look up anonymized user by id through temporary map
                User anonUser = anonUsers.get(userIdMap.get(m.user.id));

                ChatMessage anonMsg = new ChatMessage(
                        m.datetime,
                        anonUser,
                        m.type == ChatMessage.CHAT_TYPE.TO
                );
                anonMsg.content = m.content;
                textAnonymizer.anonymize(anonMsg);
                anonLog.addMessage(anonMsg);
            }

            anonLogs.put(logId, anonLog);
        }

        return new AnonymizedData(anonUsers, anonLogs);
    }
}
