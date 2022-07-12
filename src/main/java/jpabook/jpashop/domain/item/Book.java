package jpabook.jpashop.domain.item;

import jpabook.jpashop.controller.BookForm;
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
}
