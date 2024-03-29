package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepo;
    private final OrderQueryRepository orderQueryRepo;

    /**
     * entity를 직접 노출하는 안 좋은 API
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepo.findAllByCriteria(new OrderSearch());

        // LAZY 강제초기화
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                orderItem.getItem().getName();
            }
        }
        return all;
    }

    /**
     * Dto를 적용하여 종속성 제거
     * 하지만 모든 LAZY LOADING이 발생하여 N+1 문제 발생
     * 컬렉션이 있어 연쇄적으로 더 많이 발생
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> orderV2() {
        List<Order> orders = orderRepo.findAllByCriteria(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return collect;
    }

    /**
     * fetch join으로 성능 문제 해결
     * 하지만 페이징이 불가한 문제가 있음
     * (정확히는 성능상 하면 안 됨)
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> orderV3() {
        List<Order> orders = orderRepo.findAllWithIthem();
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return collect;
    }

    /**
     * findAllWithMemberDelivery 로는 1:1관계만 fetch join해서 얻어오고
     * 아래와 같이 batch size 글로벌 설정을 함으로써
     * 아래의 2개 쿼리로 컬렉션 데이터를 받아와서 성능 문제가 해결됨
     * 페이징도 가능
     *
     * 데이터 크기, 건수에 따라 batch size를 적절히 설정하면 높은 성능 유지 가능
     * + 추가 쿼리 2번이 발생하긴 하지만 딱 필요한 데이터만 얻어오기 때문에 DB와의 통신량이 최적화됨
     * + v3의 fetch join은 컬렉셔 때문에 중복 데이터를 얻어온 뒤 distinct 처리하는 것과 다름
     * -> 그래서 상황에 따라 v3.1이 쿼리는 더 나가도 오히려 v3보다 빠를 수도 있음
     *
     * <pre>
     * select -- 생략
     * from
     *   order_item orderitems0_
     * where
     *   orderitems0_.order_id in (4, 11);
     * </pre>
     * <pre>
     * select -- 생략
     * from
     *   item item0_
     * where
     *   item0_.item_id in (2, 3, 9, 10);
     * </pre>
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> orderV3_page(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "100") int limit) {
        List<Order> orders = orderRepo.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return collect;
    }

    /**
     * JPQL 레벨에서 바로 DTO로 조회
     *
     * 하지만 컬렉션을 N번에 걸쳐 쿼리하므로 N+1 발생
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepo.findOrderQueryDtos();
    }

    /**
     * in절을 사용하여 v4의 N+1 문제 해결
     *
     * v3.1의 단순 fetch join 대비 DTO 로 바로 얻기 위한 코드량이 많아짐
     * 대신 필요한 필드만 select하므로 트래픽이 줄어드는 효과가 있음 (트레이드 오프)
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepo.findAllByDto_optimization();
    }

    /**
     * 쿼리를 한 번만 날리는 방법
     *
     * 장점 :
     * - 쿼리 한 번에 컬렉션을 포함한 데이터를 모두 얻을 수 있음
     * - 조건에 따라 다르지만 보통은 가장 빠름
     *
     * 단점
     * 1. 쿼리는 1번이지만 조인으로 인해 중복 데이터를 얻어오므로 조건에 따라 더 느릴 수 있음
     * 2. 애플리케이션에서 중복을 제거하고 소팅하고 API 스펙에 맞춰 매핑하는등 추가작업이 많음
     *   - 아래 예제에서도 OrderFlatDto -> OrderQueryDto 로 변환하기 위해 복잡한 변경작업이 필요함
     * 3. 페이징이 불가능함 (1:N 조인하면서 데이터 양이 불어나니까)
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepo.findAllByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        // OrderItem도 엔티티므로 DTO로 변환해야한다.
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(io -> new OrderItemDto(io))
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem io) {
            itemName = io.getItem().getName();
            orderPrice = io.getOrderPrice();
            count = io.getCount();
        }
    }
}
