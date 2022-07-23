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

    /** fetch join
     *
     * v3 재사용성이 높음
     *
     * <pre>
     * SELECT order0_.order_id       AS order_id1_6_0_,
     *        member1_.member_id     AS member_i1_4_1_,
     *        delivery2_.delivery_id AS delivery1_2_2_,
     *        order0_.delivery_id    AS delivery4_6_0_,
     *        order0_.member_id      AS member_i5_6_0_,
     *        order0_.order_date     AS order_da2_6_0_,
     *        order0_.status         AS status3_6_0_,
     *        member1_.city          AS city2_4_1_,
     *        member1_.street        AS street3_4_1_,
     *        member1_.zipcode       AS zipcode4_4_1_,
     *        member1_.name          AS name5_4_1_,
     *        delivery2_.city        AS city2_2_2_,
     *        delivery2_.street      AS street3_2_2_,
     *        delivery2_.zipcode     AS zipcode4_2_2_,
     *        delivery2_.status      AS status5_2_2_
     * FROM   orders order0_
     *        INNER JOIN member member1_
     *                ON order0_.member_id = member1_.member_id
     *        INNER JOIN delivery delivery2_
     *                ON order0_.delivery_id = delivery2_.delivery_id;
     * </pre>
     * */
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o " +
                        " join fetch o.member m " +
                        " join fetch o.delivery d ", Order.class
        ).getResultList();
    }

    /**
     * v4 필요한 필드만 골라서 쿼리
     *
     * 성능은 v3보다 아주 조금 좋음 (필요한 필드만 조회하니까)
     * - 하지만 v3와 join은 동일하므로 대세에 영향을 끼칠 정도의 성능차이가 나지 않는것이 대부분
     * - 물론 데이터가 큰 필드가 있다면 의미있는 성능차이가 날 수 있음
     *
     * 하지만 재사용성이 낮음
     * 특정한 DTO에 JPQL이 고정되버림
     * 또한 JQPL도 DTO 생성자로 더러워짐
     *
     * 또한 API스펙인 OrderSimpleQueryDto 가 JQPL에 등장해버림
     * 논리적으로 계층이 섞여버렸다고 볼 수 있음.
     * -> 이 문제는 쿼리전용 DTO롤 도입하여 완화할 수 있음
     *
     * v3와 v4는 trade-off가 있음
     *
     * <pre>
     * SELECT order0_.order_id   AS col_0_0_,
     *        member1_.name      AS col_1_0_,
     *        order0_.order_date AS col_2_0_,
     *        order0_.status     AS col_3_0_,
     *        delivery2_.city    AS col_4_0_,
     *        delivery2_.street  AS col_4_1_,
     *        delivery2_.zipcode AS col_4_2_
     * FROM   orders order0_
     *        INNER JOIN member member1_
     *                ON order0_.member_id = member1_.member_id
     *        INNER JOIN delivery delivery2_
     *                ON order0_.delivery_id = delivery2_.delivery_id;
     * </pre>
     */
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                                " from Order o " +
                                " join o.member m " +
                                " join o.delivery d ", OrderSimpleQueryDto.class)
                .getResultList();
    }
}
