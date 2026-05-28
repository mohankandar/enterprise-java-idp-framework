package io.github.mohankandar.idp.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class DemoCacheService {

  private static final Logger log = LoggerFactory.getLogger(DemoCacheService.class);

  @Cacheable(cacheNames = "cacheProof", key = "#k")
  public String slow(String k) {

    log.info("Executing slow() for key={}", k);

    try {
      Thread.sleep(1500); // simulate expensive work
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    return "value-" + k + "-" + System.currentTimeMillis();
  }
}