package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
/*
- JPA의 데이터변경은 트랜잭션 설정이 필수
- Spring에 이미 종속적인 코드이고 기능도 더 많으니 Spring의 @Transactional 사용
- 클래스 전체는 readOnly = true로 잡아두고 데이터변경이 필요한 메서드에는 별도로 @Transactional 을 걸자.
  - 물론 변경이 많은 클래스라면 반대로 해도 된다.
- 읽기용으로 세팅해두면 일반적으로 성능이 더 최적화된다. (더티체크? 등 불필요한 동작을 생략한다고함)
  - 최적화 정도는 DBMS 벤더에 따라 다를 수 있음.
 */
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository mRepo;

    /**
    * 회원 가입
     *
     * 클래스 트랜잭션이 readOnly=true로 되어있어도
     * 데이터변경 메서드에 false (기본값) 으로 다시걸면 이게 우선권을 가진다.
    */
    @Transactional
    public Long join(Member m) {
        validateDuplicateMember(m);
        mRepo.save(m);
        return m.getId();
    }

    /** 중복 회원 검증 */
    private void validateDuplicateMember(Member m) {
        /*
        멀티쓰레드, 2개 이상의 was등의 환경을 고려하면 같은 이름의 체크가 동시에 이 지점에 도달할 경우 실제로는 중복 이름으로 가입이 발생할 수 있다.
        따라서 DB 레벨로 unique 제약조건을 부여하여 최종방어를 할 필요가 있다.
         */
        if(mRepo.findByName(m.getName()).size() > 0) {
            throw new IllegalStateException("회원 중복 발생");
        }
    }

    /**
     * 회원 전체 조회
     * */
    @Transactional(readOnly = true)
    public List<Member> findMembers() {
        return mRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Member findOne(Long memberId) {
        return mRepo.findOne(memberId);
    }
}
