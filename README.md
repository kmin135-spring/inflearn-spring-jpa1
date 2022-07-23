# 개요

* 김영한 - 실전! 스프링 부트와 JPA 활용 1편
* +2편도 이 레포지토리에서 이어서 진행

# h2 데이터베이스 세팅

1. 직접다운받은 h2 를 실행 `h2/bin/h2.bat`
2. go to `http://localhost:8082/`
3. JDBC URL을 `jdbc:h2:~/jpashop` 로 입력 후 연결 (최초 1회)
   * `~/jpashop.mv.db` 가 생성되면 정상
4. 연결 해제 후 이후에는  `jdbc:h2:tcp://localhost/~/jpashop` 로 접속

# 메모

* N:M 은 운영에서는 쓰지 않는다. 1:N, N:1 로 풀어쓴다. 
  * 중간 테이블에 대한 커스텀이 거의 안 되기 때문임.
* 외래 키가 있는 곳을 연관관계의 주인으로 사용하라.
* FK 를 잡을지는 시스템 특성에 따라 다르다.
  * 데이터 정합성을 어느정도 포기하고 실시간 트래픽에 강하게 하려면 안 잡고 인덱스만 잘 잡고 갈 수도 있고
  * 데이터 정합성이 가장 중요한 경우라면 `(ex. 돈)` FK를 쓰는 것이 좋을 수 있다.
* Setter 를 호출하면 데이터가 바뀐다. 이 때문에 Setter를 함부로 열어두면 데이터가 왜 변경된건지 추적이 점점 힘들어진다.
  * 그래도 엔티티를 변경할 때는 Setter 대신 변경 지점이 명확하도록 변경을 위한 비즈니스 메서드를 별도로 제공해야한다.
* Getter 는 사이드이펙이 딱히 없으니 다 열어둬도 된다.
* JPA가 직접 만드는 DDL을 운영에서 쓰면 안 되고 목적에 맞게 튜닝한 다음 직접 만들어야한다.
  * 단, DDL 초안 생성용으로는 좋다.

## 엔티티 설계 주의점

* 모든 연관관계는 지연로딩으로 설정해야한다.
  * 즉시로딩 `EAGER` 는 예측이 어렵고 어떤 SQL이 실행될지 추적이 어렵다. 또한 JPQL을 실행할 때 N+1 문제가 자주 발생한다.
  * 실무에서 모든 연관관계는 LAZY로 설정해야한다.
* 연관된 엔티티를 함께 조회해야한다면 fetch join 또는 엔티티 그래프 기능을 사용한다.
* @XToOne(OneToOne, ManyToOne) 은 기본이 EAGER이므로 명시적으로 LAZY로 설정하자.
  * @OneToMany 는 LAZY로 되어있으므로 혼동하면 안 됨.

## 컬렉션 초기화

* 컬렉션은 필드레벨에서 초기화해두는게 좋다.
  * NPE 에서 안전하다.

## 연관관계 세팅

* 코드상에서 객체간의 연관관계를 따로따로 잡아줘도 되지만
* 실수를 방지하기 위해 `연관관계 편의 메서드` 를 관계의 핵심 엔티티에 정의해두면 연관관계를 원자적으로 설정해줄 수 있어 실수를 막을 수 있다.


## cascade 설정에 대해

* private owner 일 때에 한해 사용하는게 안전하다.
* 이 예제프로그램에서 delivery, orderItem 모두 Order 에서만 쓰이기 때문에 cascade로 설정했다.
* persist 에 대한 라이프 사이클이 동등한 범위라면 설정하면 좋다.
* 하지만 비즈니시에 따라 devlivery, orderItem 을 다른 엔티티에서도 참조할 경우 Order 의 변경이 다른 곳으로 전파될 수 있으므로 함부로 cascade를 설정하면 안 된다.
* 애매하면 일단 쓰지 말고 각각 persist 하고 확신이 드는 경우에 한해서만 cascade를 사용하자.

## 도메인 모델 패턴 vs 트랜잭션 스크립트 패턴

* 엔티티가 비즈니스 로직의 대부분을 가지면 도메인 모델 패턴
  * http://martinfowler.com/eaaCatalog/domainModel.html
* 서비스가 비즈니스 로직의 대부분을 가지면 트랜잭션 스크립트 패턴이라 함 
  * http://martinfowler.com/eaaCatalog/transactionScript.html
* 장단이 있으므로 문맥에 따라 선택하면 됨
  * 한 프로젝트내에서도 혼용될 수 있음
* 도메인 모델 패턴의 장점 중 하나로 엔티티 자체에 비즈니스 로직이 있으므로 유닛테스트 작성이 쉽다.
  * 반면 트랜잭션 스크립트 패턴이라면 mocking 등이 필요하게된다.

# 여기서부터 활용 2편

## 메모

* 웹 계층에 절대 엔티티를 노출하지마라
  * 상호종속성이 생겨 한쪽의 변경이 사이드이펙을 발생시킨다.
  * 요청, 응답 모두 별도의 DTO로 분리해야한다.
  * 값 클래스 (Address) 는 노출해도 무방
* lombok 관련 주의사항
  * dto는 `@Data` 써도 무방
  * 엔티티는 `@Getter` 만 쓰고 나머지는 신중히 고려