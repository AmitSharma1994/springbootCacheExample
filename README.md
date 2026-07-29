# Spring Boot Cache Sharding with Redis

A Spring Boot application demonstrating **distributed cache sharding** across multiple Redis nodes with LRU eviction, backed by an H2 persistent database.

## Architecture

```
┌─────────────┐     ┌──────────────────┐     ┌────────────────────────┐
│  REST API   │────▶│  ProductService  │────▶│  ProductRepository     │
│  Controller │     │  + Cache Annot.  │     │  → H2 Database (file)  │
└─────────────┘     └───────┬──────────┘     └────────────────────────┘
                            │
                    ┌───────▼──────────┐
                    │ ShardCacheResolver│
                    │  id % 2 → shard  │
                    └───────┬──────────┘
                     ┌──────┴──────┐
                     ▼             ▼
              ┌────────────┐ ┌────────────┐
              │  Redis     │ │  Redis     │
              │  Shard 0   │ │  Shard 1   │
              │  :6379     │ │  :6380     │
              │  (≤5 keys) │ │  (≤5 keys) │
              └────────────┘ └────────────┘
```

## Tech Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Framework | Spring Boot 3.3.3 | Application framework |
| Language | Java 17 | Core language |
| Database | H2 (file-based) | Persistent storage |
| Cache | Redis (2 nodes) | Distributed caching with sharding |
| ORM | Spring Data JPA / Hibernate | Database access |
| Build | Maven (with wrapper) | Dependency management & build |
| Infrastructure | Docker Compose | Redis node orchestration |

## Caching Strategy

| Operation | Strategy | Annotation |
|-----------|----------|------------|
| Get by ID | **Cache-Aside** (lazy load) | `@Cacheable(cacheResolver = "shardCacheResolver")` |
| Get all | **Cache-Aside** (separate cache) | `@Cacheable(value = "products-all")` |
| Update | **Write-Through** | `@CachePut` + `@CacheEvict` |
| Delete | **Cache Invalidation** | `@CacheEvict(cacheResolver = "shardCacheResolver")` |

### How Sharding Works

- **Routing**: `id % 2` determines the shard — even IDs → Shard 0 (port 6379), odd IDs → Shard 1 (port 6380)
- **Eviction**: Each shard enforces a **5-key LRU limit** via `MaxKeysCache` decorator
- **Resolver**: A custom `ShardCacheResolver` implements Spring's `CacheResolver` interface to route cache operations at runtime

## Prerequisites

- **Java 17** (or higher)
- **Docker Desktop** (for Redis nodes)
- **Maven** (included via wrapper — `mvnw` / `mvnw.cmd`)

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/SiddhantRustagi/springbootCacheExample.git
cd springbootCacheExample
```

### 2. Start Redis nodes

```bash
docker-compose --profile infra up -d
```

This starts two Redis instances:
- **Shard 0**: `localhost:6379`
- **Shard 1**: `localhost:6380`

### 3. Run the application

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The app starts on **http://localhost:8089**

### 4. Access H2 Console (optional)

Open **http://localhost:8089/h2-console** and connect with:

| Field | Value |
|-------|-------|
| JDBC URL | `jdbc:h2:file:./data/productdb` |
| User | `sa` |
| Password | `sid@123` |

## Verifying Cache Sharding

### Inspect Redis keys per shard

```bash
# Shard 0 — should contain even ID keys
docker exec -it springbootcacheexample-redis-node-0-1 redis-cli KEYS "*"

# Shard 1 — should contain odd ID keys
docker exec -it springbootcacheexample-redis-node-1-1 redis-cli KEYS "*"
```

### Inspect a cached value (JSON format)

```bash
docker exec -it springbootcacheexample-redis-node-0-1 redis-cli GET "products-shard-0::2"
```

### Flush all caches

```bash
docker exec -it springbootcacheexample-redis-node-0-1 redis-cli FLUSHALL
docker exec -it springbootcacheexample-redis-node-1-1 redis-cli FLUSHALL
```

## Configuration

All configuration is in `src/main/resources/springCache-dev.yml`.

The application uses a custom config file name — configured via `SpringbootCacheExampleApplication.java`:

```java
app.setDefaultProperties(Map.of("spring.config.name", "springCache-dev"));
```

### Redis Shards

```yaml
redis:
  shards:
    - host: localhost
      port: 6379
    - host: localhost
      port: 6380
```

## Project Structure

```
src/main/java/com/redis/
├── SpringbootCacheExampleApplication.java   # Entry point + custom config name
├── config/
│   ├── RedisCacheConfig.java                # 2 connection factories + 2 cache managers
│   ├── ShardCacheResolver.java              # id % 2 routing logic
│   └── MaxKeysCache.java                    # LRU eviction decorator (5-key limit)
├── controller/
│   └── ProductController.java               # REST endpoints
├── entity/
│   └── Product.java                         # JPA entity (Serializable)
├── repository/
│   └── ProductRepository.java               # JpaRepository interface
└── service/
    └── ProductService.java                  # Business logic + cache annotations
```

## Docker

### Redis only (local development)

```bash
docker-compose --profile infra up -d
```

### Full stack (app + Redis)

```bash
docker-compose --profile deploy --profile infra up --build
```

## Stopping

```bash
# Stop Redis nodes
docker-compose --profile infra down

# Stop app — Ctrl+C in the terminal running mvnw
```
