package nl.hva.cmi.lessen.discombobulator.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DetectStepConfig.class,      name = "detect"),
        @JsonSubTypes.Type(value = ConsolidateStepConfig.class, name = "consolidate"),
        @JsonSubTypes.Type(value = ReplaceStepConfig.class,     name = "replace"),
})
public abstract class StepConfig {}
