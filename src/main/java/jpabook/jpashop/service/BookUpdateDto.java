package jpabook.jpashop.service;

import jpabook.jpashop.controller.BookForm;
import jpabook.jpashop.domain.item.Book;
import lombok.Data;

@Data
public class BookUpdateDto {
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;

    private String author;

    // isbn은 업데이트할 수 없다고 가정
//    private String isbn;

    public static BookUpdateDto createBookUpdateDto(BookForm form) {
        BookUpdateDto b = new BookUpdateDto();
        b.setId(form.getId());
        b.setName(form.getName());
        b.setPrice(form.getPrice());
        b.setStockQuantity(form.getStockQuantity());
        b.setAuthor(form.getAuthor());

        //
//        b.setIsbn(form.getIsbn());
        return b;
    }
}
