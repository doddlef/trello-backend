package org.kevin.trello.config.cache

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@ConfigurationPropertiesScan(basePackageClasses = [CacheProperties::class])
@EnableCaching
class CacheConfig(
    private val cacheProperties: CacheProperties,
) {

    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager()

        val accountCache = CaffeineCache(
            "accounts", Caffeine.newBuilder()
                .maximumSize(cacheProperties.accountMaxSize)
                .expireAfterAccess(cacheProperties.accountLifeMinutes, TimeUnit.MINUTES)
                .build()
        )

        val boardViewCache = CaffeineCache(
            "boardViews", Caffeine.newBuilder()
                .maximumSize(cacheProperties.boardViewMaxSize)
                .expireAfterAccess(cacheProperties.boardViewLifeMinutes, TimeUnit.MINUTES)
                .build()
        )

        cacheManager.setCaches(listOf(
            accountCache, boardViewCache
        ))

        return cacheManager
    }
}