package nl.hva.cmi.lessen.discombobulator.parse;

import nl.hva.cmi.lessen.discombobulator.config.InputConfig;
import nl.hva.cmi.lessen.discombobulator.model.ChatDataset;

import java.io.IOException;

/**
 * Parses a Signal chat export into a {@link ChatDataset}.
 *
 * <p>Expected input: a JSON file produced by a Signal backup tool such as
 * <a href="https://github.com/bepaald/signalbackup-tools">signalbackup-tools</a>.
 * Signal does not provide a native desktop export; the exact JSON schema therefore
 * depends on the tool used and should be verified before implementing this parser.
 *
 * <p>Messages with {@code type} "incoming" map to {@code CHAT_TYPE.FROM};
 * messages with {@code type} "outgoing" map to {@code CHAT_TYPE.TO}.
 * Messages without text content (attachments, calls) are skipped.
 */
public class SignalParser implements Parser<ChatDataset> {

    public static ChatDataset fromConfig(InputConfig config) throws IOException {
        return new SignalParser().parse(config.file);
    }

    @Override
    public ChatDataset parse(String fileName) throws IOException {
        throw new UnsupportedOperationException("SignalParser is not yet implemented");
    }
}
