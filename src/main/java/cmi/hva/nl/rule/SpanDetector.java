package cmi.hva.nl.rule;

import cmi.hva.nl.parse.ChatMessage;

import java.util.List;

public interface SpanDetector {
    List<Span> detect(ChatMessage message);
}