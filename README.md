# System Design Labs

Hands-on system design exercises built with Java 21 and Spring Boot. Each lab implements a backend pattern and shows how to verify its behavior through API requests and application logs.

## Labs

| Lab | What it demonstrates |
| --- | --- |
| Redis cache-aside | Cache product reads for 25–35 seconds with `@Cacheable`. Compare a cache miss, a cache hit, and a read after expiration. |
| Redis cache invalidation | Update a product in PostgreSQL and remove its cached copy with `@CacheEvict`. The next read fetches and caches the updated product. (Lab 2) |
| Redis cache refresh | Update PostgreSQL and replace the cached product with `@CachePut`. The next read gets the updated value from Redis. (Lab 3, current implementation) |
| Redis cache stampede prevention | Use `@Cacheable(sync = true)` with a locking Redis writer. In a test with 10 concurrent requests for the same uncached product, PostgreSQL queries dropped from 10 to 1. |
| Redis negative caching | Cache missing products for 25–35 seconds. Repeat requests return `404` without another PostgreSQL query. |
| Redis TTL jitter | Vary the 30-second base TTL by up to five seconds in either direction. This spreads out expirations and helps reduce database load spikes. |

## Redis and PostgreSQL

PostgreSQL stores the product records. Redis stores temporary copies keyed by product ID. On a cache miss, Spring Boot uses `JdbcTemplate` to query PostgreSQL and caches the result in Redis. The current PUT flow uses `@CachePut` to store the updated product in Redis after PostgreSQL updates the row. Each cache write gets a fresh TTL of 25–35 seconds. A missing product returns `404 Not Found`.

## Run locally

Install Java 21 and start Docker Desktop. From the project root, start PostgreSQL and Redis, then launch Spring Boot.

```powershell
docker compose up -d
docker compose ps
.\mvnw.cmd spring-boot:run
```

The API runs at `http://localhost:8080`. On startup, Spring Boot creates the products table and seeds product `123` with the name **Mechanical Keyboard** on a fresh database.

## Try it in Bruno

Use `http://localhost:8080/products/123` for both requests. Send PUT with a JSON body such as `{"name":"Wireless Keyboard"}`. Choose a different name from the current one so the change is visible.

| Request | Expected result |
| --- | --- |
| GET with an empty cache | `200 OK` with the product. The log shows a cache miss and a SQL SELECT. |
| Repeat GET within 25 seconds | Same product with no new SQL SELECT. |
| PUT within 25 seconds of filling the cache | `204 No Content`. The app updates PostgreSQL and refreshes the Redis entry. |
| GET immediately after PUT | `200 OK` with the new name and no new SQL SELECT. |
| Repeat GET within 25 seconds | The updated product comes from Redis. |

To test expiration separately, wait more than 35 seconds after a cache fill, then send another GET. It should query PostgreSQL again.
