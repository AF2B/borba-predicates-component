(ns borba.predicates-test
  (:require
   [borba.predicates :as pred]
   [clojure.test :refer [are deftest is testing]]))

;; ── Strings ──────────────────────────────────────────────────────────────

(deftest blank?-test
  (are [value] (true? (pred/blank? value))
    nil "" "   " "\t\n")
  (are [value] (false? (pred/blank? value))
    "hello" " a " 42 :keyword []))

(deftest present?-test
  (are [value] (true? (pred/present? value))
    "hello" " a ")
  (are [value] (false? (pred/present? value))
    nil "" "   " 42 :keyword))

(deftest length-test
  (testing "min-length?"
    (is (true? (pred/min-length? "hello" 3)))
    (is (true? (pred/min-length? "hi" 2)))
    (is (false? (pred/min-length? "hi" 5)))
    (is (false? (pred/min-length? nil 1))))

  (testing "max-length?"
    (is (true? (pred/max-length? "hi" 5)))
    (is (true? (pred/max-length? "hello" 5)))
    (is (false? (pred/max-length? "hello world" 5)))
    (is (false? (pred/max-length? nil 5))))

  (testing "length-between? includes both bounds"
    (is (true? (pred/length-between? "hello" 3 10)))
    (is (true? (pred/length-between? "hi" 2 5)))
    (is (true? (pred/length-between? "hello" 3 5)))
    (is (false? (pred/length-between? "hi" 5 10)))
    (is (false? (pred/length-between? "hello world" 1 5)))))

