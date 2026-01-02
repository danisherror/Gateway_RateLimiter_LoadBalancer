

```docker-compose.yml
services:  
  redis:  
    image: redis:7  
    container_name: springshield_redis  
    ports:  
      - "6379:6379"  
    volumes:  
      - redis_data:/data  
  
volumes:  
  redis_data:
```

Perfect! If you want **Redis to run in the background**, you can use **detached mode** with Docker Compose. Here’s how:

---

### **1️⃣ Start Redis in detached mode**

```bash
docker-compose up -d redis
```

- `-d` = detached mode (runs in background)
    
- Only starts the `redis` service (ignores Postgres and Kafka)
    
- You can check running containers with:
    

```bash
docker ps
```

You should see `springshield_redis` with `STATUS Up …`

---

### **2️⃣ Verify Redis is running**

Use one of these commands:

**Option A: Ping Redis**

```bash
docker exec -it springshield_redis redis-cli ping
```

✅ Output should be:

```
PONG
```

**Option B: Check logs**

```bash
docker logs springshield_redis
```

Look for:

```
* Ready to accept connections
```

---

### **3️⃣ Stop Redis when needed**

```bash
docker-compose stop redis
```

Or remove it completely:

```bash
docker-compose down
```

---

If you want, I can give you a **one-liner command that starts Redis in background and immediately checks `PONG`** so you know it’s ready. This is really handy for scripts.

Do you want me to give that?