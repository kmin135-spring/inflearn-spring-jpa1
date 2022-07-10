package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

// 통합테스트
@SpringBootTest
// 테스트에 @Transactional을 걸어두면 기본적으로 각 테스트 종료시점에 롤백한다.
@Transactional
class MemberServiceTest {
    @Autowired MemberService mSvc;
    @Autowired MemberRepository mRepo;
    @Autowired EntityManager em;

    /*
    테스트에 @Transactional을 걸어놨으므로 끝나는시점에 rollback이 발생한다.
    그래서 DBMS마다 다르긴하나 h2에서는 insert문이 아예 발생하지 않는 것을 볼 수 있는데
    JPA 영속성 컨텍스트에만 들어갔을뿐 실제 commit이 발생하지 않았기 때문이다.
    @Rollback(value = false) 으로 실제 반영되게 하는 방법이 있고
    롤백 정책을 유지하면서 반영 쿼리는 보고싶다면 EntityManager 를 직접 flush 해주는 방법이 있다.
     */
    @Test
//    @Rollback(value = false)
    public void memberNormalJoin() {
        // arrange
        Member m = new Member();
        m.setName("kwon");
        
        // action
        Long savedId = mSvc.join(m);

        // assert

        // em을 직접 flush 하기 때문에 insert문이 실제로 수행된다.
        em.flush();
        Assertions.assertThat(mSvc.findOne(savedId)).isEqualTo(m);
    }
    
    @Test
    public void cannotJoinSameName() {
        // arrange
        Member m1 = new Member();
        m1.setName("kwon");

        Member m2 = new Member();
        m2.setName("kwon");

        // action
        mSvc.join(m1);

        // assert
        assertThrows(IllegalStateException.class, () -> mSvc.join(m2));
    }
}