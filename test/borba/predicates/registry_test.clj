(ns borba.predicates.registry-test
  (:require
   [borba.predicates.registry :as registry]
   [clojure.test :refer [deftest is testing]]))

(defmethod registry/predicate :test/positive-even
  [_ n]
  (and (number? n) (pos? n) (even? n)))

(deftest predicate-test
  (testing "a registered predicate is called with the value"
    (is (true? (registry/predicate :test/positive-even 4)))
    (is (false? (registry/predicate :test/positive-even 3))))

  (testing "the registered keys can be listed"
    (is (contains? (methods registry/predicate) :test/positive-even)))

  (testing "an unregistered key throws data that names it"
    (let [thrown (try (registry/predicate :test/unknown "value")
                      (catch clojure.lang.ExceptionInfo e e))]
      (is (= :borba.predicates.registry/no-predicate-registered
             (:error (ex-data thrown))))
      (is (= :test/unknown (:dispatch-key (ex-data thrown)))))))
