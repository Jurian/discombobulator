package nl.hva.cmi.lessen.discombobulator.config;

import java.util.List;

public class OutputConfig {
    public String file = "output/anonymized-chatlogs.json";
    /** Only include logs where at least one participant has a role in this list. Empty means include all logs. */
    public List<String> requireRoles = List.of();
}
