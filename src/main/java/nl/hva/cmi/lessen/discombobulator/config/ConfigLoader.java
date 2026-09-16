package nl.hva.cmi.lessen.discombobulator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ConfigLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    public static AppConfig load(String path) throws IOException {
        return MAPPER.readValue(new File(path), AppConfig.class);
    }

    public static AppConfig load(Path path) throws IOException {
        return MAPPER.readValue(path.toFile(), AppConfig.class);
    }
}
