# borba-predicates-component

[![CI](https://github.com/AF2B/borba-predicates-component/actions/workflows/ci.yml/badge.svg)](https://github.com/AF2B/borba-predicates-component/actions/workflows/ci.yml)

Composable, pure predicates for validating strings, numbers, collections and Brazilian documents (CPF, CNPJ, CEP and phone
numbers), and a registry for the rules that belong to a domain.

Every predicate is **total**: it takes any value, returns `true` or `false`, and never throws. A validator can run them on
whatever arrives from the outside without a `try`, and the test suite checks that on generated values.

## Install

```clojure
io.github.af2b/borba-predicates-component
{:git/url "https://github.com/AF2B/borba-predicates-component"
 :git/tag "v1.0.0"
 :git/sha "<the commit of the tag, printed in the release notes>"}
```

It depends only on Clojure.

## Use

```clojure
(require '[borba.predicates :as pred])

(pred/email? "user@example.com")      ;; => true
(pred/email? "user@example")          ;; => false
(pred/cpf? "529.982.247-25")          ;; => true
(pred/cpf? "111.111.111-11")          ;; => false
(pred/cnpj? "11.222.333/0001-81")     ;; => true
(pred/phone-br? "(11) 98765-4321")    ;; => true
(pred/whole? 6/2)                     ;; => true
(pred/finite? Double/NaN)             ;; => false
(pred/positive? nil)                  ;; => false

(pred/all-of? "a@b.co" pred/present? pred/email?)    ;; => true
(pred/any-of? "529.982.247-25" pred/cnpj? pred/cpf?) ;; => true
```

Several names, such as `zero?` and `uuid?`, say what a core function says but accept any value. Refer to them through an alias, as
above, never with `:refer :all`.

### Custom predicates

A rule that belongs to a domain is registered, by keyword, with a method of `predicate`:

```clojure
(require '[borba.predicates.registry :as registry])

(defmethod registry/predicate :valid-plan-code [_ value]
  (boolean (re-matches #"PLN-\d{6}" value)))

(registry/predicate :valid-plan-code "PLN-123456")
;; => true
```

An unregistered key throws an `ex-info` whose data names it, because asking for a predicate nobody registered is a programming
error, not a validation result.

## The predicates

| Group | Predicates |
|---|---|
| Strings | `blank?`, `present?`, `min-length?`, `max-length?`, `length-between?`, `matches?` |
| Formats | `email?` (at most 254 characters), `uuid?` (a `UUID` or its canonical string), `url?` (http or https, at most 2048 characters) |
| Numbers | `positive?`, `negative?`, `zero?`, `non-negative?`, `between?`, `finite?`, `whole?` |
| Collections | `non-empty?`, `has-min-count?`, `has-max-count?`, `contains-key?` |
| Brazilian documents | `cpf?`, `cnpj?`, `cep?`, `phone-br?` |
| Composition | `all-of?`, `any-of?`, `none-of?`, `complement-pred` |

## Notes

- **CPF and CNPJ are checked, not only shaped.** Both accept the masked or the raw form, and nothing around it; they verify the
  two check digits by the modulo 11 rule and reject a repeated digit. The test suite generates correct numbers with an independent
  implementation of the rule and checks that a wrong check digit is always rejected.
- **`matches?` runs the caller's pattern.** A pattern with nested repetition can take exponential time on a hostile string, so
  bound the length of the input first. The patterns of this library are linear, and `email?` and `url?` refuse long values before
  they match.
- **`email?` checks the shape**, not that the mailbox exists: the only way to know that is to send a message.

## Development

```bash
make check      # lint, format, conventions, reflection, tests, coverage
make ci         # everything the pipelines enforce
```

See [CONTRIBUTING.md](CONTRIBUTING.md). The repository follows the [Borba standard](https://github.com/AF2B/borba-tooling/blob/main/docs/standard.md).

## License

[MIT](LICENSE)
