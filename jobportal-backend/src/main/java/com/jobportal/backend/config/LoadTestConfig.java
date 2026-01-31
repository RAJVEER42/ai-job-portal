package com.jobportal.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
@Profile("load-test")
@ConditionalOnProperty(name = "app.load-test.enabled", havingValue = "true")
public class LoadTestConfig {

    /**
     * REST Template for load testing
     */
    @Bean
    public RestTemplate loadTestRestTemplate() {
        return new RestTemplate();
    }

    /**
     * Load testing service for simulating traffic
     */
    @Bean
    public LoadTestService loadTestService(RestTemplate restTemplate) {
        return new LoadTestService(restTemplate);
    }

    /**
     * Load testing service implementation
     */
    public static class LoadTestService {
        private final RestTemplate restTemplate;
        private static final String BASE_URL = "http://localhost:8080";

        public LoadTestService(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        /**
         * Simulate job listing requests every 5 seconds
         */
        @Scheduled(fixedRate = 5000)
        public void simulateJobListingTraffic() {
            try {
                String url = BASE_URL + "/api/jobs?page=0&size=10";
                long startTime = System.currentTimeMillis();
                
                restTemplate.getForObject(url, String.class);
                
                long responseTime = System.currentTimeMillis() - startTime;
                log.info("Job listing request completed in {} ms", responseTime);
                
            } catch (Exception e) {
                log.warn("Job listing load test failed: {}", e.getMessage());
            }
        }

        /**
         * Simulate search requests every 10 seconds
         */
        @Scheduled(fixedRate = 10000)
        public void simulateSearchTraffic() {
            try {
                String[] keywords = {"Java", "Python", "React", "Angular", "Spring", "Node"};
                String keyword = keywords[(int) (Math.random() * keywords.length)];
                
                String url = BASE_URL + "/api/jobs/search?keyword=" + keyword;
                long startTime = System.currentTimeMillis();
                
                restTemplate.getForObject(url, String.class);
                
                long responseTime = System.currentTimeMillis() - startTime;
                log.info("Search request for '{}' completed in {} ms", keyword, responseTime);
                
            } catch (Exception e) {
                log.warn("Search load test failed: {}", e.getMessage());
            }
        }

        /**
         * Simulate concurrent user requests every 15 seconds
         */
        @Scheduled(fixedRate = 15000)
        public void simulateConcurrentTraffic() {
            try {
                // Simulate multiple concurrent requests
                for (int i = 0; i < 5; i++) {
                    new Thread(() -> {
                        try {
                            String url = BASE_URL + "/api/jobs?page=" + (int)(Math.random() * 3) + "&size=5";
                            long startTime = System.currentTimeMillis();
                            
                            restTemplate.getForObject(url, String.class);
                            
                            long responseTime = System.currentTimeMillis() - startTime;
                            log.debug("Concurrent request completed in {} ms", responseTime);
                            
                        } catch (Exception e) {
                            log.debug("Concurrent request failed: {}", e.getMessage());
                        }
                    }).start();
                }
                
            } catch (Exception e) {
                log.warn("Concurrent load test failed: {}", e.getMessage());
            }
        }
    }
}
