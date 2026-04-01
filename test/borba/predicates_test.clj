(ns borba.predicates-test
  (:require [clojure.test :refer [deftest is testing]]
            [borba.predicates :as pred]))

;; ── String predicates ─────────────────────────────────────────────────────────

(deftest blank?-test
  (testing "nil is blank"
    (is (pred/blank? nil)))
  (testing "empty string is blank"
    (is (pred/blank? "")))
  (testing "whitespace-only string is blank"
    (is (pred/blank? "   ")))
  (testing "non-blank string is not blank"
    (is (not (pred/blank? "hello"))))
  (testing "number is not blank"
    (is (not (pred/blank? 42)))))

(deftest present?-test
  (testing "non-blank string is present"
    (is (pred/present? "hello")))
  (testing "nil is not present"
    (is (not (pred/present? nil))))
  (testing "empty string is not present"
    (is (not (pred/present? ""))))
  (testing "whitespace is not present"
    (is (not (pred/present? "   "))))
  (testing "number is not present"
    (is (not (pred/present? 42)))))

(deftest min-length?-test
  (testing "string meets minimum length"
    (is (pred/min-length? "hello" 3)))
  (testing "string exactly at minimum"
    (is (pred/min-length? "hi" 2)))
  (testing "string below minimum"
    (is (not (pred/min-length? "hi" 5))))
  (testing "nil returns false"
    (is (not (pred/min-length? nil 1)))))

(deftest max-length?-test
  (testing "string within max length"
    (is (pred/max-length? "hi" 5)))
  (testing "string exactly at max"
    (is (pred/max-length? "hello" 5)))
  (testing "string exceeds max"
    (is (not (pred/max-length? "hello world" 5))))
  (testing "nil returns false"
    (is (not (pred/max-length? nil 5)))))

(deftest length-between?-test
  (testing "string within range"
    (is (pred/length-between? "hello" 3 10)))
  (testing "string at lower bound"
    (is (pred/length-between? "hi" 2 5)))
  (testing "string at upper bound"
    (is (pred/length-between? "hello" 3 5)))
  (testing "string below range"
    (is (not (pred/length-between? "hi" 5 10))))
  (testing "string above range"
    (is (not (pred/length-between? "hello world" 1 5)))))

