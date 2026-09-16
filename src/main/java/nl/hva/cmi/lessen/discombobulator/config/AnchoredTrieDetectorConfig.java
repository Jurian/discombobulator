package nl.hva.cmi.lessen.discombobulator.config;

import nl.hva.cmi.lessen.discombobulator.steps.detect.AnchoredTrieSpanDetector.AnchorSide;

public class AnchoredTrieDetectorConfig extends TrieDetectorConfig {
    public String anchorPattern;
    public int anchorMaxDistance = -1;
    public AnchorSide anchorSide = AnchorSide.BEFORE;
}
