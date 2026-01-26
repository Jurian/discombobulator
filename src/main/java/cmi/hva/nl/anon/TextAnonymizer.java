package cmi.hva.nl.anon;

import cmi.hva.nl.anon.rule.*;
import cmi.hva.nl.anon.rule.impl.*;
import cmi.hva.nl.anon.rule.impl.regex.*;
import cmi.hva.nl.parse.ChatMessage;
import cmi.hva.nl.parse.LogParser;

import java.util.List;

/**
 * {@code TextAnonymizer} orchestrates the application of multiple
 * {@link cmi.hva.nl.anon.rule.TextAnonymizationRule TextAnonymizationRule}
 * instances to chat message content.
 *
 * <p>
 * From a GDPR perspective, this class acts as a <em>pseudonymization pipeline</em>
 * coordinator. It applies a configurable, ordered set of rule-based text
 * transformations that reduce the presence of directly and indirectly
 * identifying personal data in free-text messages.
 * </p>
 *
 * <p>
 * The effectiveness and semantics of the pseudonymization depend on:
 * </p>
 * <ul>
 *   <li>the selected rules,</li>
 *   <li>their ordering,</li>
 *   <li>the characteristics of the input text.</li>
 * </ul>
 *
 * <p>
 * This class does not itself detect or classify personal data; it delegates
 * all detection and replacement logic to the configured rules. As such, it
 * does not guarantee irreversible anonymization and should be considered a
 * technical component supporting data protection by design (GDPR Art. 25),
 * rather than a compliance mechanism on its own.
 * </p>
 *
 * <p>
 * Instances of this class are immutable with respect to rule configuration.
 * However, the {@link #anonymize(ChatMessage)} method mutates the
 * {@code content} field of the provided {@link ChatMessage}.
 * </p>
 */
public final class TextAnonymizer {

    /**
     * Ordered list of rules to apply to message content.
     *
     * <p>
     * Rules are applied sequentially; the output of one rule becomes the
     * input to the next. Rule ordering therefore directly affects the final
     * result.
     * </p>
     */
    private final List<TextAnonymizationRule> rules;


    /**
     * Creates a new {@code TextAnonymizer} with the given ordered rule set.
     *
     * @param rules the rules to apply, in execution order
     */
    public TextAnonymizer(List<TextAnonymizationRule> rules) {
        this.rules = rules;
    }


    /**
     * Applies all configured rules to the given chat message.
     *
     * <p>
     * Each rule is applied in sequence, and the {@code content} field of the
     * provided {@link ChatMessage} is updated after each step. The method
     * operates in-place on the message object.
     * </p>
     *
     * <p>
     * Individual rules are responsible for handling {@code null} content
     * values consistently.
     * </p>
     *
     * @param message the chat message whose content should be pseudonymized
     */
    public void anonymize(ChatMessage message) {
        for (TextAnonymizationRule rule : rules) {
            message.content = rule.apply(message);
        }
    }

    /**
     * Creates a {@code TextAnonymizer} preconfigured with a basic set of
     * rules for pseudonymizing common forms of personally identifiable
     * information (PII) in chat logs.
     *
     * <p>
     * The returned anonymizer applies rules in an order that prioritizes
     * normalization and high-risk identifiers before more contextual
     * detections:
     * </p>
     * <ol>
     *   <li>Whitespace normalization</li>
     *   <li>Track-and-trace codes</li>
     *   <li>File references</li>
     *   <li>URLs</li>
     *   <li>Address components</li>
     *   <li>Personal names</li>
     *   <li>Financial identifiers (IBAN)</li>
     *   <li>Phone numbers</li>
     *   <li>Email addresses</li>
     * </ol>
     *
     * <p>
     * This configuration is intended as a pragmatic default for log processing
     * and analysis. Depending on context, additional rules or alternative
     * ordering may be required.
     * </p>
     *
     * @param chatlogs parsed chat log metadata (currently unused, but retained
     *                 for configuration or future extensions)
     * @return a {@code TextAnonymizer} with a basic PII pseudonymization pipeline
     */
    public static TextAnonymizer basicPiiAnonymizer(LogParser.Result chatlogs) {
        return new TextAnonymizer(List.of(
                new NormalizationRule(),

                new PhoneRule(),
                new EmailRule(),
                new IBANRule(),
                new URLRule(),
                new TrackAndTraceRule(),
                new FileReferenceRule(),

                new UserNameRule(),
                new AddressRule()
        ));
    }
}
