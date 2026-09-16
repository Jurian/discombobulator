package nl.hva.cmi.lessen.discombobulator.pipeline;

public interface PipelineStep {
    void apply(PipelineContext ctx);
}
