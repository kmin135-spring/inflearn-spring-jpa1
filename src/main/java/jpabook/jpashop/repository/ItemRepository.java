package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {
    private final EntityManager em;

    public void save(Item i) {
        if(i.getId() == null) {
            em.persist(i);
        } else {
            /*
            업데이트방법2 : 병합
            - 파라미터로 넘긴 i는 준영속이다.
            - merge의 결과인 mergeItem 가 영속상태
            - merge의 동작방식은 i를 그대로 DB에 일괄반영해버린다.
            - 그래서 만약 i에 업데이트할 필드값만 세팅해놓고 나머지는 null로 두면 그대로 db에 반영되버린다.
              -> 실수할 여지가 많아짐
            - 반면 변경감지는 명시적으로 영속 엔티티에서 변경할 값을 바꿔주고 이를 반영해주기때문에 실주할 가능성이 작다.
            - 병합은 왠만하면 안 쓴다고 생각하자.
             */
            Item mergeItem = em.merge(i);

        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
