
---
```
docker run -d \

-p 8080:8080 \

-e BACKEND_URLS=http://host.docker.internal:8081,http://host.docker.internal:8082,http://host.docker.internal:8083,http://host.docker.internal:8082 \

-e FRONTEND_URL=http://localhost:3000 \

--name gateway \

generic-gateway
```

Here, it will check if the given url is healthy or not if not then don't send request to server and after some time send request to healthy server onlt

