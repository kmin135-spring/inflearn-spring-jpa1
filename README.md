# 개요

* 김영한 - 실전! 스프링 부트와 JPA 활용 1편 

# h2 데이터베이스 세팅

1. 직접다운받은 h2 를 실행 `h2/bin/h2.bat`
2. go to `http://localhost:8082/`
3. JDBC URL을 `jdbc:h2:~/jpashop` 로 입력 후 연결 (최초 1회)
   * `~/jpashop.mv.db` 가 생성되면 정상
4. 연결 해제 후 이후에는  `jdbc:h2:tcp://localhost/~/jpashop` 로 접속

# 메모

* N:M 은 운영에서는 쓰지 않는다. 1:N, N:1 로 풀어쓴다.
* 외래 키가 있는 곳을 연관관계의 주인으로 사용하라.