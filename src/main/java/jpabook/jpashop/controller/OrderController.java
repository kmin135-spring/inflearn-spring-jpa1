package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService oSvc;
    private final MemberService mSvc;
    private final ItemService iSvc;

    @GetMapping("/order")
    public String createForm(Model model) {
        List<Member> members = mSvc.findMembers();
        List<Item> items = iSvc.findItems();

        model.addAttribute("members", members);
        model.addAttribute("items", items);
        return "order/orderForm";
    }

    @PostMapping("/order")
    public String order(@RequestParam Long memberId,
                        @RequestParam Long itemId,
                        @RequestParam int count) {
        // 컨트롤러를 단순화하기 위해 id들과 필수값만 넘겨줬다.
        // 추가 장점은 서비스단에 핵심 비즈니스로직이 모두 담긴다.
        oSvc.order(memberId, itemId, count);
        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        List<Order> orders = oSvc.findOrders(orderSearch);
        model.addAttribute("orders", orders);

        return "order/orderList";
    }

    @PostMapping("/orders/{orderId}/cancel")
    private String orderCancel(@PathVariable Long orderId) {
        oSvc.cancelOrder(orderId);

        return "redirect:/orders";
    }
}
