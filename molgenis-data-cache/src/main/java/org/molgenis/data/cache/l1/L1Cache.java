package org.molgenis.data.cache.l1;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.guava.CaffeinatedGuava;
import com.google.common.cache.Cache;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.Fetch;
import org.molgenis.data.cache.utils.CacheHit;
import org.molgenis.data.cache.utils.CombinedEntityCache;
import org.molgenis.data.cache.utils.EntityHydration;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.transaction.TransactionListener;
import org.molgenis.data.transaction.TransactionManager;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Caches entities within a transaction to speed up queries within those transactions. Each
 * transaction has its own cache. When the transaction is committed the cache is removed.
 */
@Component
public class L1Cache implements TransactionListener {
  private static final Logger LOG = getLogger(L1Cache.class);
  private static final int MAX_CACHE_SIZE = 25_000;
  private final ThreadLocal<CombinedEntityCache> caches;
  private final EntityHydration entityHydration;

  L1Cache(TransactionManager transactionManager, EntityHydration entityHydration) {
    caches = new ThreadLocal<>();
    this.entityHydration = requireNonNull(entityHydration);
    requireNonNull(transactionManager).addTransactionListener(this);
  }

  @Override
  public void transactionStarted(String transactionId) {
    LOG.trace("Creating L1 cache for transaction [{}]", transactionId);
    caches.set(createCache());
  }

  private CombinedEntityCache createCache() {
    Cache<EntityKey, CacheHit<Map<String, Object>>> cache =
        CaffeinatedGuava.build(Caffeine.newBuilder().maximumSize(MAX_CACHE_SIZE).recordStats());
    return new CombinedEntityCache(entityHydration, cache);
  }

  @Override
  public void doCleanupAfterCompletion(String transactionId) {
    CombinedEntityCache entityCache = caches.get();
    if (entityCache != null) {
      LOG.trace("Cleaning up L1 cache after transaction [{}]", transactionId);
      caches.remove();
    }
  }

  void putDeletion(Entity entity) {
    CombinedEntityCache entityCache = caches.get();
    if (entityCache != null) {
      EntityKey entityKey = EntityKey.create(entity.getEntityType().getId(), entity.getIdValue());
      entityCache.putDeletion(entityKey);
    }
  }

  void putDeletion(EntityType entityType, Object entityId) {
    CombinedEntityCache entityCache = caches.get();
    if (entityCache != null) {
      EntityKey entityKey = EntityKey.create(entityType.getId(), entityId);
      entityCache.putDeletion(entityKey);
    }
  }

  void evict(Entity entity) {
    CombinedEntityCache entityCache = caches.get();
    if (entityCache != null) {
      entityCache.evict(
          Stream.of(EntityKey.create(entity.getEntityType().getId(), entity.getIdValue())));
    }
  }

  // TODO: Call this also when repository metadata changes!
  void evictAll(EntityType entityType) {
    CombinedEntityCache entityCache = caches.get();
    if (entityCache != null) {
      LOG.trace("Removing all entities from L1 cache that belong to {}", entityType.getId());
      entityCache.evictAll(entityType);
    }
  }

  public Optional<CacheHit<Entity>> get(EntityType entityType, Object entityId) {
    return get(entityType, entityId, null);
  }

  /**
   * Retrieves an entity from the L1 cache based on a combination of entity name and entity id.
   *
   * @param entityType entity type
   * @param entityId id value of the entity to retrieve
   * @param fetch containing attributes to retrieve, can be null
   * @return an {@link Optional<CacheHit>} of which the CacheHit contains an {@link Entity} or is
   *     empty if deletion of this entity is stored in the cache, or Optional.empty() if there's no
   *     information available about this entity in the cache
   */
  public Optional<CacheHit<Entity>> get(
      EntityType entityType, Object entityId, @Nullable @CheckForNull Fetch fetch) {
    CombinedEntityCache cache = caches.get();
    if (cache == null) {
      return Optional.empty();
    }
    Optional<CacheHit<Entity>> result = cache.getIfPresent(entityType, entityId, fetch);

    if (LOG.isDebugEnabled()) {
      if (result.isPresent()) {
        LOG.debug(
            "Retrieved entity [{}] from L1 cache that belongs to {}", entityId, entityType.getId());
      } else if (LOG.isTraceEnabled()) {
        LOG.trace(
            "No entity with id [{}] present in L1 cache that belongs to {}",
            entityId,
            entityType.getId());
      }
    }

    return result;
  }

  /** Puts an entity into the L1 cache, if the cache exists for the current thread. */
  public void put(Entity entity) {
    CombinedEntityCache entityCache = caches.get();
    if (entityCache != null) {
      entityCache.put(entity);
      if (LOG.isTraceEnabled()) {
        LOG.trace(
            "Added dehydrated row [{}] from entity {} to the L1 cache",
            entity.getIdValue(),
            entity.getEntityType().getId());
      }
    }
  }
}
