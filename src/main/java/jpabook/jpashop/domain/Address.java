package jpabook.jpashop.domain;

import lombok.*;

import javax.persistence.Embeddable;

/*
값 타입은 불변으로 설계해야한다.
Setter는 아예 안 만들고 생성자만 만드는 방법이 좋다.

다만 JPA 는 기본 생성자를 만들어야하는데 public 또는 protected 로 세팅이 가능하므로
protected 로 기본 생성자를 만들어두면 무분별한 생성을 그나마 막을 수 있다.
이런 규칙이 있는건 JPA가 리플렉션을 사용하기 위한 제약조건이라 보면 됨.
 */
@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {
    private String city;
    private String street;
    private String zipcode;
}
