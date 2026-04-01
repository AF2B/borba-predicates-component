(ns borba.predicates.registry
  "Multimethod registry for custom predicates.

   ── Pattern ───────────────────────────────────────────────────────────────────

   Register domain-specific predicates in your service by implementing
   this multimethod:

     (require '[borba.predicates.registry :as registry])

     (defmethod registry/predicate :valid-plan-code [_ value]
       (boolean (re-matches #\"^PLN-\\d{6}$\" value)))

     (defmethod registry/predicate :active-employee [_ {:keys [status]}]
       (= :active status))

   The dispatch value is a keyword identifying the predicate.

   ── Calling registered predicates ────────────────────────────────────────────

     (registry/predicate :valid-plan-code \"PLN-123456\")  ;; => true
     (registry/predicate :active-employee {:status :active}) ;; => true

   ── Listing registered predicates ────────────────────────────────────────────

     (keys (methods registry/predicate))
     ;; => (:valid-plan-code :active-employee ...)")

(defmulti predicate
  "Registry of custom domain predicates.
   Dispatch key is the predicate keyword (e.g. :valid-plan-code).
   Returns true or false."
  (fn [dispatch-key _value] dispatch-key))

(defmethod predicate :default [k _]
  (throw (ex-info (str "[predicates] No predicate registered for: " k)
                  {:dispatch-key k})))
