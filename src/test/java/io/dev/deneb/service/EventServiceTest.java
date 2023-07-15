package io.dev.deneb.service;

import io.dev.deneb.model.Event;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventServiceTest {

    @Autowired
    EventService eventService;

    String key;
    Event event;

    @BeforeEach
    void setUp() {
        String eventName = "my_event";
        key = "1234";
        event = new Event(eventName, key, 1000L, 1000L);

        eventService.initEvent(key, event);
    }

    @Order(1)
    @Test
    void 이벤트참석_1000명_진행() throws InterruptedException {
        int joiner = 1000;
        CountDownLatch joinCounter = new CountDownLatch(joiner);
        List<Thread> workers = Stream
                .generate(() -> new Thread(new EventWorker(key, joinCounter)))
                .limit(joiner)
                .toList();

        workers.forEach(Thread::start);
        joinCounter.await();

        Event currentEvent = eventService.getEvent(key);
        Assertions.assertEquals(0, currentEvent.getCurrent());
    }

    @Order(2)
    @Test
    void 이벤트참석_1000명_진행_동시성제어X() throws InterruptedException {
        int joiner = 1_000;
        CountDownLatch joinCounter = new CountDownLatch(joiner);
        List<Thread> workers = Stream
                .generate(() -> new Thread(new EventWorkerWithNoLock(key, joinCounter)))
                .limit(joiner)
                .toList();

        workers.forEach(Thread::start);
        joinCounter.await();

        Event currentEvent = eventService.getEvent(key);
        Assertions.assertEquals(0, currentEvent.getCurrent());
    }

    // 멀티스레딩용 작업 정의
    class EventWorker implements Runnable{

        private String key;
        private CountDownLatch countDownLatch;

        public EventWorker(String key, CountDownLatch countDownLatch) {
            this.key = key;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            eventService.join(key);
            countDownLatch.countDown();
        }
    }


    private class EventWorkerWithNoLock implements Runnable {

        private String key;
        private CountDownLatch countDownLatch;

        public EventWorkerWithNoLock(String key, CountDownLatch countDownLatch) {
            this.key = key;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            eventService.joinWithNoLock(key);
            countDownLatch.countDown();
        }
    }

}