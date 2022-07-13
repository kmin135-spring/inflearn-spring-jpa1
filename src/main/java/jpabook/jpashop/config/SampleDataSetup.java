package jpabook.jpashop.config;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
@RequiredArgsConstructor
public class SampleDataSetup implements CommandLineRunner {
    private final ItemService iSvc;
    private final MemberService mSvc;

    @Override
    public void run(String... args) throws Exception {
        Book sampleBook = new Book();
        sampleBook.setStockQuantity(2);
        sampleBook.setAuthor("a1");
        sampleBook.setName("n1");
        sampleBook.setIsbn("isbn1");
        sampleBook.setPrice(1000);
        iSvc.saveItem(sampleBook);

        Member sampleMember = new Member();
        sampleMember.setName("user1");
        sampleMember.setAddress(new Address("c1", "s1", "z1"));
        mSvc.join(sampleMember);
    }
}
