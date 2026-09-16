package nl.hva.cmi.lessen.discombobulator;

import nl.hva.cmi.lessen.discombobulator.anon.AnonymizedData;
import nl.hva.cmi.lessen.discombobulator.anon.DatasetAnonymizer;
import nl.hva.cmi.lessen.discombobulator.config.AppConfig;
import nl.hva.cmi.lessen.discombobulator.config.ConfigLoader;
import nl.hva.cmi.lessen.discombobulator.config.PipelineFactory;
import nl.hva.cmi.lessen.discombobulator.model.ChatDataset;
import nl.hva.cmi.lessen.discombobulator.model.ChatLog;
import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.parse.*;

import nl.hva.cmi.lessen.discombobulator.pipeline.Pipeline;
import nl.hva.cmi.lessen.discombobulator.pipeline.PipelineContext;
import nl.hva.cmi.lessen.discombobulator.write.AnonymizedDataWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    public static void main(String[] args) throws IOException {
        String configFile = args.length > 0 ? args[0] : "config.yaml";

        AppConfig config = ConfigLoader.load(configFile);

        int nThreads = config.settings.nThreads > 0
                ? config.settings.nThreads
                : resolveThreadCount();
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        try {
            Pipeline pipeline = PipelineFactory.build(config);

            ChatDataset chatlog = parseInput(config);

            // Discard logs that don't meet role requirements
            if (!config.output.requireRoles.isEmpty()) {
                Set<String> required = new HashSet<>(config.output.requireRoles);
                chatlog.logs().entrySet().removeIf(entry ->
                        entry.getValue().messages.stream()
                                .noneMatch(m -> required.contains(m.user.role)));
            }

            // Apply pipeline to each message in parallel
            List<Future<?>> futures = new ArrayList<>();
            for (ChatLog log : chatlog.logs().values()) {
                for (ChatMessage message : log.messages) {
                    futures.add(executor.submit(() -> {
                        PipelineContext ctx = new PipelineContext(message);
                        pipeline.run(ctx);
                        message.content = ctx.text;
                    }));
                }
            }
            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Pipeline interrupted", e);
                } catch (ExecutionException e) {
                    throw new RuntimeException("Pipeline failed", e.getCause());
                }
            }

            AnonymizedData anonymized = new DatasetAnonymizer()
                    .anonymize(chatlog.users(), chatlog.logs());

            new AnonymizedDataWriter()
                    .writeJson(anonymized, Path.of(config.output.file));

        } finally {
            executor.shutdown();
        }
    }

    private static int resolveThreadCount() {
        return Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    }

    private static ChatDataset parseInput(AppConfig config) throws IOException {
        if (config.input == null) throw new IllegalArgumentException("No input configured");

        return switch (config.input.type.toUpperCase()) {
            case "WHATSAPP" -> WhatsappParser.fromConfig(config.input);
            case "TELEGRAM" -> TelegramParser.fromConfig(config.input);
            case "SIGNAL"   -> SignalParser.fromConfig(config.input);
            case "FACEBOOK" -> FacebookParser.fromConfig(config.input);
            case "DISCORD"  -> DiscordParser.fromConfig(config.input);
            case "SLACK"    -> SlackParser.fromConfig(config.input);
            default -> throw new IllegalArgumentException("Unknown input type: " + config.input.type);
        };
    }
}
