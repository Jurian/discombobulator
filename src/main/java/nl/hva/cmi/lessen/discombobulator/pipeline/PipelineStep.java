package nl.hva.cmi.lessen.discombobulator.pipeline;

public interface PipelineStep {
    void applyStep(PipelineStep previousStep);
}
