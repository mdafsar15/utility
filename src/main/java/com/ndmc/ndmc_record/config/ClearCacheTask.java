package com.ndmc.ndmc_record.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ClearCacheTask {
    private static final Logger logger = LoggerFactory.getLogger(ClearCacheTask.class);
    @Autowired
    private CacheManager cacheManager;

    //@Scheduled(fixedRateString = "${clear.all.cache.fixed.rate}", initialDelayString = "${clear.all.cache.init.delay}") // reset cache every hr, with delay of 1hr after app start
    @Scheduled(cron = "${scheduler.cronCache}", zone = "Asia/Kolkata")
    public void reportCurrentTime() {
        logger.info("Clearing all cache, time: " + LocalDateTime.now());
        cacheManager.getCacheNames().parallelStream().forEach(name -> cacheManager.getCache(name).clear());
    }
}

