/**
 * Parsing and structural representation of chat datasets and supporting resources.
 *
 * <p>
 * This package contains data model classes and parsers used to load, structure,
 * and preprocess chat data and auxiliary text resources. It provides the
 * foundational representations (users, messages, logs) that are consumed by
 * higher-level components such as pseudonymization and analysis pipelines.
 * </p>
 *
 * <h2>Scope and responsibilities</h2>
 * <p>
 * The {@code parse} package is responsible for:
 * </p>
 * <ul>
 *   <li>representing chat data in structured, in-memory models
 *       (e.g. {@link nl.hva.cmi.lessen.discombobulator.model.ChatLog}, {@link nl.hva.cmi.lessen.discombobulator.model.ChatMessage}),</li>
 *   <li>parsing text-based resources and configuration files,</li>
 *   <li>loading linguistic resources used by downstream processing.</li>
 * </ul>
 *
 * <p>
 * This package is <strong>not</strong> responsible for:
 * </p>
 * <ul>
 *   <li>anonymization or pseudonymization of personal data,</li>
 *   <li>privacy or compliance decisions,</li>
 *   <li>data minimization, masking, or redaction,</li>
 *   <li>interpretation or classification of personal data.</li>
 * </ul>
 *
 * <h2>Data characteristics</h2>
 * <p>
 * Objects in this package may contain raw, unmodified message content and user
 * identifiers. As such, instances should be treated as potentially containing
 * personal data and should not be exposed to untrusted consumers without
 * appropriate processing.
 * </p>
 *
 * <p>
 * Privacy-preserving transformations are expected to be applied by components
 * in {@code nl.hva.cmi.lessen.discombobulator.anon} or higher-level orchestration layers.
 * </p>
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>Parsing interfaces are intentionally minimal to allow flexible
 *       implementations.</li>
 *   <li>Most model classes are mutable and intended for controlled, internal
 *       use during parsing and preprocessing.</li>
 *   <li>Error handling is delegated to callers via checked exceptions where
 *       appropriate.</li>
 * </ul>
 */
package nl.hva.cmi.lessen.discombobulator.parse;
