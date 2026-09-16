package nl.hva.cmi.lessen.discombobulator.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppConfig {
    public SettingsConfig settings = new SettingsConfig();
    public Map<String, DetectorConfig> detectors = new LinkedHashMap<>();
    public List<StepConfig> pipeline = List.of();
    public InputConfig input;
    public OutputConfig output = new OutputConfig();
}
