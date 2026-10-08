# Convenience targets. Each wraps a script in ./scripts.
.PHONY: build images local-db local-db-down istio deploy smoke smoke-local traffic teardown teardown-all

build:          ## Compile and run unit tests
	mvn verify

images:         ## Build jars + Docker images
	./scripts/build-images.sh

local-db:       ## Start local Postgres (docker compose)
	docker compose -f infra/docker-compose.yml up -d

local-db-down:  ## Stop local Postgres and delete its volume
	docker compose -f infra/docker-compose.yml down -v

istio:          ## Install Gateway API CRDs + Istio ambient + add-ons
	./scripts/istio-install.sh

deploy:         ## Deploy Postgres + services to Kubernetes
	./scripts/deploy.sh

smoke:          ## Smoke test via ingress gateway
	./scripts/smoke-test.sh

smoke-local:    ## Smoke test against locally running services
	./scripts/smoke-test.sh local

traffic:        ## Generate 100 orders through the gateway
	./scripts/generate-traffic.sh 100

teardown:       ## Delete app namespaces
	./scripts/teardown.sh

teardown-all:   ## Delete app namespaces and uninstall Istio
	./scripts/teardown.sh --istio
