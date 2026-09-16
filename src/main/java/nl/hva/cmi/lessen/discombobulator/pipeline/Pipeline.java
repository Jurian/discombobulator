package nl.hva.cmi.lessen.discombobulator.pipeline;

import java.util.List;

public class Pipeline {

    private final List<PipelineStep> steps;

    public Pipeline(List<PipelineStep> steps) {
        this.steps = List.copyOf(steps);
    }

    public void run(PipelineContext ctx) {
        for (PipelineStep step : steps) {
            step.apply(ctx);
        }
    }
}
