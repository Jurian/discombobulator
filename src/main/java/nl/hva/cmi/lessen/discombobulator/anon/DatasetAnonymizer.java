package nl.hva.cmi.lessen.discombobulator.anon;

import nl.hva.cmi.lessen.discombobulator.model.ChatLog;
import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.User;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates a pseudonymized copy of a chat dataset by replacing real user and
 * log identifiers with synthetic ones (e.g. {@code Customer_1}, {@code Participant_2},
 * {@code log_N}).
 *
 * <p>Synthetic user identifiers are derived from each user's role: the role string
 * is title-cased and combined with a per-role counter (e.g. role {@code "customer"}
 * → {@code Customer_1}). Users with no role assigned receive the prefix {@code User_}.
 *
 * <p>Text content is assumed to have already been processed by the anonymization
 * pipeline before this class is called; it is copied as-is.
 *
 * <p>Synthetic identifiers are assigned in iteration order of the input maps,
 * so reproducible results require stable-order collections (e.g. LinkedHashMap).
 *
 * <p>Logs marked {@code isInternal} are excluded from the output.
 */
public final class DatasetAnonymizer {

    public AnonymizedData anonymize(Map<String, User> originalUsers,
                                    Map<String, ChatLog> originalLogs) {

        Map<String, Integer> roleCounters = new HashMap<>();
        int nLogs = 1;

        Map<String, User> anonUsers = new HashMap<>();
        Map<String, String> userIdMap = new HashMap<>();

        for (Map.Entry<String, User> entry : originalUsers.entrySet()) {
            User u = entry.getValue();
            String prefix = pseudonymPrefix(u.role);
            int n = roleCounters.merge(prefix, 1, Integer::sum);
            User copy = new User(prefix + "_" + n);
            copy.role = u.role;
            userIdMap.put(u.id, copy.id);
            anonUsers.put(copy.id, copy);
        }

        Map<String, ChatLog> anonLogs = new LinkedHashMap<>();
        for (Map.Entry<String, ChatLog> entry : originalLogs.entrySet()) {
            ChatLog original = entry.getValue();
            if (original.isInternal) continue;

            String logId = "log_" + nLogs++;
            ChatLog anonLog = new ChatLog(logId);
            anonLog.name = original.name;

            for (ChatMessage m : original.messages) {
                User anonUser = anonUsers.get(userIdMap.get(m.user.id));
                ChatMessage anonMsg = new ChatMessage(m.datetime, anonUser, m.type);
                anonMsg.content = m.content;
                anonLog.addMessage(anonMsg);
            }

            anonLogs.put(logId, anonLog);
        }

        return new AnonymizedData(anonUsers, anonLogs);
    }

    private static String pseudonymPrefix(String role) {
        if (role == null || role.isEmpty()) return "User";
        return Character.toUpperCase(role.charAt(0)) + role.substring(1).toLowerCase();
    }
}
