package jpabook.jpashop.service;

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

    public List<Item> findItems() {
        return iRepo.findAll();
    }

    // 강사님은 단순히 위임만 한다면 굳이 service에 두지 않고 controller에서 repo를 직접 호출해도 된다고 생각
    public Item findOne(Long itemId) {
        return iRepo.findOne(itemId);
    }
}
