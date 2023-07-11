package io.dev.deneb.model;

import java.io.Serializable;

/**
 *  이벤트를 정의한다.
 */
public class Event implements Serializable {
    private String name;        // 이벤트명
    private String id;          // 이벤트 아이디
    private Long limit;         // 최대 참석 가능 수
    private Long current;       // 현재 참석한 사용자

    public Event(String name,
                 String id,
                 Long limit,
                 Long current) {
        this.name = name;
        this.id = id;
        this.limit = limit;
        this.current = current;
    }


    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Long getLimit() {
        return limit;
    }

    public Long getCurrent() {
        return current;
    }

    public void join(){
        if (current <= 0) {
            return;
        }
        this.current--;
    }


    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", limit=" + limit +
                ", current=" + current +
                '}';
    }
}
