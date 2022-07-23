package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepoistory {
    private final EntityManager em;

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
     * -> 이 문제는 화면용 쿼리만 담당하는 repository 를 분리하여 완화할 수 있음.
     *
     * v3와 v4는 trade-off가 있음
     * 일단 v3로 하고 성능 등의 문제가 있을 때 v4를 고려하자
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
                        "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                                " from Order o " +
                                " join o.member m " +
                                " join o.delivery d ", OrderSimpleQueryDto.class)
                .getResultList();
    }
}
