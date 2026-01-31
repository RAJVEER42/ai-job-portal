package com.jobportal.backend.controller;

import com.jobportal.backend.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cache")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class CacheController {

    private final CacheManager cacheManager;

    /**
     * Get cache statistics for all caches
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCacheStats() {
        log.info("Fetching cache statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache caffeineCache) {
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = 
                    caffeineCache.getNativeCache();
                
                Map<String, Object> cacheStats = new HashMap<>();
                cacheStats.put("size", nativeCache.estimatedSize());
                cacheStats.put("hitCount", nativeCache.stats().hitCount());
                cacheStats.put("missCount", nativeCache.stats().missCount());
                cacheStats.put("hitRate", nativeCache.stats().hitRate());
                cacheStats.put("evictionCount", nativeCache.stats().evictionCount());
                
                stats.put(cacheName, cacheStats);
            }
        }
        
        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Cache statistics retrieved successfully")
                .data(stats)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Clear specific cache
     */
    @DeleteMapping("/{cacheName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> clearCache(@PathVariable String cacheName) {
        log.info("Clearing cache: {}", cacheName);
        
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .message("Cache '" + cacheName + "' cleared successfully")
                    .data("Cache cleared")
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(false)
                    .message("Cache '" + cacheName + "' not found")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Clear all caches
     */
    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> clearAllCaches() {
        log.info("Clearing all caches");
        
        int clearedCount = 0;
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                clearedCount++;
            }
        }
        
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message(clearedCount + " caches cleared successfully")
                .data("All caches cleared")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get cache names
     */
    @GetMapping("/names")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Iterable<String>>> getCacheNames() {
        log.info("Fetching cache names");
        
        ApiResponse<Iterable<String>> response = ApiResponse.<Iterable<String>>builder()
                .success(true)
                .message("Cache names retrieved successfully")
                .data(cacheManager.getCacheNames())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
