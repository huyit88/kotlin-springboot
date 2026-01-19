# CHALLENGE.md — Topic 31: Deployment with Kubernetes (K8s)

> Scope reminder: **no Helm, no cloud-specific features**, no DB.
> Assume you already have a Docker image from Topic 30 (e.g. `demo-app:local`) that exposes:
>
> * `GET /api/ping`
> * `GET /actuator/health`
> * `GET /actuator/health/liveness`
> * `GET /actuator/health/readiness`

---

## Problem A — Deployment (stateless Spring Boot app)

### Requirement

Create a Kubernetes **Deployment** for your app.

* File: `k8s/deployment.yaml`
* Specs:

  * `apiVersion: apps/v1`
  * `kind: Deployment`
  * `metadata.name: demo-app`
  * `replicas: 2`
  * Container:

    * `image: demo-app:local`
    * `containerPort: 8080`
    * Environment variable:

      * `SPRING_PROFILES_ACTIVE=k8s`

### Acceptance criteria

* `kubectl apply -f deployment.yaml` succeeds.
* `kubectl get pods` shows **2 running pods**.
* Restarting a pod results in Kubernetes recreating it automatically.

### Suggested Import Path

*(N/A — YAML only)*

### Command to verify/run

```bash
kubectl apply -f k8s/deployment.yaml
kubectl get pods
kubectl delete pod <one-pod-name>
kubectl get pods
kubectl delete -f k8s/deployment.yaml
```

---

## Problem B — Service (stable networking + load balancing)

### Requirement

Expose the Deployment via a **Service**.

* File: `k8s/service.yaml`
* Specs:

  * `apiVersion: v1`
  * `kind: Service`
  * `metadata.name: demo-app`
  * `type: ClusterIP`
  * Selector must match Deployment labels.
  * Port mapping:

    * Service port `80`
    * Target port `8080`

### Acceptance criteria

* `kubectl get svc demo-app` shows a ClusterIP.
* Requests to the Service are **load-balanced** across both pods.

### Suggested Import Path

*(N/A — YAML only)*

### Command to verify/run

```bash
kubectl apply -f k8s/service.yaml
kubectl get svc demo-app

# Port-forward to test locally
kubectl port-forward svc/demo-app 8080:80
curl http://localhost:8080/api/ping

# Stop port-forward (Ctrl+C) and cleanup
kubectl delete -f k8s/service.yaml
```

---

## Problem C — Health Probes (liveness & readiness)

### Requirement

Add **health probes** to the Deployment container.

* Liveness probe:

  * HTTP GET `/actuator/health/liveness`
  * Initial delay: 10s
* Readiness probe:

  * HTTP GET `/actuator/health/readiness`
  * Initial delay: 5s

Update `k8s/deployment.yaml`.

### Acceptance criteria

* Pods transition from `NotReady` → `Ready`.
* If you simulate a failure (e.g., temporarily return 500), K8s restarts the pod (liveness).
* Traffic is only sent to **Ready** pods (readiness).

### Suggested Import Path

*(N/A — YAML only)*

### Command to verify/run

```bash
kubectl apply -f k8s/deployment.yaml
kubectl describe pod <pod-name>
kubectl get pods
```

---

## Problem D — Configuration via ConfigMap

### Requirement

Externalize configuration using a **ConfigMap**.

* File: `k8s/configmap.yaml`
* Contents:

  ```yaml
  SPRING_PROFILES_ACTIVE: k8s
  SERVER_PORT: "8080"
  ```
* Inject ConfigMap values as **environment variables** into the container.
* Remove hardcoded env vars from the Deployment spec.

### Acceptance criteria

* App still starts and listens on `8080`.
* Changing ConfigMap and reapplying it affects new pods.
* No rebuild of Docker image required for config changes.

### Suggested Import Path

*(N/A — YAML only)*

### Command to verify/run

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml

kubectl exec <pod-name> -- printenv | grep -E "SPRING|SERVER"
```

---

## Problem E — Resource Limits & Scaling

### Requirement

Add **resource requests & limits**, then scale.

* In container spec:

  ```yaml
  resources:
    requests:
      cpu: "100m"
      memory: "256Mi"
    limits:
      cpu: "500m"
      memory: "512Mi"
  ```
* Scale Deployment to **3 replicas**.

### Acceptance criteria

* `kubectl describe pod` shows CPU/memory requests & limits.
* `kubectl scale deployment demo-app --replicas=3` results in 3 running pods.
* Service continues to route traffic correctly.

### Suggested Import Path

*(N/A — YAML only)*

### Command to verify/run

```bash
kubectl apply -f k8s/deployment.yaml
kubectl scale deployment demo-app --replicas=3
kubectl get pods
```
