package jpabook.jpashop.domain.item;

import jpabook.jpashop.controller.BookForm;
import jpabook.jpashop.service.BookUpdateDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("B")
@Getter @Setter
public class Book extends Item {
    private String author;
    private String isbn;

    /**
     * 내생각) 샘플로 정적 팩토리 메서드를 엔티티에 만들어봤는데
     * 엔티티가 웹계층 dto를 알 필요도 없고 코드가 지저분해지므로
     * BookForm 쪽에 Book Entity 나 서비스 계층으로 넘길 dto를 생성하는 팩토리 메서드를 만드는게 좋을 것 같다.
     */
    public static Book createBookFrom(BookForm form) {
        Book b = new Book();
        b.setId(form.getId());
        b.setName(form.getName());
        b.setPrice(form.getPrice());
        b.setStockQuantity(form.getStockQuantity());
        b.setAuthor(form.getAuthor());
        b.setIsbn(form.getIsbn());
        return b;
    }

    public void changeCommonValues(BookUpdateDto param) {
        setPrice(param.getPrice());
        setAuthor(param.getAuthor());
        setName(param.getName());
        setStockQuantity(param.getStockQuantity());
    }
}
