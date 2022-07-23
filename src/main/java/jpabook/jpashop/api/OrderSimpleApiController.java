package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * xToOne 관계에 대한 api
 *
 * order
 * order -> member
 * order -> delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepo;

    /**
     * 첫번째 문제 : 무한 참조 발생 - StackOverflowError 발생!
     * order -> member
     * member -> order 무한 참조 발생
     *
     * @JsonIgnore 로 끊어줘야한다.
     *
     * 두번째 문제 : LAZY 로 불러오는 프록시 엔티티 문제
     * @JsonIgnore를 넣어줘도
     * LAZY 인 연관관계는 프록시 엔티티들이 들어있으니 (ByteBuddyInterceptor)
     * Jackson이 json으로 만들다가 프록시 엔티티들을 만나 처리불능에 빠져 에러발생
     * 기본은 jackson이 hibernate의 엔티티 프록시들을 알지 못하기 때문
     *
     * 이 문제는 jackson-datatype-hibernate5 같은
     * 특화 라이브러리를 받고 Hibernate5Module bean을 만들어서 해결해야한다.
     * LAZY가 비어있으면 null로 해주기도하고
     * 강제로 LAZY 로딩을 수행해서 전체 결과를 얻을 수 있게해줄 수도 있다.
     * (당연히 불필요한 LAZY 연관관계까지 강제로 긁어오므로 성능문제도 발생)
     *
     * 전체 강제 LAZY 로딩 대신
     * order.getMember().getName() 이런식으로
     * 필요한 연관관계의 명시적인 LAZY 강제 초기화도 가능하긴 하다.
     *
     * 결론 : 엔티티를 API에 노출하지 마라
     * 참고 : Hibernate5Module 자체는 필요에 따라 쓸수는 있다고 함.
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepo.findAllByCriteria(new OrderSearch());
        return all;
    }
}
