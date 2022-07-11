package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
public class Item {
    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany
    @JoinTable(name = "category_item",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private List<Category> categories = new ArrayList<>();

    //==비즈니스 로직==//
    // 도메인 주도 설계 관점에서 엔티티가 담을 수 있는 비즈니스 로직은 구현하는게 좋다.
    // stock은 setter로 값을 변경하는게 아니라 도메인 객체가 가진 비즈니스로직으로 변경한다.

    /** 재고 증가 */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /** 재고 감소 */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if(restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }
}