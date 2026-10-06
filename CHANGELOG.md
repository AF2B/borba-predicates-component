# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-10-06

First release.

### Added

- Predicates for strings (`blank?`, `present?`, `min-length?`, `max-length?`, `length-between?`, `matches?`), formats (`email?`,
  `uuid?`, `url?`), numbers (`positive?`, `negative?`, `zero?`, `non-negative?`, `between?`, `finite?`, `whole?`), collections
  (`non-empty?`, `has-min-count?`, `has-max-count?`, `contains-key?`) and Brazilian documents (`cpf?`, `cnpj?`, `cep?`,
  `phone-br?`), and their composition (`all-of?`, `any-of?`, `none-of?`, `complement-pred`).
- `borba.predicates.registry`, a multimethod for the custom predicates of a domain.
- Every predicate is total: it takes any value and returns `true` or `false`, which the test suite checks on generated values.
- `email?` and `url?` refuse a value longer than a mailbox or a browser accepts before they match it.
- `cpf?` and `cnpj?` accept the masked or the raw form and nothing around it, verify both check digits and reject a repeated
  digit; the suite checks them against an independent implementation of the rule.
- A test suite with 100% coverage.

[Unreleased]: https://github.com/AF2B/borba-predicates-component/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/AF2B/borba-predicates-component/releases/tag/v1.0.0
