package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
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
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
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
                    .collect(Collectors.toList());
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
