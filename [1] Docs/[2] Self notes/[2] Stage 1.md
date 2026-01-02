

```
mvn clean package
```

You have this file (this is what Docker needs):

```
target/gateway-0.0.1-SNAPSHOT.jar
```

Now continue **exactly in this order** 👇

---

## 1️⃣ Create Dockerfile (confirm)

In the **gateway root folder** (`pom.xml` is here):

```bash
touch Dockerfile
```

Open it and paste **ONLY this**:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Save and close.

---

## 2️⃣ Build Docker Image

Run:

```bash
docker build -t generic-gateway .
```

You should see:

```
Successfully tagged generic-gateway:latest
```

---

## 3️⃣ Run Docker Container

Make sure **backend (8081)** is running first.

Then run:

```bash
docker run -d \
  -p 8080:8080 \
  -e GATEWAY_BACKEND_URL=http://host.docker.internal:8081 \
  -e ALLOWED_ORIGINS=http://localhost:3000 \
  --name gateway \
  generic-gateway
```

---

## 4️⃣ Verify Container

```bash
docker ps
```

You must see:

```
generic-gateway   Up ...
```

---

## 5️⃣ Test Gateway

In browser / Postman:

```
http://localhost:8080/api/hello
```

Expected response:

```
Hello from BACKEND
```

---

## 🚨 If it fails

Run:

```bash
docker logs gateway
```

Paste the **full output** here.

---


To stop your running Docker container named `gateway`, run:

```bash
docker stop gateway
```

If you also want to **remove it** after stopping:

```bash
docker rm gateway
```

✅ `stop` just stops it; `rm` deletes the container so you can start fresh.


---