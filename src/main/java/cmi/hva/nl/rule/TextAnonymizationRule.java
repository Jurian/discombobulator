package cmi.hva.nl.rule;

import cmi.hva.nl.parse.ChatMessage;

public interface TextAnonymizationRule {
    String apply(ChatMessage message);
}
