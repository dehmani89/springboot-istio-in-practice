# http — IntelliJ HTTP Client requests

Open any `.http` file in IntelliJ and choose an environment from the toolbar:

| Environment | Targets |
|---|---|
| `local` | Services running on your machine (8080–8083, pricing v2 on 8093) |
| `mesh` | Istio ingress gateway at `http://localhost` (lab 03+). Only orders and products are exposed |

Files: `orders.http`, `catalog.http`, `internal.http` (inventory and pricing, local only).
