(ns borba.predicates
  "Composable, pure predicates for validating strings, numbers, collections
   and Brazilian documents (CPF, CNPJ, CEP and phone numbers).

   Every predicate is total: it takes any value, returns true or false and
   never throws, so a validator can run them on whatever arrives from the
   outside.

     (require '[borba.predicates :as pred])

     (pred/email? \"user@example.com\")  ;; => true
     (pred/cpf? \"529.982.247-25\")      ;; => true
     (pred/all-of? \"a@b.co\"
                   pred/present?
                   pred/email?)         ;; => true

   Several names, such as `zero?`, say what a core function says but accept
   any value; refer to them through an alias, never with :refer :all.
   Custom predicates are registered with the multimethod in
   `borba.predicates.registry`."
  (:refer-clojure :exclude [uuid? zero?])
  (:require
   [clojure.string :as str]))

(set! *warn-on-reflection* true)

;; ── Limits and patterns ─────────────────────────────────────────────────

(def ^:private max-email-length
  "The longest e-mail address a mailbox can have, by RFC 5321."
  254)

(def ^:private max-url-length 2048)

(def ^:private modulus 11)
(def ^:private first-valid-remainder 2)

(def ^:private cpf-first-weights [10 9 8 7 6 5 4 3 2])
(def ^:private cpf-second-weights [11 10 9 8 7 6 5 4 3 2])
(def ^:private cnpj-first-weights [5 4 3 2 9 8 7 6 5 4 3 2])
(def ^:private cnpj-second-weights [6 5 4 3 2 9 8 7 6 5 4 3 2])

(def ^:private email-pattern
  #"^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$")

(def ^:private uuid-pattern
  #"^[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}$")

(def ^:private url-pattern
  #"^https?://[^\s/$.?#][^\s]*$")

(def ^:private cpf-pattern
  #"^(\d{3}\.\d{3}\.\d{3}-\d{2}|\d{11})$")

(def ^:private cnpj-pattern
  #"^(\d{2}\.\d{3}\.\d{3}/\d{4}-\d{2}|\d{14})$")

(def ^:private cep-pattern #"^\d{5}-?\d{3}$")

(def ^:private phone-br-pattern
  #"^(\+55\s?)?(\(\d{2}\)|\d{2})?\s?(9\d{4}|\d{4})-?\d{4}$")

;; ── String predicates ───────────────────────────────────────────────────

(defn blank?
  "Returns true when the value is nil, an empty string or a string of only
   whitespace.
   - value: any value"
  [value]
  (or (nil? value)
      (and (string? value) (str/blank? value))))

(defn present?
  "Returns true when the value is a string with something other than
   whitespace in it.
   - value: any value"
  [value]
  (and (string? value) (not (str/blank? value))))

(defn min-length?
  "Returns true when the value is a string of at least a given length.
   - value: any value
   - minimum: the least number of characters"
  [value
   minimum]
  (and (string? value) (>= (count value) minimum)))

(defn max-length?
  "Returns true when the value is a string of at most a given length.
   - value: any value
   - maximum: the most number of characters"
  [value
   maximum]
  (and (string? value) (<= (count value) maximum)))

(defn length-between?
  "Returns true when the value is a string whose length is within the bounds,
   both included.
   - value: any value
   - minimum: the least number of characters
   - maximum: the most number of characters"
  [value
   minimum
   maximum]
  (and (min-length? value minimum) (max-length? value maximum)))

(defn matches?
  "Returns true when the value is a string that the pattern matches entirely.
   The pattern is the caller's: a pattern with nested repetition can take
   exponential time on a hostile string, so bound the length first.
   - value: any value
   - pattern: a regular expression"
  [value
   pattern]
  (and (string? value) (boolean (re-matches pattern value))))

;; ── Format predicates ───────────────────────────────────────────────────

(defn email?
  "Returns true when the value is a plausible e-mail address of at most 254
   characters. It checks the shape, not that the mailbox exists.
   - value: any value"
  [value]
  (and (max-length? value max-email-length)
       (matches? value email-pattern)))

(defn uuid?
  "Returns true when the value is a UUID, or a string in the canonical
   8-4-4-4-12 hexadecimal form.
   - value: any value"
  [value]
  (or (clojure.core/uuid? value)
      (matches? value uuid-pattern)))

(defn url?
  "Returns true when the value is an http or https URL of at most 2048
   characters.
   - value: any value"
  [value]
  (and (max-length? value max-url-length)
       (matches? value url-pattern)))

;; ── Number predicates ───────────────────────────────────────────────────

(defn positive?
  "Returns true when the value is a number greater than zero.
   - value: any value"
  [value]
  (and (number? value) (pos? value)))

(defn negative?
  "Returns true when the value is a number less than zero.
   - value: any value"
  [value]
  (and (number? value) (neg? value)))

(defn zero?
  "Returns true when the value is a number equal to zero.
   - value: any value"
  [value]
  (and (number? value) (clojure.core/zero? value)))

(defn non-negative?
  "Returns true when the value is a number that is not less than zero.
   - value: any value"
  [value]
  (and (number? value) (>= value 0)))

(defn between?
  "Returns true when the value is a number within the bounds, both included.
   - value: any value
   - lower: the least value
   - upper: the greatest value"
  [value
   lower
   upper]
  (and (number? value) (>= value lower) (<= value upper)))

(defn finite?
  "Returns true when the value is a number that is neither NaN nor infinite.
   - value: any value"
  [value]
  (and (number? value)
       (or (not (or (instance? Double value) (instance? Float value)))
           (let [x (double value)]
             (not (or (Double/isNaN x) (Double/isInfinite x)))))))

(defn whole?
  "Returns true when the value is a finite number with no fractional part,
   whatever its type: 3, 3.0 and 6/2 are whole.
   - value: any value"
  [value]
  (and (finite? value) (clojure.core/zero? (rem value 1))))

;; ── Collection predicates ───────────────────────────────────────────────

(defn- sized?
  "Returns true when the value is a collection or a string, which is what
   count and empty? are defined on here.
   - value: any value"
  [value]
  (or (coll? value) (string? value)))

(defn non-empty?
  "Returns true when the value is a collection or a string with something in
   it.
   - value: any value"
  [value]
  (and (sized? value) (boolean (seq value))))

(defn has-min-count?
  "Returns true when the value is a collection or a string of at least a
   given size.
   - value: any value
   - minimum: the least number of elements"
  [value
   minimum]
  (and (sized? value) (>= (count value) minimum)))

(defn has-max-count?
  "Returns true when the value is a collection or a string of at most a given
   size.
   - value: any value
   - maximum: the most number of elements"
  [value
   maximum]
  (and (sized? value) (<= (count value) maximum)))

(defn contains-key?
  "Returns true when the value is a map that has the key.
   - value: any value
   - k: the key to look for"
  [value
   k]
  (and (map? value) (contains? value k)))

;; ── Brazilian documents ─────────────────────────────────────────────────

(defn- digits-of
  "Returns the digits of a string as numbers, ignoring the punctuation of a
   document mask.
   - text: a string of digits and mask characters"
  [text]
  (->> text
       (filter #(<= (int \0) (int %) (int \9)))
       (mapv #(- (int %) (int \0)))))

(defn- check-digit
  "Returns the check digit of the weighed digits, by the modulo 11 rule that
   CPF and CNPJ share: the remainder of the weighed sum, subtracted from 11,
   and 0 when the remainder is 0 or 1.
   - digits: the digits to weigh
   - weights: one weight for each digit that is used"
  [digits
   weights]
  (let [remainder (mod (reduce + (map * digits weights)) modulus)]
    (if (< remainder first-valid-remainder)
      0
      (- modulus remainder))))

(defn- valid-document?
  "Returns true when a masked or raw document is well formed, is not the same
   digit repeated, and carries the two check digits its weights compute.
   - text: the document as given
   - pattern: the shapes the document may be written in
   - first-weights: the weights of the first check digit
   - second-weights: the weights of the second check digit"
  [text
   pattern
   first-weights
   second-weights]
  (and (matches? text pattern)
       (let [digits (digits-of text)
             base   (count first-weights)]
         (and (not (apply = digits))
              (= (digits base) (check-digit digits first-weights))
              (= (digits (inc base))
                 (check-digit digits second-weights))))))

(defn cpf?
  "Returns true when the value is a valid CPF, written as 000.000.000-00 or as
   eleven digits, with correct check digits and not a repeated digit.
   - value: any value"
  [value]
  (valid-document? value cpf-pattern cpf-first-weights cpf-second-weights))

(defn cnpj?
  "Returns true when the value is a valid CNPJ, written as
   00.000.000/0000-00 or as fourteen digits, with correct check digits and not
   a repeated digit.
   - value: any value"
  [value]
  (valid-document? value cnpj-pattern cnpj-first-weights cnpj-second-weights))

(defn cep?
  "Returns true when the value is a Brazilian postal code, written as
   00000-000 or as eight digits.
   - value: any value"
  [value]
  (matches? value cep-pattern))

(defn phone-br?
  "Returns true when the value is a Brazilian phone number: a mobile (nine
   digits, starting with 9) or a landline (eight), with or without the area
   code, which may be in parentheses, and with or without +55.
   - value: any value"
  [value]
  (matches? value phone-br-pattern))

;; ── Composition ─────────────────────────────────────────────────────────

(defn all-of?
  "Returns true when the value satisfies every predicate.
   - value: the value to check
   - preds: the predicates it has to satisfy"
  [value & preds]
  (every? #(boolean (% value)) preds))

(defn any-of?
  "Returns true when the value satisfies at least one predicate.
   - value: the value to check
   - preds: the predicates, one of which it has to satisfy"
  [value & preds]
  (boolean (some #(% value) preds)))

(defn none-of?
  "Returns true when the value satisfies no predicate.
   - value: the value to check
   - preds: the predicates it must not satisfy"
  [value & preds]
  (not (apply any-of? value preds)))

(defn complement-pred
  "Returns a predicate that is true exactly when the given one is false.
   - pred: the predicate to complement"
  [pred]
  (fn [value] (not (pred value))))
