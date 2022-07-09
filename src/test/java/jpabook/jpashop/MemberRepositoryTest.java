package jpabook.jpashop;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberRepositoryTest {
    @Autowired MemberRepository mRepo;

    @Test
    @Transactional // 테스트에 걸려있으면 트랜잭션 종료 후 rollback
     @Rollback(value = false) // rollback을 비활성화
    public void testMember() {
        // arrange
        Member m = new Member();
        m.setUsername("memberA");

        // action
        Long saveId = mRepo.save(m);
        Member findMember = mRepo.find(saveId);

        // assert
        assertThat(findMember).isEqualTo(m);
        assertThat(findMember).isSameAs(m);
    }
}