(deftest matches?-test
  (testing "string matches pattern"
    (is (pred/matches? "abc123" #"^[a-z]+\d+$")))
  (testing "string does not match pattern"
    (is (not (pred/matches? "123abc" #"^[a-z]+\d+$"))))
  (testing "nil does not match"
    (is (not (pred/matches? nil #".*")))))

;; ── Format predicates ─────────────────────────────────────────────────────────

(deftest email?-test
  (testing "valid email"
    (is (pred/email? "user@example.com")))
  (testing "valid email with subdomain"
    (is (pred/email? "user@mail.example.com")))
  (testing "valid email with plus"
    (is (pred/email? "user+tag@example.com")))
  (testing "missing @ is invalid"
    (is (not (pred/email? "userexample.com"))))
  (testing "missing domain is invalid"
    (is (not (pred/email? "user@"))))
  (testing "nil is invalid"
    (is (not (pred/email? nil)))))

(deftest uuid?-test
  (testing "valid UUID"
    (is (pred/uuid? "550e8400-e29b-41d4-a716-446655440000")))
  (testing "invalid UUID format"
    (is (not (pred/uuid? "not-a-uuid"))))
  (testing "nil is invalid"
    (is (not (pred/uuid? nil)))))

(deftest url?-test
  (testing "valid http URL"
    (is (pred/url? "http://example.com")))
  (testing "valid https URL"
    (is (pred/url? "https://example.com/path?q=1")))
  (testing "invalid URL without scheme"
    (is (not (pred/url? "example.com"))))
  (testing "nil is invalid"
    (is (not (pred/url? nil)))))

;; ── Number predicates ─────────────────────────────────────────────────────────

(deftest positive?-test
  (testing "positive integer"
    (is (pred/positive? 1)))
  (testing "positive float"
    (is (pred/positive? 0.1)))
  (testing "zero is not positive"
    (is (not (pred/positive? 0))))
  (testing "negative is not positive"
    (is (not (pred/positive? -1))))
  (testing "nil is not positive"
    (is (not (pred/positive? nil)))))

(deftest negative?-test
  (testing "negative integer"
    (is (pred/negative? -1)))
  (testing "zero is not negative"
    (is (not (pred/negative? 0))))
  (testing "positive is not negative"
    (is (not (pred/negative? 1)))))

(deftest zero?-test
  (testing "zero integer"
    (is (pred/zero? 0)))
  (testing "positive is not zero"
    (is (not (pred/zero? 1))))
  (testing "negative is not zero"
    (is (not (pred/zero? -1)))))

(deftest non-negative?-test
  (testing "zero is non-negative"
    (is (pred/non-negative? 0)))
  (testing "positive is non-negative"
    (is (pred/non-negative? 5)))
  (testing "negative is not non-negative"
    (is (not (pred/non-negative? -1)))))

(deftest between?-test
  (testing "value within range"
    (is (pred/between? 5 1 10)))
  (testing "value at lower bound"
    (is (pred/between? 1 1 10)))
  (testing "value at upper bound"
    (is (pred/between? 10 1 10)))
  (testing "value below range"
    (is (not (pred/between? 0 1 10))))
  (testing "value above range"
    (is (not (pred/between? 11 1 10)))))

;; ── Collection predicates ─────────────────────────────────────────────────────

(deftest non-empty?-test
  (testing "non-empty vector"
    (is (pred/non-empty? [1 2 3])))
  (testing "non-empty string"
    (is (pred/non-empty? "hello")))
  (testing "non-empty map"
    (is (pred/non-empty? {:a 1})))
  (testing "empty vector is not non-empty"
    (is (not (pred/non-empty? []))))
  (testing "nil is not non-empty"
    (is (not (pred/non-empty? nil)))))

(deftest has-min-count?-test
  (testing "collection meets min count"
    (is (pred/has-min-count? [1 2 3] 2)))
  (testing "collection exactly at min"
    (is (pred/has-min-count? [1 2] 2)))
  (testing "collection below min"
    (is (not (pred/has-min-count? [1] 2)))))

(deftest has-max-count?-test
  (testing "collection within max count"
    (is (pred/has-max-count? [1 2] 5)))
  (testing "collection exactly at max"
    (is (pred/has-max-count? [1 2 3] 3)))
  (testing "collection exceeds max"
    (is (not (pred/has-max-count? [1 2 3 4] 3)))))

(deftest contains-key?-test
  (testing "map contains key"
    (is (pred/contains-key? {:name "Alice"} :name)))
  (testing "map does not contain key"
    (is (not (pred/contains-key? {:name "Alice"} :age))))
  (testing "nil map returns false"
    (is (not (pred/contains-key? nil :name))))
  (testing "non-map returns false"
    (is (not (pred/contains-key? "string" :name)))))

;; ── Brazilian document predicates ─────────────────────────────────────────────

(deftest cpf?-test
  (testing "valid CPF formatted"
    (is (pred/cpf? "529.982.247-25")))
  (testing "valid CPF raw"
    (is (pred/cpf? "52998224725")))
  (testing "invalid CPF — all same digits"
    (is (not (pred/cpf? "111.111.111-11"))))
  (testing "invalid CPF — wrong check digits"
    (is (not (pred/cpf? "123.456.789-00"))))
  (testing "nil is invalid"
    (is (not (pred/cpf? nil))))
  (testing "too short is invalid"
    (is (not (pred/cpf? "1234567890")))))

(deftest cnpj?-test
  (testing "valid CNPJ formatted"
    (is (pred/cnpj? "11.222.333/0001-81")))
  (testing "valid CNPJ raw"
    (is (pred/cnpj? "11222333000181")))
  (testing "invalid CNPJ — all same digits"
    (is (not (pred/cnpj? "00.000.000/0000-00"))))
  (testing "invalid CNPJ — wrong check digits"
    (is (not (pred/cnpj? "11.222.333/0001-00"))))
  (testing "nil is invalid"
    (is (not (pred/cnpj? nil)))))

(deftest cep?-test
  (testing "valid CEP formatted"
    (is (pred/cep? "01310-100")))
  (testing "valid CEP raw"
    (is (pred/cep? "01310100")))
  (testing "invalid CEP — too short"
    (is (not (pred/cep? "0131010"))))
  (testing "nil is invalid"
    (is (not (pred/cep? nil)))))

(deftest phone-br?-test
  (testing "valid mobile with DDD"
    (is (pred/phone-br? "(11) 99999-9999")))
  (testing "valid landline with DDD"
    (is (pred/phone-br? "(11) 3333-4444")))
  (testing "invalid phone — too short"
    (is (not (pred/phone-br? "9999"))))
  (testing "nil is invalid"
    (is (not (pred/phone-br? nil)))))

;; ── Composition ───────────────────────────────────────────────────────────────

(deftest all?-test
  (testing "all predicates pass"
    (is (pred/all? "test@email.com" pred/present? pred/email?)))
  (testing "one predicate fails"
    (is (not (pred/all? "" pred/present? pred/email?))))
  (testing "all predicates fail"
    (is (not (pred/all? nil pred/present? pred/email?)))))

(deftest any?-test
  (testing "one predicate passes"
    (is (pred/any? "52998224725" pred/email? pred/cpf?)))
  (testing "all predicates fail"
    (is (not (pred/any? "invalid" pred/email? pred/uuid?))))
  (testing "all predicates pass"
    (is (pred/any? "test@email.com" pred/present? pred/email?))))

(deftest none?-test
  (testing "no predicates pass"
    (is (pred/none? "not-an-email" pred/email? pred/uuid?)))
  (testing "one predicate passes"
    (is (not (pred/none? "test@email.com" pred/email? pred/uuid?))))
  (testing "all predicates pass"
    (is (not (pred/none? "test@email.com" pred/present? pred/email?)))))

(deftest complement-pred-test
  (testing "complement of blank? returns true for non-blank"
    (is ((pred/complement-pred pred/blank?) "hello")))
  (testing "complement of blank? returns false for blank"
    (is (not ((pred/complement-pred pred/blank?) ""))))
  (testing "complement of present? returns true for nil"
    (is ((pred/complement-pred pred/present?) nil))))
