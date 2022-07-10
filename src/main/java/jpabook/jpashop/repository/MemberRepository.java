package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    /*
    그냥 스프링에서 JPA의 em의 경우 @PersistenceContext 로 inject 하곤 하는데
    SpringBoot에서는 @Autowired 로 해도 자동으로 DI해준다.
    따라서 이와 같이 final로 잡고 생성자를 세팅해주는것으로 일관된 구조를 가질 수 있다.
    (* 강의에서는(=20년기준) 기본 스프링도 향후에는 이게 지원될수 있다고 하던데 어떨지 모르겠음.)
     */
//    @PersistenceContext
    private final EntityManager em;

    public void save(Member m) {
        em.persist(m);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
