package nl.hva.cmi.lessen.discombobulator.parse;

import nl.hva.cmi.lessen.discombobulator.config.InputConfig;
import nl.hva.cmi.lessen.discombobulator.model.ChatDataset;

import java.io.IOException;

/**
 * Parses a Telegram Desktop JSON export into a {@link ChatDataset}.
 *
 * <p>Expected input: the {@code result.json} file produced by Telegram Desktop via
 * Settings → Advanced → Export Telegram Data. Each chat is exported as a single
 * JSON object containing a {@code messages} array. Text-only messages are parsed;
 * media, stickers, polls and service messages are skipped.
 *
 * <p>Message direction ({@code CHAT_TYPE}) is left {@code null} because Telegram
 * exports do not distinguish a single account owner in group contexts. For personal
 * chats a future implementation may infer direction from {@code from_id}.
 */
public class TelegramParser implements Parser<ChatDataset> {

    public static ChatDataset fromConfig(InputConfig config) throws IOException {
        return new TelegramParser().parse(config.file);
    }

    @Override
    public ChatDataset parse(String fileName) throws IOException {
        throw new UnsupportedOperationException("TelegramParser is not yet implemented");
    }
}
