package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {
    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // cascade 를 설정해두면 order만 저장하면 orderItem 컬렉션이 함께 저장된다. (delete 도 마찬가지)
    // cascade 를 설정하지 않으면 기본은 모든 Entity를 개별적으로 persist 해야함.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    // 1:1은 FK를 아무곳이나 세팅가능하지만 자주 쓰이는 쪽에 두는게 좋다.
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;

    // 기본값이 ORDINAL 이므로 꼭 STRING 으로 세팅하자
    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER, CANCEL]

    //==연관관계 편의 메서드 :
    // - 양방향의 연관관계를 원자적으로 세팅해주는 메서드 (실수를 줄여줌)
    // - 연관관계 편의 메서드는 핵심적으로 컨트롤하는쪽에 만들어두면 좋다.
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }
}
