/**
 * High-level pseudonymization of chat datasets and message content.
 *
 * <p>
 * This package provides orchestration logic for applying rule-based text
 * pseudonymization to chat datasets, including users, chat logs, and message
 * content. It builds on the lower-level rules defined in
 * {@link cmi.hva.nl.anon.rule} to support privacy-preserving secondary use of
 * conversational data.
 * </p>
 *
 * <h2>GDPR terminology and scope</h2>
 * <p>
 * The functionality in this package is designed to support
 * <em>pseudonymization</em> as defined in Article&nbsp;4(5) of the GDPR.
 * Identifiers are replaced with synthetic identifiers or placeholders in order
 * to reduce the likelihood of data subject identification.
 * </p>
 *
 * <p>
 * The output produced by this package must <strong>not</strong> be assumed to be
 * irreversibly anonymized. Residual re-identification risk may remain due to:
 * </p>
 * <ul>
 *   <li>contextual information in free text,</li>
 *   <li>false negatives in rule-based detection,</li>
 *   <li>role or structural information that is intentionally preserved,</li>
 *   <li>potential linkage with external datasets.</li>
 * </ul>
 *
 * <h2>Responsibilities</h2>
 * <p>
 * This package is responsible for:
 * </p>
 * <ul>
 *   <li>coordinating ordered application of text pseudonymization rules,</li>
 *   <li>creating pseudonymized copies of users and chat logs,</li>
 *   <li>assigning synthetic identifiers to users and logs,</li>
 *   <li>producing datasets suitable for analysis, experimentation, or tooling
 *       where exposure of raw personal data is not required.</li>
 * </ul>
 *
 * <p>
 * This package is <strong>not</strong> responsible for:
 * </p>
 * <ul>
 *   <li>legal compliance decisions or GDPR applicability assessment,</li>
 *   <li>access control or authorization,</li>
 *   <li>data retention or deletion policies,</li>
 *   <li>cryptographic anonymization or irreversible de-identification.</li>
 * </ul>
 *
 * <h2>Determinism and reproducibility</h2>
 * <p>
 * Pseudonymization is deterministic at the level of individual rule execution.
 * However, assignment of synthetic identifiers (e.g. {@code Customer_1},
 * {@code Log_1}) may depend on the iteration order of input collections.
 * Unless stable input ordering is guaranteed, identifier values are not
 * reproducible across runs.
 * </p>
 *
 * <h2>Intended usage</h2>
 * <p>
 * Typical usage includes:
 * </p>
 * <ul>
 *   <li>preparing chat logs for research or analysis,</li>
 *   <li>privacy-preserving evaluation of conversational AI systems,</li>
 *   <li>quality monitoring and error analysis,</li>
 *   <li>sharing datasets internally without exposing raw identifiers.</li>
 * </ul>
 *
 * <p>
 * This package should be used as part of a broader data protection by design
 * approach (GDPR Art.&nbsp;25), in combination with organizational and technical
 * safeguards.
 * </p>
 */
package cmi.hva.nl.anon;
