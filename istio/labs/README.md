# Labs

Sequential, hands-on exercises. See the [lab table in the Istio README](../README.md#labs).

**Ground rules**

- Start from a clean deployment: `./scripts/deploy.sh` after `./scripts/istio-install.sh`.
- Each lab assumes the previous ones are applied unless its README states otherwise.
- `./scripts/smoke-test.sh` should pass at the end of every lab (except where a lab deliberately breaks something).
- Shell variables used throughout:

```bash
export GW=http://localhost          # ingress gateway (from lab 03)
alias kc='kubectl -n dental'
alias curlc='kubectl exec -n dental deploy/curl-client -- curl -s'
```
