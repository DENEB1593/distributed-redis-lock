package io.dev.deneb.model;

/**
 *  이벤트를 정의한다.
 */
public record Event(
        String name, // 이벤트명
        String id, // 이벤트 아이디
        Long limit, // 최대 참석 가능 수
        Long current // 현재 참석한 사용자

) {

}
