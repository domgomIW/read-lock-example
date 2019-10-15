package com.example.redisson.demo;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    private Logger log = LoggerFactory.getLogger(DemoApplicationTests.class);
    private RedissonClient redisson;

    @Before
    public void init() {
        String address = "redis://127.0.0.1:6379";

        Config config = new Config();
        config.useSingleServer()
                .setAddress(address)
                .setTimeout(1000);

        log.info("Creating redis client pointing at {}", address);

		redisson = Redisson.create(config);
    }

    @Test
    public void test() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(10);
        RReadWriteLock rwlock = redisson.getMap("test").getReadWriteLock("test");
        RLock rlock = rwlock.readLock();

        List<Callable<Void>> callables = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            callables.add(() -> {
                for (int j = 0; j < 1000; j++) {
                    rlock.lock();
                    try {
                    } finally {
                        rlock.unlock();
                    }
                }
                return null;
            });
        }

        log.info("Invoking futures");
        List<Future<Void>> futures = service.invokeAll(callables);

        for (Future<Void> future : futures) {
            assertThatCode(future::get).doesNotThrowAnyException();
        }

        service.shutdown();

        assertThat(service.awaitTermination(30, TimeUnit.MINUTES)).isTrue();
    }

}
