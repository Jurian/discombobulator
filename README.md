# Discombobulator — Chat Log Pseudonymization Toolkit

A Java toolkit for pseudonymizing chat log exports from WhatsApp, Telegram, Signal, and other platforms. Designed for researchers who need to reduce privacy risk in conversational datasets before storage, sharing, or analysis.

Pseudonymization is driven entirely by a YAML config file — no code changes are needed to adapt the tool to a new dataset or detection strategy.

---

## GDPR and privacy scope

This tool implements **pseudonymization** as defined in GDPR Article 4(5): direct and indirect identifiers are replaced with placeholders or synthetic identifiers. It is designed to support *data protection by design* (GDPR Art. 25).

Important caveats:
- This tool does **not** claim irreversible anonymization. Residual re-identification risk may remain due to false negatives, contextual inference, or external data linkage.
- The tool is a technical component, not a complete compliance solution. Legal responsibility for GDPR compliance remains with the data controller.
- Detection quality depends on the completeness of the name lists and regex patterns you provide.

---

## Requirements

- Java 17 or later
- Maven 3.6 or later

---

## Getting started

```bash
git clone <repository-url>
cd discombobulator
mvn package -q
java -jar target/discombobulator.jar config.yaml
```

If no config file argument is given, `config.yaml` in the working directory is used.

---

## Config file

All behaviour is controlled by a single YAML file. The top-level sections are:

```yaml
settings:   # optional runtime settings
detectors:  # named PII detectors
pipeline:   # ordered list of processing steps
input:      # source file and format
output:     # output path and filtering options
```

### `settings`

```yaml
settings:
  threads: 4   # number of parallel threads (default: availableProcessors - 1)
```

### `input`

```yaml
input:
  type: WHATSAPP          # WHATSAPP | TELEGRAM | SIGNAL | FACEBOOK | DISCORD | SLACK
  file: data/export.txt   # path to the chat export file (or directory for Slack)
  groupsFile: data/groups.txt  # (WhatsApp only) IDs of internal/employee group chats
```

### `output`

```yaml
output:
  file: output/pseudonymized.json   # output path (default: output/anonymized-chatlogs.json)
  requireRoles: [customer]          # only include logs where at least one participant
                                    # has one of these roles; omit or leave empty to
                                    # include all logs
```

---

## Detectors

Each named detector in the `detectors` section can be referenced by name from pipeline steps.

### `regex` — regular expression matching

```yaml
email:
  type: regex
  label: "[EMAIL]"
  pattern: '[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}'
  options:
    ignoreCase: true        # default: false
    multiline: false        # default: false
    dotAll: false           # default: false
    unicodeCharacterClass: false  # default: false
    comments: false         # default: false
```

Multi-line YAML block scalars (`>`) can be used to split long patterns across lines for readability. Whitespace introduced by the block scalar is automatically removed by Java's `COMMENTS` flag if `comments: true` is set.

### `trie` — dictionary matching (Aho-Corasick)

Matches any word or phrase from a plain-text word list (one entry per line). Efficient for large name lists.

```yaml
firstNames:
  type: trie
  label: "[NAME]"
  file: data/firstnames.txt
  options:
    ignoreCase: false       # default: false
    wholeWords: true        # default: false — only match on word boundaries
    ignoreOverlaps: true    # default: false — discard shorter overlapping matches
    ignoreFiles:            # entries found in these files are excluded from matching
      - data/ignore.txt
      - data/brandnames.txt
```

### `anchoredTrie` — dictionary matching near an anchor

Like `trie`, but only emits a match if a given anchor string (e.g. a previously placed label) appears nearby. Useful for detecting last names only when a first name label has already been placed in the same message.

```yaml
familyNamesNearName:
  type: anchoredTrie
  label: "[NAME]"
  file: data/lastnames.txt
  anchorPattern: "[NAME]"   # the anchor string to look for
  anchorMaxDistance: 5      # maximum character distance from the anchor
  anchorSide: BEFORE        # BEFORE | AFTER | EITHER
  options:
    wholeWords: true
    ignoreCase: false
    ignoreFiles: [data/ignore.txt]
```

### `greeting` — greeting + name pattern

Detects names that appear directly after a salutation (e.g. "Dear John", "Hoi Anna"). Labels the name differently depending on the direction of the message.

