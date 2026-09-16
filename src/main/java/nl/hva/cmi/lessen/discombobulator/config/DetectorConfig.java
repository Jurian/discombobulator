package nl.hva.cmi.lessen.discombobulator.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegexDetectorConfig.class,        name = "regex"),
        @JsonSubTypes.Type(value = TrieDetectorConfig.class,         name = "trie"),
        @JsonSubTypes.Type(value = AnchoredTrieDetectorConfig.class, name = "anchoredTrie"),
        @JsonSubTypes.Type(value = GreetingDetectorConfig.class,     name = "greeting"),
        @JsonSubTypes.Type(value = TrieTailRegexDetectorConfig.class, name = "trieTailRegex"),
})
public abstract class DetectorConfig {}
