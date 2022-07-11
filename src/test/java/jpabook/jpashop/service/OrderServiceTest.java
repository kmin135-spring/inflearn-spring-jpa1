package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;

// 통합테스트 - 실제로 이 부분은 단위테스트로 작성하는게 좋겠지만 여기서는 실제 동작까지를 보기 위해 통테로 작성
@SpringBootTest
@Transactional
class OrderServiceTest {
    @Autowired
    private OrderService oSvc;
    @Autowired private OrderRepository oRepo;
    @Autowired
    private EntityManager em;

    @Test
    public void orderItem() {
        // arrange
        Member m = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        // action
        Long orderId = oSvc.order(m.getId(), book.getId(), orderCount);

        // assert
        Order order = oRepo.findOne(orderId);
        assertThat(order.getStatus()).as("상품 주문시 상태는 ORDER").isEqualTo(OrderStatus.ORDER);
        assertThat(order.getOrderItems().size()).as("주문한 상품 종류 수가 정확해야한다.").isEqualTo(1);
        assertThat(order.getTotalPrice()).as("주문 가격은 가격 * 수량이다.").isEqualTo(10000 * orderCount);
        assertThat(book.getStockQuantity()).as("주문 수량만큼 재고가 줄어야한다.").isEqualTo(8);
    }

    @Test
    public void cannotOrderByExceedStock() {
        // arrange
        Member m = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        // action
        // assert
        assertThatThrownBy(() -> oSvc.order(m.getId(), book.getId(), orderCount))
                .isInstanceOf(NotEnoughStockException.class)
                .hasMessageContaining("need more stock");
    }

    @Test
    public void cancelOrder() {
        // arrange
        Member m = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        Long orderId = oSvc.order(m.getId(), book.getId(), orderCount);

        // action
        oSvc.cancelOrder(orderId);

        // assert
        Order canceledOrder = oRepo.findOne(orderId);
        assertThat(canceledOrder.getStatus()).as("주문 취소하면 상태는 CANCEL이다.").isEqualTo(OrderStatus.CANCEL);
        assertThat(book.getStockQuantity()).as("주문이 취소되면 그만큼 재고가 증가해야한다.").isEqualTo(10);
    }

    private Book createBook(String name, int price, int quantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(quantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member m = new Member();
        m.setName("회원1");
        m.setAddress(new Address("서울", "경기", "12345"));
        em.persist(m);
        return m;
    }
}