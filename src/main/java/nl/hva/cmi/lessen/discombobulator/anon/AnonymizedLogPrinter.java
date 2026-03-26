package nl.hva.cmi.lessen.discombobulator.anon;

import nl.hva.cmi.lessen.discombobulator.model.ChatLog;
import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;

public final class AnonymizedLogPrinter {

    /**
     * Prints a readable preview of anonymized chat logs.
     *
     * @param data   The full anonymized dataset (users + logs)
     * @param offset Number of conversations to skip before printing
     * @param limit  Maximum number of logs to print
     */
    public static void printSample(AnonymizedData data, int offset, int limit) {
        if (limit <= 0) {
            System.out.println("Nothing to print: limit <= 0");
            return;
        }

        int index = 0;
        int printed = 0;

        for (ChatLog log : data.logs().values()) {

            // Skip logs before the offset
            if (index++ < offset) {
                continue;
            }

            // Print this log
            System.out.println("====================================================");
            System.out.println("Conversation: " + log.id);
            System.out.println("Messages: " + log.messages.size());
            System.out.println("----------------------------------------------------");

            for (ChatMessage msg : log.messages) {
                System.out.println(
                        msg.datetime.toString() + " | "
                                + msg.type + " | "
                                + msg.user.id + " → "
                                + msg.content
                );
            }

            System.out.println("====================================================\n");

            printed++;
            if (printed >= limit) {
                break;
            }
        }

        if (printed == 0) {
            System.out.println("No conversations printed (offset too large?).");
        }
    }
}
