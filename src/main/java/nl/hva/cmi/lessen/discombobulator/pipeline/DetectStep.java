package nl.hva.cmi.lessen.discombobulator.pipeline;

import nl.hva.cmi.lessen.discombobulator.steps.detect.SpanDetector;

import java.util.List;

public class DetectStep implements PipelineStep {

    private final List<SpanDetector> detectors;

    public DetectStep(List<SpanDetector> detectors) {
        this.detectors = List.copyOf(detectors);
    }

    @Override
    public void apply(PipelineContext ctx) {
        for (SpanDetector detector : detectors) {
            ctx.spans.addAll(detector.detect(ctx.text, ctx.message));
        }
    }
}
