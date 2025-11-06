<!--
Sync Impact Report:
- Version change: 1.0.0
- Added sections: Core Principles, Additional Constraints, Development Workflow, Governance
- Modified principles: Library-First Design, CLI Interface, Test-First Development, Integration Testing, Observability & Versioning
- Templates requiring updates: ✅ Updated .specify/templates/plan-template.md, ✅ Updated .specify/templates/spec-template.md, ✅ Updated .specify/templates/tasks-template.md
- Follow-up TODOs: None
-->

# Java Example Project Constitution

## Core Principles

### I. Library-First Design
Every feature starts as a standalone library; Libraries must be self-contained, independently testable, and well-documented; Clear purpose is required - no organizational-only libraries. This ensures maximum reusability and maintainability of code components.

### II. CLI Interface
Every library exposes functionality via CLI; Text in/out protocol: stdin/args → stdout, errors → stderr; Support JSON + human-readable formats. This ensures all functionality is accessible and testable through standard interfaces.

### III. Test-First Development (NON-NEGOTIABLE)
TDD mandatory: Tests written → User approved → Tests fail → Then implement; Red-Green-Refactor cycle strictly enforced. This ensures code quality and that all functionality is properly verified before implementation.

### IV. Integration Testing
Focus areas requiring integration tests: New library contract tests, Contract changes, Inter-service communication, Shared schemas. This ensures that components work together correctly in realistic scenarios.

### V. Observability & Versioning
All components must be observable through structured logging and metrics; Use semantic versioning (MAJOR.MINOR.PATCH) for all releases; Breaking changes require MAJOR version bump. This ensures debuggability and proper change management.

## Additional Constraints

### Technology Stack
- Primary Language: Java (latest LTS version)
- Build Tool: Maven or Gradle
- Testing Framework: JUnit 5
- Documentation: Javadoc for all public APIs
- Code Style: Google Java Style Guide

### Quality Standards
- Code Coverage: Minimum 80% for all new features
- Static Analysis: SonarQube or similar tool integration
- Security: OWASP Top 10 compliance mandatory
- Performance: Response time under 200ms for 95th percentile

## Development Workflow

### Code Review Process
- All changes require at least one peer review
- Reviews must verify compliance with Constitution principles
- CI/CD pipeline must pass before merge
- Documentation updates required for all functional changes

### Release Process
- Feature branches for all new development
- Semantic versioning for all releases
- CHANGELOG.md updated with each release
- Release tags required for all versions

## Governance

### Amendment Procedure
This Constitution supersedes all other practices; Amendments require documentation, approval, and migration plan. All team members must be notified of changes within 48 hours of ratification./sp

### Compliance Review
All PRs/reviews must verify compliance with these principles; Complexity must be justified with architectural documentation; Use this document for runtime development guidance and decision-making.

### Versioning Policy
Version numbers follow semantic versioning (MAJOR.MINOR.PATCH):
- MAJOR: Backward incompatible changes or principle removals
- MINOR: New principles or materially expanded guidance
- PATCH: Clarifications, wording fixes, non-semantic refinements

**Version**: 1.0.0 | **Ratified**: 2025-11-03 | **Last Amended**: 2025-11-03