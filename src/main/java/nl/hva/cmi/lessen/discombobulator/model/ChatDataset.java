package nl.hva.cmi.lessen.discombobulator.model;

import java.util.Map;

/**
 * The structured output of a chat log parser: a set of users and their
 * associated chat logs, keyed by their respective IDs.
 */
public record ChatDataset(Map<String, User> users, Map<String, ChatLog> logs) {}
