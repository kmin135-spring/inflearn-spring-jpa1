package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    // 동적 쿼리를 어떻게 해결할까?
    public List<Order> findAll(OrderSearch orderSearch) {
        /* status, name이 없으면 조건에서 빠져야한다!
        em.createQuery("select o from Order o join o.member m"
                                + " where o.status = :status "
                                + " and m.name like :name",
                        Order.class)
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("name", orderSearch.getMemberName())
                //.setFirstResult(0) // 페이징에 사용
                .setMaxResults(1000)
                .getResultList();
         */

        /*
        첫번째 방법 : if~else 로 노가다로 만들까?
        -> 코드가 엄청나게 장황해지고 버그 양산 NO!!

        두번째 방법 : JPA Criteria - 동적으로 JPQL 을 만들어주는 표준 기술
        -> if~else 보다는 좋지만 여전히 가독성이 너무 낮아 유지보수 HELL
        -> 그래서 실무에서 안 씀

        결론 : QueryDSL 로 짠다.
        -> 가독성이 높음 -> 유지보수성 용이함
         */
        return null;
    }

    /*
     JPA Criteria를 이용한 동적쿼리
     어떤 쿼리가 수행될지 전혀 머리에 그려지지 않는다.
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }
}
