# Chat Log Pseudonymization Toolkit

This repository contains a Java-based toolkit for parsing chat logs and
applying rule-based pseudonymization to reduce the exposure of personal data
in free-text conversations.

The primary use case is the privacy-preserving secondary use of chat data,
for example in research, analytics, quality monitoring, or AI-assisted
experimentation.

---

## Overview

The project is structured into two main packages:

### `nl.hva.cmi.lessen.discombobulator.parse`
Provides data models and parsers for:
- chat logs and messages,
- users and metadata,
- auxiliary text resources (e.g. name lists).

This package **does not perform anonymization**. Parsed objects may still
contain raw personal data.

### `nl.hva.cmi.lessen.discombobulator.anon`
Implements rule-based pseudonymization:
- text-level pseudonymization pipelines,
- dataset-level copying with synthetic identifiers,
- modular rules for detecting and replacing identifiers such as:
  - names,
  - email addresses,
  - phone numbers,
  - addresses,
  - URLs,
  - financial identifiers,
  - shipment tracking codes.

The anonymization process is deterministic but **does not claim irreversible
anonymization**.

---

## GDPR and privacy scope

This project implements **pseudonymization** as defined in
GDPR Article 4(5).

- Direct and indirect identifiers are replaced with placeholders or synthetic
  identifiers.
- Residual re-identification risk may remain due to context, false negatives,
  or external linkage.
- The toolkit is a technical component intended to support
  *data protection by design* (GDPR Art. 25), not a complete compliance solution.

Legal responsibility for GDPR compliance remains with the data controller.

---

## Rule-based approach

Pseudonymization is implemented using an ordered pipeline of rules.
Examples include:

- normalization of whitespace,
- detection of email addresses, phone numbers, IBANs,
- dictionary-based detection of names and places,
- consolidation of nearby address components.

Rule ordering matters and can be customized depending on the use case.

---

## Data directory (important)

Sensitive linguistic resources (e.g. name lists, ignore lists) are expected to
be located in a local `data/` directory.

**This directory is intentionally NOT included in the Git repository.**

Reasons:
- the files may contain sensitive or licensed data,
- contents may vary per deployment or institution,
- it avoids accidental publication of personal data.

You are expected to provide the following locally, for example:

```text
data/
├── voornamen.txt
├── familienamen.txt
├── werknemernamen.txt
├── klantnamen.txt
├── straatnamen.txt
├── plaatsnamen.txt
├── brandnames.txt
└── ignore.txt
```

All files in `data/` should be UTF-8 encoded, one entry per line.
Lines starting with `#` may be used for comments where supported.

---

## Output

Code for writing pseudonymized datasets back to disk (e.g. as log files or
structured exports) is intentionally not yet included and will be added later.

The current focus of this repository is:
- correctness of parsing,
- robustness of pseudonymization rules,
- clear separation of responsibilities.

---

## Intended audience

This repository is intended for:
- researchers working with conversational data,
- developers building privacy-aware tooling,
- internal analytics or QA teams,
- educational or experimental use.

It is **not** intended as a drop-in production anonymization service.

---

## License

(Add license information here.)