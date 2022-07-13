package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.BookUpdateDto;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService iSvc;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {
        Book b = Book.createBookFrom(form);

        iSvc.saveItem(b);
        return "redirect:/items";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = iSvc.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    /**
     * 수정은 병합보다는 변경감지를 쓰자
     */
    @GetMapping("/items/{id}/edit")
    public String updateItemForm(@PathVariable("id") Long id, Model model) {
        Book item = (Book) iSvc.findOne(id);

        // 준영속 엔티티
        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form", form);
        return "items/updateItemForm";
    }

    /*
    * -강의에서는 @ModelAttribute 를 달아주는데 없어도 동작한다.
    *
    * 검증관련 내의견)
    * 1. 컨트롤러에서는 단순히 값 양식에 대한 검증만 해주자. (notnull, 길이, 유효한 url, 전화번호 등등)
    * 2. 서비스단에서는 권한 검증 (수정 권한이 있는가? 등), 백엔드의 추가 데이터를 조회해야 할 수 있는 검증 (선착순 등) 을 해주자.
    */
    @PostMapping("/items/{id}/edit")
    public String updateItem(BookForm form) {
        BookUpdateDto bookUpdateDto = BookUpdateDto.createBookUpdateDto(form);

//        iSvc.saveItem(book);

        // form을 그대로 전달하기 보다는 업데이트가 가능할 값만 서비스계층으로 전달
        // 파라미터가 많다면 서비스계층용 dto를 정의하여 넘기는 것도 고려
        // 중요한 것은 실제로 업데이트할 값만 담아서 전달하는 것
        iSvc.updateBook(bookUpdateDto.getId(), bookUpdateDto);
        return "redirect:/items";
    }
}