```yaml
greetingNames:
  type: greeting
  labelTo: "[CUSTOMER_NAME]"    # label when the message is sent TO the customer
  labelFrom: "[AGENT_NAME]"     # label when the message is FROM the customer
  # pattern: '...'              # override the default greeting regex (optional)
  # ignoreCase: true            # default: true
```

The default pattern covers common Dutch and English greetings.

---

## Pipeline

The pipeline is an ordered list of steps applied to each message. Messages themselves are processed in parallel across available CPU cores; the steps within each message run sequentially in the order defined.

### Spans

When a detector finds a match in a message, it records a **span**: a start and end position in the text together with a replacement label (e.g. `[EMAIL]`). Spans accumulate during `detect` steps and are consumed by `replace` and `consolidate` steps. They are never written to the output — only the final replaced text is.

### `detect`

Runs one or more detectors on the current message text and adds their matches to the span list.

```yaml
- type: detect
  detectors: [phone, email, iban]
```

### `replace`

Replaces all currently marked spans with their labels and clears the span list. Text after this step no longer contains the original values.

```yaml
- type: replace
```

### `consolidate`

Merges nearby spans into a single span with a new label. Useful for collapsing address components (street, number, postal code, city) that were detected separately but belong together.

```yaml
- type: consolidate
  label: "[ADDRESS]"
  maxSpanDistance: 10   # maximum character gap between spans to be merged
```

### Example pipeline

```yaml
pipeline:
  # Pass 1: unambiguous structured identifiers
  - type: detect
    detectors: [phone, email, iban, url]
  - type: replace

  # Pass 2: names (greeting-based first, then dictionary)
  - type: detect
    detectors: [greetingNames, firstNames, employeeNames]
  - type: replace

  # Pass 3: address components — detect, cluster, replace
  - type: detect
    detectors: [postalCode, street, city]
  - type: consolidate
    label: "[ADDRESS]"
    maxSpanDistance: 10
  - type: replace
```

Multiple detect → replace passes allow later passes to operate on already-pseudonymized text, which prevents re-matching of previously placed labels.

---

## Word list files

Word list files used by `trie` and `anchoredTrie` detectors should be:
- UTF-8 encoded
- One entry per line
- Lines starting with `#` are treated as comments and skipped

The `data/` directory is not included in this repository because it may contain sensitive or licensed linguistic resources (name lists, address data). You are expected to provide it locally:

```
data/
├── firstnames.txt
├── lastnames.txt
├── streetnames.txt
├── citynames.txt
├── employeenames.txt
├── brandnames.txt    # entries that should never be matched as names
└── ignore.txt        # general ignore list applied across multiple detectors
```

---

## Output format

The tool writes a JSON file with pseudonymized message content and synthetic user identifiers. Real names, phone numbers, and other identifiers are not present in the output.

```json
{
  "schemaVersion": "1.0",
  "logs": {
    "log_1": {
      "messages": [
        {
          "datetime": "2023-04-12T10:34:00",
          "direction": "FROM",
          "content": "Goedemorgen [CUSTOMER_NAME], ik heb een vraag over mijn bestelling."
        },
        {
          "datetime": "2023-04-12T10:35:12",
          "direction": "TO",
          "content": "Goedemorgen [AGENT_NAME]! Wat kan ik voor u doen?"
        }
      ]
    }
  }
}
```

Synthetic user identifiers are derived from the participant's role. For example, a user with role `customer` receives the identifier `Customer_1`, `Customer_2`, etc. A user with role `participant` receives `Participant_1`, and so on. Users with no role assigned receive `User_N`.

---

## Supported input formats

| Format | Status | Notes |
|---|---|---|
| WhatsApp | Supported | Text export from WhatsApp (Android/iOS). Supports group chats and contact-to-contact logs. |
| Telegram | Planned | JSON export from Telegram Desktop. |
| Signal | Planned | JSON export via third-party tools (e.g. signalbackup-tools). |
| Facebook Messenger | Planned | JSON from Facebook "Download Your Information". |
| Discord | Planned | JSON from DiscordChatExporter. |
| Slack | Planned | Directory export from Slack workspace settings. |

---

## License

(Add license information here.)
