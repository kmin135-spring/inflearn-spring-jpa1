package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository iRepo;

    @Transactional
    public void saveItem(Item i) {
        iRepo.save(i);
    }

    /*
    업데이트방법1 : 변경감지
    트랜잭션내에서 영속화된 엔티티는 JPA가 더티체크 후 변경내용을
    트랜잭션 종료전에 반영해준다.
    -> 이게 JPA에서 더 권장되는 방법
     */
    @Transactional
    public void updateBook(Long id, BookUpdateDto param) {
        Book findBook = (Book)iRepo.findOne(id);

        // 실제 업데이트 메서드를 setter로 도배하면 안 된다. -> 실제로 DB를 갱신하겠다는건지 알기가 어렵기 때문
//        findBook.setPrice(param.getPrice());
//        findBook.setAuthor(param.getAuthor());
//        findBook.setName(param.getName());
//        findBook.setStockQuantity(param.getStockQuantity());

        // 아래 처럼 명확하게 엔티티의 상태를 변경하는 메서드로 정의하는게 좋다.
        // entity의 setter 는 private으로 두고 이런 의도가 내포된 비즈니스 메서드로만 상태를 변경해주는 것도 방법
        findBook.changeCommonValues(param);
    }

    public List<Item> findItems() {
        return iRepo.findAll();
    }

    // 강사님은 단순히 위임만 한다면 굳이 service에 두지 않고 controller에서 repo를 직접 호출해도 된다고 생각
    public Item findOne(Long itemId) {
        return iRepo.findOne(itemId);
    }
}
