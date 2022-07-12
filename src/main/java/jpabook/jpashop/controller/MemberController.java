package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService mSvc;

    @GetMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    /*
    컨트롤러단에서는 MemberForm 등의 독립적인 DTO 로 받고
    이 dto에서 엔티티를 생성하여 서비스단에서 넘겨주는게 맞다.
    또는 서비스까지 dto로 넘기고 서비스에서 엔티티를 생성할 수도 있다.
    이건 요청만이 아니라 응답도 마찬가지다.

    중요한 것은 화면과 컨트롤러단에서 오가는 데이터와
    entity는 유사해보이지만 복잡도가 높아짐에 따라 필드도 달라지고 표시범위도 달라지므로
    분리해서 사용해야한다.

    만약 같이 쓰게되면 뷰단의 요구사항에 따라 entity 객체가 복잡해진다.
    -> 유지보수HELL
    -> 뷰를 고쳤더니 entity가 깨진다거나 entity를 고쳤더니 화면이 깨진다거나
    -> entity 객체는 JPA에 맞게 최대한 순수하게 유지해야한다.

    특히 API 일 때는 노출되는 데이터 자체가 API 스펙이므로
    API 일때는 무조건 전용 DTO로 변환해서 리턴해야한다.
     */
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult validResult) {
        if (validResult.hasErrors()) {
            return "members/createMemberForm";
        }

        // 예제로 생성자로 만들었지만 실무에서는 엔티티에 정적 팩토리 메소드를 도입하자
        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member m = new Member();
        m.setName(form.getName());
        m.setAddress(address);

        mSvc.join(m);
        return "redirect:/";
    }

    /*
    여기서는 Member 엔티티를 그대로 반환했다.
    서버 렌더링에서는 그나마 필요한 데이터만 선택해서 노출되므로
    아주 간단한 기능일때는 이런 방식도 고려할 수 있다.
    하지만 단 1개라도 뷰와 엔티티의 차이가 생긴다면 바로 분리해야한다.

    -> 근데 이것도 혼자할때나 지킬 수 있지 개발자가 많아지만 중구난방되기 딱 좋으므로
    아예 무조건 분리로 규칙을 정하는 것도 고려해야겠다.
     */
    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = mSvc.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }
}
