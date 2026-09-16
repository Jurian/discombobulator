package nl.hva.cmi.lessen.discombobulator.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SettingsConfig {
    /** Number of threads for parallel detection. -1 means use availableProcessors - 1. */
    @JsonProperty("threads")
    public int nThreads = -1;
}
