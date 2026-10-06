(ns borba.predicates.registry
  "A registry of custom predicates, keyed by keyword, for the rules that belong
   to a domain and not to this library.

   Register one with a method of `predicate`:

     (require '[borba.predicates.registry :as registry])

     (defmethod registry/predicate :valid-plan-code [_ value]
       (boolean (re-matches #\"PLN-\\d{6}\" value)))

     (registry/predicate :valid-plan-code \"PLN-123456\")
     ;; => true

   The registered keys are `(keys (methods registry/predicate))`.")

(defmulti predicate
  "Calls the custom predicate registered under a keyword, and throws when none
   is registered.
   - dispatch-key: the keyword the predicate was registered under
   - value: the value to check"
  (fn [dispatch-key _value] dispatch-key))

(defmethod predicate :default
  [dispatch-key _value]
  (throw (ex-info (str "[predicates] No predicate registered for: "
                       dispatch-key)
                  {:error        ::no-predicate-registered
                   :dispatch-key dispatch-key})))
