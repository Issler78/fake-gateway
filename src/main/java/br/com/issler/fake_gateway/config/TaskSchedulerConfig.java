package br.com.issler.fake_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newScheduledThreadPool;

@Configuration
public class TaskSchedulerConfig {
    @Bean
    public ConcurrentTaskScheduler taskScheduler() {
        ScheduledExecutorService localThreads = newScheduledThreadPool(5);

        return new ConcurrentTaskScheduler(localThreads);
    }
}
