# Lab 02 — Ambient Enrollment and mTLS

## Goal
Bring running workloads into the mesh with **a label and zero restarts**, then enforce strict mTLS.

## Steps

1. Note pod ages and restart counts:
   ```bash
   kubectl get pods -n dental -o wide
   ```
2. Enroll both namespaces:
   ```bash
   kubectl label namespace dental      istio.io/dataplane-mode=ambient
   kubectl label namespace dental-data istio.io/dataplane-mode=ambient
   ```
3. Verify enrollment:
   ```bash
   kubectl get pods -n dental            # same ages, 1/1, no restarts
   istioctl ztunnel-config workloads | grep dental     # PROTOCOL = HBONE
   ```
4. Generate traffic and watch ztunnel log the mTLS connections with identities:
   ```bash
   ./scripts/smoke-test.sh in-cluster
   kubectl logs -n istio-system ds/ztunnel --since=2m | grep -E 'src.identity|dst.identity' | tail -5
   ```
   Look for `src.identity="spiffe://cluster.local/ns/dental/sa/order-service"` and `dst.identity=...inventory-service`. That identity comes from the ServiceAccount.
5. The order-service → Postgres connection (`dental` → `dental-data`) is now mTLS too, transparently. JDBC is unaware.
6. Enforce STRICT mode (no plaintext fallback for non-mesh callers):
   ```bash
   kubectl apply -f istio/labs/02-ambient-enrollment-mtls/peer-authentication-strict.yaml
   ```
7. Prove plaintext is rejected using a pod **outside** the mesh:
   ```bash
   kubectl run outsider -n default --image=curlimages/curl:8.10.1 --restart=Never -- sleep 3600
   kubectl exec -n default outsider -- curl -s -m 3 http://catalog-service.dental/api/products \
     || echo "rejected (expected)"
   ```

## Expected results
- No pod restarts on enrollment.
- In-mesh traffic keeps working. The out-of-mesh pod fails once STRICT is applied.

## Takeaways
- Ambient decouples the mesh from the application lifecycle. Onboarding is a namespace label, not a redeploy.
- Identity = ServiceAccount. This is why every service has its own SA.
- Kubelet health probes still work: ambient handles probes without mTLS.

## Cleanup
`kubectl delete pod outsider -n default`. Keep the enrollment and STRICT policy for the next labs.
