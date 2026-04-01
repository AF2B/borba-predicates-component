(ns borba.predicates
  "Predicate functions for common validation scenarios.

   Provides composable, pure predicates for validating strings, numbers,
   collections, and Brazilian-specific documents (CPF, CNPJ, CEP).

   ── Usage ────────────────────────────────────────────────────────────────────

     (require '[borba.predicates :as pred])

     (pred/email? \"user@example.com\")   ;; => true
     (pred/cpf? \"123.456.789-09\")       ;; => true or false (digit check)
     (pred/uuid? \"550e8400-...\")        ;; => true

   ── Composition ──────────────────────────────────────────────────────────────

     Use `all?`, `any?`, and `none?` to compose predicates:

     (pred/all? \"test@email.com\" pred/present? pred/email?)
     ;; => true only if both predicates pass

   ── Extension ────────────────────────────────────────────────────────────────

   Custom predicates can be registered via the multimethod in
   `borba.predicates.registry`."
   (:require [clojure.string :as str]))

;; ── String predicates ─────────────────────────────────────────────────────────

(defn blank?
  "Returns true if s is nil, empty, or contains only whitespace."
  [s]
  (or (nil? s)
      (and (string? s) (str/blank? s))))

(defn present?
  "Returns true if s is a non-nil, non-blank string."
  [s]
  (and (string? s) (not (str/blank? s))))

(defn min-length?
  "Returns true if s has at least n characters."
  [s n]
  (and (string? s) (>= (count s) n)))

(defn max-length?
  "Returns true if s has at most n characters."
  [s n]
  (and (string? s) (<= (count s) n)))

(defn length-between?
  "Returns true if s length is between min and max (inclusive)."
  [s min max]
  (and (min-length? s min) (max-length? s max)))

(defn matches?
  "Returns true if s matches the given regex pattern."
  [s pattern]
  (and (string? s) (boolean (re-matches pattern s))))

;; ── Format predicates ─────────────────────────────────────────────────────────

(def ^:private email-pattern
  #"^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$")

(defn email?
  "Returns true if s is a valid e-mail address."
  [s]
  (matches? s email-pattern))

(def ^:private uuid-pattern
  #"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

(defn uuid?
  "Returns true if s is a valid UUID string."
  [s]
  (matches? s uuid-pattern))

(def ^:private url-pattern
  #"^(https?://)[^\s/$.?#].[^\s]*$")

(defn url?
  "Returns true if s is a valid HTTP/HTTPS URL."
  [s]
  (matches? s url-pattern))

;; ── Number predicates ─────────────────────────────────────────────────────────

(defn positive?
  "Returns true if n is a positive number (> 0)."
  [n]
  (and (number? n) (pos? n)))

(defn negative?
  "Returns true if n is a negative number (< 0)."
  [n]
  (and (number? n) (neg? n)))

(defn zero?
  "Returns true if n is zero."
  [n]
  (and (number? n) (clojure.core/zero? n)))

(defn non-negative?
  "Returns true if n is a number >= 0."
  [n]
  (and (number? n) (>= n 0)))

(defn between?
  "Returns true if n is between min and max (inclusive)."
  [n min max]
  (and (number? n) (>= n min) (<= n max)))

;; ── Collection predicates ─────────────────────────────────────────────────────

(defn non-empty?
  "Returns true if coll is a non-nil, non-empty collection or string."
  [coll]
  (and (some? coll)
       (not (empty? coll))))

(defn has-min-count?
  "Returns true if coll has at least n elements."
  [coll n]
  (and (some? coll) (>= (count coll) n)))

(defn has-max-count?
  "Returns true if coll has at most n elements."
  [coll n]
  (and (some? coll) (<= (count coll) n)))

(defn contains-key?
  "Returns true if map m contains key k."
  [m k]
  (and (map? m) (contains? m k)))

;; ── Brazilian document predicates ─────────────────────────────────────────────

(defn- digits-only
  "Strips all non-digit characters from s."
  [s]
  (when (string? s)
    (clojure.string/replace s #"\D" "")))

(defn- cpf-digits-valid?
  "Validates CPF check digits (last 2 digits) against the first 9."
  [digits]
  (let [ns (mapv #(Character/digit % 10) digits)
        d1 (mod (- 11 (mod (reduce + (map * ns (range 10 1 -1))) 11)) 11)
        d1 (if (>= d1 10) 0 d1)
        d2 (mod (- 11 (mod (reduce + (map * (conj (vec (take 9 ns)) d1) (range 11 1 -1))) 11)) 11)
        d2 (if (>= d2 10) 0 d2)]
    (and (= d1 (nth ns 9))
         (= d2 (nth ns 10)))))

(defn cpf?
  "Returns true if s is a valid CPF number.
   Accepts formatted (000.000.000-00) or raw (00000000000) input.
   Validates both structure and check digits."
  [s]
  (when-let [d (digits-only s)]
    (and (= 11 (count d))
         (not (apply = (seq d)))
         (cpf-digits-valid? d))))

(defn- cnpj-digits-valid?
  "Validates CNPJ check digits against the first 12 digits."
  [digits]
  (let [ns    (mapv #(Character/digit % 10) digits)
        w1    [5 4 3 2 9 8 7 6 5 4 3 2]
        w2    [6 5 4 3 2 9 8 7 6 5 4 3 2]
        calc  (fn [ws]
                (let [r (mod (reduce + (map * ns ws)) 11)]
                  (if (< r 2) 0 (- 11 r))))
        d1    (calc w1)
        d2    (calc w2)]
    (and (= d1 (nth ns 12))
         (= d2 (nth ns 13)))))

(defn cnpj?
  "Returns true if s is a valid CNPJ number.
   Accepts formatted (00.000.000/0001-00) or raw (00000000000100) input.
   Validates both structure and check digits."
  [s]
  (when-let [d (digits-only s)]
    (and (= 14 (count d))
         (not (apply = (seq d)))
         (cnpj-digits-valid? d))))

(def ^:private cep-pattern #"^\d{5}-?\d{3}$")

(defn cep?
  "Returns true if s is a valid Brazilian CEP (postal code).
   Accepts formatted (00000-000) or raw (00000000) input."
  [s]
  (matches? s cep-pattern))

(def ^:private phone-br-pattern
  #"^(\+55\s?)?(\(?\d{2}\)?\s?)?(9\d{4}|\d{4})-?\d{4}$")

(defn phone-br?
  "Returns true if s is a valid Brazilian phone number.
   Accepts mobile (9 digits) and landline (8 digits), with or without DDD/country code."
  [s]
  (matches? s phone-br-pattern))

;; ── Composition ───────────────────────────────────────────────────────────────

(defn all?
  "Returns true if value satisfies all given predicates.

   (all? \"test@email.com\" present? email?)
   ;; => true only if both pass"
  [value & preds]
  (every? #(% value) preds))

(defn any?
  "Returns true if value satisfies at least one of the given predicates.

   (any? value cpf? cnpj?)
   ;; => true if either passes"
  [value & preds]
  (boolean (some #(% value) preds)))

(defn none?
  "Returns true if value satisfies none of the given predicates.

   (none? value blank? nil?)
   ;; => true only if both fail"
  [value & preds]
  (not (boolean (some #(% value) preds))))

(defn complement-pred
  "Returns a predicate that is the logical complement of pred.

   ((complement-pred blank?) \"\") ;; => false
   ((complement-pred blank?) \"hi\") ;; => true"
  [pred]
  (fn [value] (not (pred value))))
