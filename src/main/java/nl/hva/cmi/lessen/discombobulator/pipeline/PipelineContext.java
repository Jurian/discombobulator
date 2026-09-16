package nl.hva.cmi.lessen.discombobulator.pipeline;

import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.Span;

import java.util.ArrayList;
import java.util.List;

public class PipelineContext {

    /** Original message — used by detectors that need direction or other metadata. */
    public final ChatMessage message;

    /** Current working text — updated by each replace step. */
    public String text;

    /** Spans accumulated by detect steps, consumed and cleared by replace/consolidate steps. */
    public final List<Span> spans = new ArrayList<>();

    public PipelineContext(ChatMessage message) {
        this.message = message;
        this.text = message.content;
    }
}
