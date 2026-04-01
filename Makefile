.PHONY: test coverage fmt-check fmt-fix lint build clean deps help

# ── Development ───────────────────────────────────────────────────────────────

test: ## Run tests with Kaocha
	clojure -M:test

coverage: ## Run tests with coverage report (fails below 85%)
	clojure -M:test -m kaocha.runner \
		--plugin kaocha.plugin/cloverage \
		--cov-output target/coverage \
		--cov-fail-threshold 85

fmt-check: ## Check code formatting with cljfmt
	clojure -M:fmt-check

fmt-fix: ## Fix code formatting with cljfmt
	clojure -M:fmt-fix

lint: ## Lint source code with clj-kondo
	clojure -M:lint

# ── Build ─────────────────────────────────────────────────────────────────────

build: ## Build JAR (set APP_VERSION env var for versioned builds)
	clojure -T:build jar

clean: ## Remove build artifacts
	rm -rf target

# ── Setup ─────────────────────────────────────────────────────────────────────

deps: ## Download all dependencies
	clojure -P
	clojure -P -M:test
	clojure -P -M:fmt-check
	clojure -P -M:lint
	clojure -P -T:build

# ── Help ──────────────────────────────────────────────────────────────────────

help: ## Show this help message
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'
