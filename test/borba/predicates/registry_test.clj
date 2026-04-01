(ns borba.predicates.registry-test
  (:require [clojure.test :refer [deftest is testing]]
            [borba.predicates.registry :as registry]))

(defmethod registry/predicate :test/positive-even [_ n]
  (and (number? n) (pos? n) (even? n)))

(deftest registry-predicate-test
  (testing "registered predicate dispatches correctly"
    (is (registry/predicate :test/positive-even 4)))
  (testing "registered predicate returns false when condition fails"
    (is (not (registry/predicate :test/positive-even 3))))
  (testing "unregistered predicate throws ex-info"
    (is (thrown? clojure.lang.ExceptionInfo
                 (registry/predicate :test/unknown-predicate "value")))))
