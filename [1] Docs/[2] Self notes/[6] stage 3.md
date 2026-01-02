To **stop / close** a running `docker-compose up --build`, you have a few options depending on what you want.

---

## 🛑 Stop containers (most common)

If it’s running in the **foreground** (logs streaming):

### 👉 Press:

```text
CTRL + C
```

This:

- Stops all services
    
- Keeps containers, networks, and volumes
    

---

## 🧹 Stop & remove containers (clean shutdown)

From another terminal or after `CTRL + C`:

```bash
docker-compose down
```

This removes:

- Containers
    
- Default network
    

---

## 🔥 Stop & remove EVERYTHING (including Redis data)

⚠️ This deletes Redis data.

```bash
docker-compose down -v
```

---

## 🔍 Check running containers

```bash
docker ps
```

Stop manually if needed:

```bash
docker stop gateway-gateway-1 springshield_redis
```

---

## 🧠 When to use what

|Action|Command|
|---|---|
|Stop only|`CTRL + C`|
|Restart later|`docker-compose down`|
|Full reset|`docker-compose down -v`|

---

If you want **graceful shutdown logs in Spring Boot** or handling `SIGTERM` properly inside Docker, I can help you add that too.