(deftest matches?-test
  (is (true? (pred/matches? "abc123" #"[a-z]+\d+")))
  (is (false? (pred/matches? "123abc" #"[a-z]+\d+")))
  (testing "the whole string has to match, not a part of it"
    (is (false? (pred/matches? "abc123!" #"[a-z]+\d+"))))
  (testing "a value that is not a string never matches"
    (is (false? (pred/matches? nil #".*")))
    (is (false? (pred/matches? 42 #".*")))))

;; ── Formats ──────────────────────────────────────────────────────────────

(deftest email?-test
  (are [value] (true? (pred/email? value))
    "user@example.com" "user@mail.example.com" "user+tag@example.com"
    "first.last@example.co")
  (are [value] (false? (pred/email? value))
    "userexample.com" "user@" "@example.com" "user@example" nil 42
    "user @example.com")

  (testing "an address longer than a mailbox can have is refused"
    (let [local (apply str (repeat 250 "a"))]
      (is (false? (pred/email? (str local "@example.com")))))
    (is (true? (pred/email? (str (apply str (repeat 240 "a"))
                                 "@example.com"))))))

(deftest uuid?-test
  (are [value] (true? (pred/uuid? value))
    "550e8400-e29b-41d4-a716-446655440000"
    "550E8400-E29B-41D4-A716-446655440000"
    (random-uuid))
  (are [value] (false? (pred/uuid? value))
    "not-a-uuid" "550e8400e29b41d4a716446655440000" nil 42
    "550e8400-e29b-41d4-a716-44665544000"))

(deftest url?-test
  (are [value] (true? (pred/url? value))
    "http://example.com" "https://example.com/path?q=1"
    "https://example.com:8080/a#b")
  (are [value] (false? (pred/url? value))
    "example.com" "ftp://example.com" "https://" "https://exa mple.com"
    nil 42)

  (testing "a URL longer than a browser accepts is refused"
    (is (false? (pred/url? (str "https://example.com/"
                                (apply str (repeat 2048 "a"))))))))

;; ── Numbers ──────────────────────────────────────────────────────────────

(deftest sign-test
  (testing "positive?"
    (are [value] (true? (pred/positive? value))
      1 0.1 1/2 (bigdec 3))
    (are [value] (false? (pred/positive? value))
      0 -1 nil "1" Double/NaN))

  (testing "negative?"
    (are [value] (true? (pred/negative? value))
      -1 -0.1)
    (are [value] (false? (pred/negative? value))
      0 1 nil "-1" Double/NaN))

  (testing "zero?"
    (are [value] (true? (pred/zero? value))
      0 0.0 (bigdec 0))
    (are [value] (false? (pred/zero? value))
      1 -1 nil "0"))

  (testing "non-negative?"
    (are [value] (true? (pred/non-negative? value))
      0 5 0.0)
    (are [value] (false? (pred/non-negative? value))
      -1 nil "5" Double/NaN)))

(deftest between?-test
  (are [value] (true? (pred/between? value 1 10))
    1 5 10 1.5)
  (are [value] (false? (pred/between? value 1 10))
    0 11 nil "5" Double/NaN))

(deftest finite?-test
  (are [value] (true? (pred/finite? value))
    0 -1 1.5 1/3 (bigdec 2.5) Float/MAX_VALUE)
  (are [value] (false? (pred/finite? value))
    Double/NaN Double/POSITIVE_INFINITY Double/NEGATIVE_INFINITY
    Float/NaN nil "1" :a))

(deftest whole?-test
  (are [value] (true? (pred/whole? value))
    0 3 -7 3.0 6/2 (bigdec 4))
  (are [value] (false? (pred/whole? value))
    3.5 1/3 Double/NaN Double/POSITIVE_INFINITY nil "3"))

;; ── Collections ──────────────────────────────────────────────────────────

(deftest collection-test
  (testing "non-empty?"
    (are [value] (true? (pred/non-empty? value))
      [1] "a" {:a 1} #{1} '(1))
    (are [value] (false? (pred/non-empty? value))
      [] "" {} #{} nil 42))

  (testing "has-min-count?"
    (is (true? (pred/has-min-count? [1 2 3] 3)))
    (is (false? (pred/has-min-count? [1 2] 3)))
    (is (false? (pred/has-min-count? nil 0))))

  (testing "has-max-count?"
    (is (true? (pred/has-max-count? [1 2] 3)))
    (is (true? (pred/has-max-count? "abc" 3)))
    (is (false? (pred/has-max-count? [1 2 3 4] 3)))
    (is (false? (pred/has-max-count? 42 3))))

  (testing "contains-key?"
    (is (true? (pred/contains-key? {:a nil} :a)))
    (is (false? (pred/contains-key? {:a 1} :b)))
    (is (false? (pred/contains-key? [:a] :a)))
    (is (false? (pred/contains-key? nil :a)))))

;; ── Brazilian documents ──────────────────────────────────────────────────

(deftest cpf?-test
  (are [value] (true? (pred/cpf? value))
    "529.982.247-25" "52998224725")
  (are [value] (false? (pred/cpf? value))
    "529.982.247-26"
    "111.111.111-11"
    "00000000000"
    "529.982.247-2"
    "529982247-25"
    "abc52998224725"
    "529.982.247-25 "
    nil 52998224725))

(deftest cnpj?-test
  (are [value] (true? (pred/cnpj? value))
    "11.222.333/0001-81" "11222333000181")
  (are [value] (false? (pred/cnpj? value))
    "11.222.333/0001-82"
    "00.000.000/0000-00"
    "11222333000180"
    "11.222.333/0001-8"
    "11.222.333-0001/81"
    nil 11222333000181))

(deftest cep?-test
  (are [value] (true? (pred/cep? value))
    "01310-100" "01310100")
  (are [value] (false? (pred/cep? value))
    "0131-100" "01310-10" "013101000" "abcde-fgh" nil))

(deftest phone-br?-test
  (are [value] (true? (pred/phone-br? value))
    "(11) 98765-4321" "11987654321" "+55 11 98765-4321" "98765-4321"
    "(11) 3456-7890" "1134567890" "3456-7890")
  (are [value] (false? (pred/phone-br? value))
    "(11 98765-4321" "11) 98765-4321" "12345" "98765-432" "abc" nil))

;; ── Composition ──────────────────────────────────────────────────────────

(deftest composition-test
  (testing "all-of?"
    (is (true? (pred/all-of? "a@b.co" pred/present? pred/email?)))
    (is (false? (pred/all-of? "a@b" pred/present? pred/email?)))
    (is (true? (pred/all-of? "anything"))))

  (testing "any-of?"
    (is (true? (pred/any-of? "529.982.247-25" pred/cnpj? pred/cpf?)))
    (is (false? (pred/any-of? "nope" pred/cnpj? pred/cpf?)))
    (is (false? (pred/any-of? "anything"))))

  (testing "none-of?"
    (is (true? (pred/none-of? "nope" pred/cnpj? pred/cpf?)))
    (is (false? (pred/none-of? "529.982.247-25" pred/cnpj? pred/cpf?)))
    (is (true? (pred/none-of? "anything"))))

  (testing "complement-pred"
    (is (false? ((pred/complement-pred pred/blank?) "")))
    (is (true? ((pred/complement-pred pred/blank?) "hi")))))
