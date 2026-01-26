package cmi.hva.nl.anon;

import cmi.hva.nl.parse.ChatLog;
import cmi.hva.nl.parse.User;

import java.util.Map;

/**
 * Container for a pseudonymized dataset consisting of users and chat logs.
 *
 * <p>
 * From a GDPR perspective, this record represents the output of a
 * <em>pseudonymization</em> process (Article&nbsp;4(5)) where direct and indirect
 * identifiers have been replaced or reduced to lower the risk of data subject
 * identification. The contents should not be assumed to be irreversibly
 * anonymized: residual identifiability may remain due to context, undetected
 * identifiers, or linkage with external information.
 * </p>
 *
 * <p>
 * The {@code users} map typically contains synthetic user identifiers mapped to
 * {@link User} objects, while the {@code logs} map contains synthetic log
 * identifiers mapped to {@link ChatLog} instances whose message content has
 * been processed by a pseudonymization pipeline.
 * </p>
 *
 * @param users pseudonymized users keyed by synthetic user ID
 * @param logs  pseudonymized chat logs keyed by synthetic log ID
 */
public record AnonymizedData(Map<String, User> users, Map<String, ChatLog> logs) { }
