(ns borba.predicates-properties-test
  "Properties of the predicates on generated values: that they are total, which
   is what lets a validator run them on whatever arrives, and that the check
   digits of CPF and CNPJ accept every correct number and reject a wrong one."
  (:require
   [borba.predicates :as pred]
   [clojure.test.check.clojure-test :as tct]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]))

(def ^:private trials 300)

(def ^:private unary-predicates
  [pred/blank? pred/present? pred/email? pred/uuid? pred/url?
   pred/positive? pred/negative? pred/zero? pred/non-negative?
   pred/finite? pred/whole? pred/non-empty? pred/cpf? pred/cnpj?
   pred/cep? pred/phone-br?])

(def ^:private sized-predicates
  [pred/min-length? pred/max-length? pred/has-min-count?
   pred/has-max-count?])

(def ^:private gen-anything
  (gen/one-of [gen/any-printable
               (gen/elements [Double/NaN Double/POSITIVE_INFINITY
                              Double/NEGATIVE_INFINITY ##NaN])]))

(tct/defspec every-unary-predicate-is-total-and-boolean trials
  (prop/for-all [value gen-anything]
    (every? #(boolean? (% value)) unary-predicates)))

(tct/defspec every-sized-predicate-is-total-and-boolean trials
  (prop/for-all [value gen-anything
                 size  gen/small-integer]
    (every? #(boolean? (% value size)) sized-predicates)))

;; The check digits, computed here as the specification states them, without
;; sharing code with the library.

(defn- check-digit
  [digits weights]
  (let [digit (- 11 (mod (reduce + (map * digits weights)) 11))]
    (if (>= digit 10) 0 digit)))

(defn- with-check-digits
  [base first-weights second-weights]
  (let [first-digit  (check-digit base first-weights)
        second-digit (check-digit (conj base first-digit) second-weights)]
    (conj base first-digit second-digit)))

(defn- not-repeated?
  [digits]
  (not (apply = digits)))

(defn- digits->string
  [digits]
  (apply str digits))

(defn- cpf-mask
  [raw]
  (str (subs raw 0 3) "." (subs raw 3 6) "." (subs raw 6 9) "-" (subs raw 9)))

(defn- cnpj-mask
  [raw]
  (str (subs raw 0 2) "." (subs raw 2 5) "." (subs raw 5 8) "/"
       (subs raw 8 12) "-" (subs raw 12)))

(defn- digits-gen
  [size]
  (gen/such-that not-repeated? (gen/vector (gen/choose 0 9) size)))

(def ^:private gen-cpf
  (gen/fmap #(with-check-digits % [10 9 8 7 6 5 4 3 2]
               [11 10 9 8 7 6 5 4 3 2])
            (digits-gen 9)))

(def ^:private gen-cnpj
  (gen/fmap #(with-check-digits % [5 4 3 2 9 8 7 6 5 4 3 2]
               [6 5 4 3 2 9 8 7 6 5 4 3 2])
            (digits-gen 12)))

(defn- change-digit
  "Returns the digits with the one at an index replaced by another digit."
  [digits index]
  (update digits index #(mod (inc %) 10)))

(tct/defspec a-correct-cpf-is-accepted trials
  (prop/for-all [digits gen-cpf]
    (let [raw (digits->string digits)]
      (and (pred/cpf? raw) (pred/cpf? (cpf-mask raw))))))

(tct/defspec a-cpf-with-a-wrong-check-digit-is-rejected trials
  (prop/for-all [digits gen-cpf
                 index  (gen/elements [9 10])]
    (not (pred/cpf? (digits->string (change-digit digits index))))))

(tct/defspec a-cpf-with-anything-around-it-is-rejected trials
  (prop/for-all [digits gen-cpf
                 noise  (gen/not-empty gen/string-alphanumeric)]
    (let [raw (digits->string digits)]
      (not (or (pred/cpf? (str noise raw))
               (pred/cpf? (str raw noise)))))))

(tct/defspec a-correct-cnpj-is-accepted trials
  (prop/for-all [digits gen-cnpj]
    (let [raw (digits->string digits)]
      (and (pred/cnpj? raw) (pred/cnpj? (cnpj-mask raw))))))

(tct/defspec a-cnpj-with-a-wrong-check-digit-is-rejected trials
  (prop/for-all [digits gen-cnpj
                 index  (gen/elements [12 13])]
    (not (pred/cnpj? (digits->string (change-digit digits index))))))

(tct/defspec composition-follows-boolean-logic trials
  (prop/for-all [value gen-anything]
    (let [preds [pred/blank? pred/present? pred/positive? pred/non-empty?]]
      (and (= (apply pred/none-of? value preds)
              (not (apply pred/any-of? value preds)))
           (= (apply pred/all-of? value preds)
              (every? #(% value) preds))))))
