package nl.hva.cmi.lessen.discombobulator;

import nl.hva.cmi.lessen.discombobulator.anon.AnonymizedData;
import nl.hva.cmi.lessen.discombobulator.anon.DatasetAnonymizer;
import nl.hva.cmi.lessen.discombobulator.anon.TextAnonymizer;
import nl.hva.cmi.lessen.discombobulator.model.ChatLog;
import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.User;
import nl.hva.cmi.lessen.discombobulator.parse.*;
import nl.hva.cmi.lessen.discombobulator.write.AnonymizedDataWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Main {


    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("Usage: java App <chatlog-file> <groupchat-file>");
            System.exit(1);
        }

        String chatlogFile = args[0];
        String groupchatFile = args[1];

        LogParser parser;

        // Parse group chat file
        TextFileParser<String> groupParser = new TextFileParser<>(line -> line);
        try {
            // Create log parser with loaded group chat set
            parser = new LogParser(groupParser.parse(groupchatFile));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load group chats", e);
        }

        // Parse chat logs
        LogParser.Result chatlog;
        try {
            chatlog = parser.parse(chatlogFile);

            chatlog.logs().entrySet().removeIf(entry -> {
                ChatLog log = entry.getValue();

                boolean hasCustomer = log.messages.stream()
                        .map(m -> m.user)
                        .anyMatch(User::isCustomer);

                return !hasCustomer; // remove if no customer
            });

            // Build basic PII anonymizer
            TextAnonymizer textAnonymizer = TextAnonymizer.basicPiiAnonymizer(chatlog);

            // Build dataset anonymizer
            DatasetAnonymizer datasetAnonymizer = new DatasetAnonymizer(textAnonymizer);

            // Create anonymized copy
            AnonymizedData anonymized = datasetAnonymizer.anonymize(chatlog.users(), chatlog.logs());

            AnonymizedDataWriter writer = new AnonymizedDataWriter();
            writer.writeJson(anonymized, Path.of("output/anonymized-chatlogs.json"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        //PARTICLES.sort(Comparator.comparingInt(String::length).reversed());
/*
        try (FileWriter writer = new FileWriter(Path.of("test.txt").toFile())) {

            TextFileParser<String> textFileParser =
                    new TextFileParser<>(line -> line.startsWith("#") ? null : line);

            Set<String> familyNames = textFileParser.parse("data/familienamen.txt");

            Set<String> expandedNames = new LinkedHashSet<>();

            for (String name : familyNames) {
                expandedNames.addAll(generateVariants(name));
            }

            for (String name : expandedNames) {
                writer.write(name);
                writer.write(System.lineSeparator());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
*/
/*
        try(
                FileWriter streetWriter = new FileWriter(Path.of("data/streetnameconflicts.txt").toFile());
                FileWriter placeWriter = new FileWriter(Path.of("data/placenameconflicts.txt").toFile())) {

            TextFileParser<String> textFileParser = new TextFileParser<>(line -> line.startsWith("#") ? null : line);

            Set<String> firstNames = textFileParser.parse("data/voornamen.txt");
            Set<String> familyNames = textFileParser.parse("data/familienamen.txt");
            Set<String> streetNames = textFileParser.parse("data/straatnamen.txt");
            Set<String> placeNames = textFileParser.parse("data/plaatsnamen.txt");

            Set<String> streetNameConflicts = new HashSet<>();
            Set<String> placeNameConflicts = new HashSet<>();

            for(String firstName : firstNames) {
                if(streetNames.contains(firstName)) {
                    streetNameConflicts.add(firstName);
                }
                if(placeNames.contains(firstName)) {
                    placeNameConflicts.add(firstName);
                }
            }
            streetWriter.write("# Conflicting first names\n");
            for(String conflict : streetNameConflicts) {
                streetWriter.write(conflict+"\n");
            }
            placeWriter.write("# Conflicting first names\n");
            for(String conflict : placeNameConflicts) {
                placeWriter.write(conflict+"\n");
            }
            streetNameConflicts.clear();
            placeNameConflicts.clear();

            for(String familyName : familyNames) {
                if(streetNames.contains(familyName)) {
                    streetNameConflicts.add(familyName);
                }
                if(placeNames.contains(familyName)) {
                    placeNameConflicts.add(familyName);
                }
            }
            streetWriter.write("# Conflicting family names\n");
            for(String conflict : streetNameConflicts) {
                streetWriter.write(conflict+"\n");
            }
            placeWriter.write("# Conflicting family names\n");
            for(String conflict : placeNameConflicts) {
                placeWriter.write(conflict+"\n");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
*/
        //AnonymizedLogPrinter.printSample(anonymized, 1000,1000);

        //log(chatlog);
    }

    private static List<String> PARTICLES = List.of(
            "van der",
            "van den",
            "van de",
            "in t",
            "van",
            "den",
            "der",
            "ter",
            "ten",
            "te",
            "de",
            "in"
    );


    private static Set<String> generateVariants(String name) {
        Set<String> variants = new LinkedHashSet<>();
        variants.add(name);

        String lower = name.toLowerCase(Locale.ROOT);

        for (String particle : PARTICLES) {
            if (lower.startsWith(particle + " ")) {
                String remainder = name.substring(particle.length());

                // lowercase particle
                variants.add(particle.toLowerCase(Locale.ROOT) + remainder);

                // capitalized particle
                variants.add(capitalize(particle) + remainder);

                break; // important: only the longest matching particle
            }
        }

        return variants;
    }

    private static String capitalize(String particle) {
        return Arrays.stream(particle.split(" "))
                .map(p -> Character.toUpperCase(p.charAt(0)) + p.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    public static void log(LogParser.Result chatlog) {
        long customerCount =
                chatlog.users().values().stream()
                        .filter(user -> user.isCustomer)
                        .count();

        long customerConversationCount =
                chatlog.logs().values().stream()
                        .filter(log -> log.messages.stream().anyMatch(message -> message.user.isCustomer))
                        .count();

        double avgConversationLength =
                chatlog.logs().values().stream()
                        .filter(log -> log.messages.stream().anyMatch(message -> message.user.isCustomer))
                        .mapToInt(ChatLog::length)
                        .average()
                        .orElse(0.0);

        System.out.println("Parse info:");
        System.out.println("Total users found: " + chatlog.users().size());
        System.out.println("Total customers found (estimated): " + customerCount);
        System.out.println("Total conversations with customers: " + customerConversationCount);

        System.out.println("Average employee - customer conversation length: " + avgConversationLength + " messages");

        System.out.println("Employees found:");
        chatlog.users().values().stream()
                .filter(user -> !user.isCustomer && !user.isSystem)
                .forEach(System.out::println);

        Set<User> customersNeverInConversation =
                chatlog.users().values().stream()
                        .filter(user -> user.isCustomer)
                        .filter(user ->
                                chatlog.logs().values().stream()
                                        .noneMatch(log ->
                                                log.messages.stream().anyMatch(m -> m.user == user)
                                        )
                        )
                        .collect(Collectors.toSet());

        System.out.println("Customers never appearing in any message: "
                + customersNeverInConversation.size());

        Set<ChatLog> customerOnlyMetadataLogs =
                chatlog.logs().values().stream()
                        .filter(log -> {
                            boolean hasCustomer = log.messages.stream()
                                    .anyMatch(m -> m.user.isCustomer);

                            if (hasCustomer) return false; // customer actually sends

                            // customer never sends, but metadata user might be a customer
                            // → check first message's user
                            return log.messages.stream().findFirst()
                                    .map(m -> m.user.isCustomer)
                                    .orElse(false);
                        })
                        .collect(Collectors.toSet());

        System.out.println("Logs where customer only appears in metadata: "
                + customerOnlyMetadataLogs.size());
        long emptyLogs =
                chatlog.logs().values().stream()
                        .filter(log -> log.messages.isEmpty())
                        .count();

        System.out.println("Empty logs (no messages): " + emptyLogs);
        Set<User> customersReceiveOnly =
                chatlog.users().values().stream()
                        .filter(User::isCustomer)
                        .filter(customer ->
                                chatlog.logs().values().stream()
                                        .flatMap(log -> log.messages.stream())
                                        .filter(m -> !m.user.isCustomer) // message sent by employee
                                        .anyMatch(m -> m.type == ChatMessage.CHAT_TYPE.TO)
                        )
                        .collect(Collectors.toSet());

        System.out.println("Customers who only receive messages: "
                + customersReceiveOnly.size());




// All messages in one stream (for convenience)
        var allMessages = chatlog.logs().values().stream()
                .flatMap(log -> log.messages.stream())
                .toList();

// Customers that appear in at least one message
        Set<User> customersInMessages = chatlog.users().values().stream()
                .filter(u -> u.isCustomer)
                .filter(u -> allMessages.stream().anyMatch(m -> m.user == u))
                .collect(Collectors.toSet());

        System.out.println("Customers appearing in at least one message: "
                + customersInMessages.size());

// Customers who send at least one message (type FROM)
        Set<User> customersWhoSend = chatlog.users().values().stream()
                .filter(u -> u.isCustomer)
                .filter(u -> allMessages.stream()
                        .anyMatch(m -> m.user == u && m.type == ChatMessage.CHAT_TYPE.FROM))
                .collect(Collectors.toSet());

        System.out.println("Customers who send at least one message: "
                + customersWhoSend.size());

// Customers who only ever receive (appear in messages, but never with type FROM)
        Set<User> customersOnlyReceive = customersInMessages.stream()
                .filter(u -> !customersWhoSend.contains(u))
                .collect(Collectors.toSet());

        System.out.println("Customers who only receive messages: "
                + customersOnlyReceive.size());



// Find all logs that contain more than one distinct customer
        List<ChatLog> groupChats =
                chatlog.logs().values().stream()
                        .filter(log -> {
                            long distinctCustomerCount =
                                    log.messages.stream()
                                            .map(m -> m.user)
                                            .filter(u -> u.isCustomer)
                                            .map(u -> u.id)      // distinct by user id
                                            .distinct()
                                            .count();
                            return distinctCustomerCount > 1;
                        })
                        .toList();

        System.out.println("Group chats with multiple customers: " + groupChats.size());

// Print their IDs and how many distinct customers each has
        groupChats.forEach(log -> {
            long customerCount2 =
                    log.messages.stream()
                            .map(m -> m.user)
                            .filter(u -> u.isCustomer)
                            .map(u -> u.id)
                            .distinct()
                            .count();

            System.out.println("Log ID: " + log.id +
                    " | distinct customers: " + customerCount2 +
                    " | messages: " + log.length());
        });
    }
}