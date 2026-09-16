package nl.hva.cmi.lessen.discombobulator.parse;

import nl.hva.cmi.lessen.discombobulator.config.InputConfig;
import nl.hva.cmi.lessen.discombobulator.model.ChatDataset;

import java.io.IOException;

/**
 * Parses a Slack workspace export into a {@link ChatDataset}.
 *
 * <p>Expected input: the directory produced by a Slack workspace export
 * (Workspace Settings → Import/Export Data → Export). The directory contains:
 * <ul>
 *   <li>{@code users.json} — user list mapping user IDs to display names</li>
 *   <li>one subdirectory per channel, each containing daily JSON message files
 *       named {@code YYYY-MM-DD.json}</li>
 * </ul>
 * The {@code file} field in {@link nl.hva.cmi.lessen.discombobulator.config.InputConfig}
 * should point to the root export directory. Only messages with {@code type} "message"
 * and a non-empty {@code text} field are parsed; bot messages and system subtypes
 * are skipped.
 *
 * <p>Message direction ({@code CHAT_TYPE}) is left {@code null} because Slack
 * channels do not have an inherent TO/FROM axis.
 */
public class SlackParser implements Parser<ChatDataset> {

    public static ChatDataset fromConfig(InputConfig config) throws IOException {
        return new SlackParser().parse(config.file);
    }

    @Override
    public ChatDataset parse(String fileName) throws IOException {
        throw new UnsupportedOperationException("SlackParser is not yet implemented");
    }
}
