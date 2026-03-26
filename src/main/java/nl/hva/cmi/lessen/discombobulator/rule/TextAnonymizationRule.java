package nl.hva.cmi.lessen.discombobulator.rule;

import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;

public interface TextAnonymizationRule {
    String apply(ChatMessage message);
}
