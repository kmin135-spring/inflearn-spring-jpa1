package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository oRepo;
    private final MemberRepository mRepo;
    private final ItemRepository iRepo;

    /**
     * 주문
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        // 엔티티 조회
        Member member = mRepo.findOne(memberId);
        Item item = iRepo.findOne(itemId);

        // 배송정보 생성 - 현실에서는 배송지 입력 시나리오가 있을 것이다.
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        // 주문상품 생성 - 현실에서는 다양한 할인정책 등이 적용될 수 있을 것이다.
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
        // 정적 팩토리 메서드만 쓰도록 강제했으므로 이 방법으로 객체를 생성할 수 없다! = 객체 생성 문제가 생기면 한 곳만 보면 된다!
//        OrderItem oi1 = new OrderItem();

        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        // 주문 저장
        // cascade = CascadeType.ALL 로 세팅해뒀기 때문에 orderItem, delivery 도 함께 persist 된다.
        oRepo.save(order);
        return order.getId();
    }

    /** 취소 */
    @Transactional
    public void cancelOrder(Long orderId) {
        // 주문 엔티티 조회
        Order order = oRepo.findOne(orderId);

        // 주문 취소
        order.cancel();

        /*
        JPA를 사용했기 때문에 order.cancel() 까지만 해주면
        JPA가 엔티티 상태 변경을 감지하고 이를 DB에 반영해준다.
        덕분에 코드가 간결하다.

        만약 mybatis 였다면 order.cancel()까지 한 뒤
        다시 서비스단에서 변경된 상태를 db에 반영해주는 코드를 별도로 작성해야한다.
         */
    }

    /**
     * 검색
     */
//    public List<Order> findOrders(OrderSearch orderSearch) {
//        return oRepo.findAll(orderSearch);
//    }
}
