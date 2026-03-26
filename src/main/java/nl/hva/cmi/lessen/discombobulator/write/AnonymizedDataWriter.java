package nl.hva.cmi.lessen.discombobulator.write;

import nl.hva.cmi.lessen.discombobulator.anon.AnonymizedData;
import nl.hva.cmi.lessen.discombobulator.model.ChatLog;
import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Writes {@link AnonymizedData} to disk in a structured JSON format.
 *
 * <p>
 * The output format is designed to be compact, explicit, and easy to
 * consume in downstream analysis pipelines. Role information is
 * encoded implicitly in synthetic identifiers (e.g. {@code Customer_1}).
 * </p>
 */
public final class AnonymizedDataWriter {

    private static final String SCHEMA_VERSION = "1.0";

    private final ObjectMapper mapper;

    public AnonymizedDataWriter() {
        this.mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Writes the given anonymized dataset to a JSON file.
     *
     * @param data the anonymized dataset
     * @param outputFile target file path
     * @throws IOException if writing fails
     */
    public void writeJson(AnonymizedData data, Path outputFile) throws IOException {
        Map<String, Object> root = new LinkedHashMap<>();

        root.put("schemaVersion", SCHEMA_VERSION);
        //root.put("users", data.users().keySet());
        root.put("logs", serializeLogs(data.logs()));

        mapper.writeValue(outputFile.toFile(), root);
    }

    private Map<String, Object> serializeLogs(Map<String, ChatLog> logs) {
        Map<String, Object> out = new LinkedHashMap<>();

        for (Map.Entry<String, ChatLog> e : logs.entrySet()) {
            ChatLog log = e.getValue();
            Map<String, Object> logMap = new LinkedHashMap<>();
            logMap.put(
                    "messages",
                    log.messages.stream().map(this::serializeMessage).toList()
            );
            out.put(e.getKey(), logMap);
        }

        return out;
    }

    private Map<String, Object> serializeMessage(ChatMessage m) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("datetime", m.datetime.toString());
        //msg.put("userId", m.user.id);
        msg.put("direction", m.type.name());
        msg.put("content", m.content);
        return msg;
    }
}