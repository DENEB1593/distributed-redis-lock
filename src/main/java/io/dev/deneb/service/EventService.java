package io.dev.deneb.service;

import io.dev.deneb.model.Event;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);
    private final RedissonClient redissonClient;

    public EventService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 이벤트 참석을 처리한다.
     */
    public void join(String key) {

        String currentThread = Thread.currentThread().getName();
        RLock lock = redissonClient.getLock("event_lock");

        try {
            if (!lock.tryLock(2, 3, TimeUnit.SECONDS)) {
                log.warn("LOCK 획득에 실패하였습니다. [thread: {}]", currentThread);
                return;
            }

            // 현재 이벤트 참석 가능 여부를 파악한다.
            Event event = getEvent(key);
            if (event.getLimit() == event.getCurrent()) {
                log.info("이벤트 참석이 종료되었습니다. [정보: {}", event);
                return;
            }

            // 이벤트 참석 처리
            doJoin(key, event);
            log.info("이벤트 참석현황 : 이벤트: {}, thread: {}", event, currentThread);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }


    }

    public Event getEvent(String key) {
        return (Event) redissonClient.getBucket(key).get();
    }

    private void doJoin(String key, Event event) {
        event.join();
        redissonClient.getBucket(key).set(event);
    }

    public void initEvent(String key, Event event) {
        redissonClient.getBucket(key).set(event);
    }

    public void joinWithNoLock(String key) {
        String currentThread = Thread.currentThread().getName();

        Event event = getEvent(key);
        if (event.getLimit() == event.getCurrent()) {
            log.info("이벤트 참석이 종료되었습니다. [정보: {}", event);
            return;
        }

        doJoin(key, event);
        log.info("이벤트 참석현황 : 이벤트: {}, thread: {}", event, currentThread);
    }
}
