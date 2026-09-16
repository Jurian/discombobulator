package nl.hva.cmi.lessen.discombobulator.parse;

import nl.hva.cmi.lessen.discombobulator.config.InputConfig;
import nl.hva.cmi.lessen.discombobulator.model.ChatDataset;

import java.io.IOException;

/**
 * Parses a Discord channel export into a {@link ChatDataset}.
 *
 * <p>Expected input: a JSON file produced by
 * <a href="https://github.com/Tyrrrz/DiscordChatExporter">DiscordChatExporter</a>
 * using the JSON format. The file contains a top-level {@code channel} object
 * (with {@code id} and {@code name}) and a {@code messages} array. Each message
 * has an {@code author} object ({@code id}, {@code name}), a {@code timestamp},
 * and a {@code content} string. Messages with empty content (embeds, attachments
 * only) are skipped.
 *
 * <p>Message direction ({@code CHAT_TYPE}) is left {@code null} because Discord
 * channels do not have an inherent TO/FROM axis.
 */
public class DiscordParser implements Parser<ChatDataset> {

    public static ChatDataset fromConfig(InputConfig config) throws IOException {
        return new DiscordParser().parse(config.file);
    }

    @Override
    public ChatDataset parse(String fileName) throws IOException {
        throw new UnsupportedOperationException("DiscordParser is not yet implemented");
    }
}
