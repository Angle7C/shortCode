# Redirect Cache Flow

## Redirect Read Path

Short URL redirect now follows this read order:

1. Query local cache through `CacheService.getLocalRemoteCache(shortCode)`
2. If local cache misses, fall back to Redis through `CacheService`
3. If Redis also misses, query the database
4. If the database returns a mapping, write it back to local cache and Redis
5. If the database returns nothing, return `null`

## Redirect Response Behavior

- When a mapping is found, `ShortUrlController` returns `302 Found` and sets the `Location` header to the origin URL
- When no mapping is found, `ShortUrlController` returns `404 Not Found`

## Cache Access Rule

All cache operations in the redirect path must go through `CacheService`.

- `ShortUrlService` must not access local Caffeine cache directly
- `ShortUrlService` must not access Redis directly
- Cache writes after create or database fallback must use `CacheService`

## Utility Classes

The project now provides these utility classes under `com.atangle.shortcode.util`:

- `SnowflakeIdGenerator`: thread-safe 64-bit snowflake ID generator
- `Base62Codec`: Base62 encode/decode utility for `long` and `BigInteger`

## Create Short URL Path

Short URL creation now follows this order:

1. Generate a short code
2. Query the database by short code
3. If the short code already exists, write the existing mapping into local cache and Redis
4. If the short code does not exist, insert the new mapping into the database
5. After insert, write the new mapping into local cache and Redis
