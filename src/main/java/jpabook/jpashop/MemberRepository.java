package jpabook.jpashop;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class MemberRepository {
    @PersistenceContext
    private EntityManager em;

    public Long save(Member m) {
        em.persist(m);
        return m.getId();
    }

    public Member find(Long id) {
        return em.find(Member.class, id);
    }
}
