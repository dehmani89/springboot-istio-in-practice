# k8s — Workload Manifests

Plain Kubernetes manifests for the Dental OMS. **No Istio resources live here** — everything mesh-related is under `../istio`, so you can see exactly what the mesh adds on top of a vanilla deployment.

## Layout

| Path | Contents |
|---|---|
| `namespaces.yaml` | `dental` (apps) and `dental-data` (Postgres). Created **unenrolled**; Lab 02 adds the ambient label |
| `data/postgres.yaml` | Postgres 17 StatefulSet, Service (`tcp-postgres`), superuser Secret, 1Gi PVC |
| `apps/*.yaml` | Per service: ServiceAccount, DB Secret, Service (port 80 → `http`), Deployment |
| `tools/curl-client.yaml` | In-cluster curl pod with its own identity for authorization tests |

## Conventions that matter to Istio

| Convention | Why |
|---|---|
| One **ServiceAccount per service** | The SA is the workload's mTLS identity (`spiffe://cluster.local/ns/dental/sa/<name>`). AuthorizationPolicies match on it |
| Port names `http` / `tcp-postgres` | Explicit protocol selection; avoids relying on auto-detection |
| `app` + `version` labels | Used by Kiali graphs and version-specific Services |
| `pricing-service-v1` / `-v2` Services | Gateway API `HTTPRoute` splits traffic across **Services**, not subsets |
| HTTP probes on `/actuator/health/*` | Ambient mode exempts kubelet probes from mTLS enforcement; no probe rewriting needed |

## Deploy

Use the script (creates the `postgres-init` ConfigMap from `infra/postgres/init`, then applies everything in order):

```bash
./scripts/deploy.sh
```

Manual equivalent:

```bash
kubectl apply -f k8s/namespaces.yaml
kubectl create configmap postgres-init -n dental-data \
  --from-file=infra/postgres/init --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -f k8s/data/
kubectl rollout status statefulset/postgres -n dental-data
kubectl apply -f k8s/apps/ -f k8s/tools/
```

Images are referenced as `dental-oms/<service>:0.1.0` with `imagePullPolicy: IfNotPresent`. Docker Desktop's Kubernetes shares the local Docker image store, so `scripts/build-images.sh` is sufficient — no registry required.

## Resetting data

Postgres init SQL runs only on an empty volume:

```bash
kubectl delete statefulset postgres -n dental-data
kubectl delete pvc data-postgres-0 -n dental-data
./scripts/deploy.sh
kubectl rollout restart deploy -n dental   # Flyway re-seeds on startup
```
