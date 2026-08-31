# Echo Chamber – The Perspective Flipper

> “Flip the perspective. See beyond your side.”

An AI-powered perspective flipper that analyzes user statements, arguments, and topics to break cognitive echo chambers. It delivers positive points, negative counterpoints, a 3D flipped perspective, cognitive bias detection, and a perspective balance score.

---

## Architecture Overview

```text
┌─────────────────────────┐         POST /api/analyze         ┌─────────────────────────┐
│     HTML / CSS / JS     │ ─────────────────────────────────> │   Spring Boot Backend   │
│  (Modern AI Dashboard)  │ <───────────────────────────────── │    (Java 17 / MVC)      │
└─────────────────────────┘      Structured Analysis JSON      └────────────┬────────────┘
                                                                            │
                                                                   Remote AI API / Engine
                                                                            ▼
                                                                ┌─────────────────────────┐
                                                                │  OpenAI / Custom LLM    │
                                                                └─────────────────────────┘