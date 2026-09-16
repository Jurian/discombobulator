package nl.hva.cmi.lessen.discombobulator.parse;

import nl.hva.cmi.lessen.discombobulator.config.InputConfig;
import nl.hva.cmi.lessen.discombobulator.model.ChatDataset;

import java.io.IOException;

/**
 * Parses a Facebook Messenger JSON export into a {@link ChatDataset}.
 *
 * <p>Expected input: a {@code message_N.json} file from a Facebook "Download Your
 * Information" archive (Settings → Your Facebook Information → Download Your
 * Information → Messages). Each file contains one conversation with a
 * {@code participants} array and a {@code messages} array. Only messages with a
 * {@code content} field (plain text) are parsed; photos, GIFs, and reactions are
 * skipped.
 *
 * <p>Note: Facebook encodes all strings as Latin-1 bytes interpreted as UTF-8.
 * The file must be read with {@code ISO-8859-1} encoding and re-encoded to UTF-8
 * before JSON parsing.
 *
 * <p>Message direction ({@code CHAT_TYPE}) is left {@code null} because the export
 * does not mark which participant owns the account. Direction may be inferred in a
 * future implementation by comparing {@code sender_name} against a configured
 * account-owner name.
 */
public class FacebookParser implements Parser<ChatDataset> {

    public static ChatDataset fromConfig(InputConfig config) throws IOException {
        return new FacebookParser().parse(config.file);
    }

    @Override
    public ChatDataset parse(String fileName) throws IOException {
        throw new UnsupportedOperationException("FacebookParser is not yet implemented");
    }
}
