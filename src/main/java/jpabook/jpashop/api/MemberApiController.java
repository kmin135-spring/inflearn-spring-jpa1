package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberSvc;

    /**
     * Entity로 파라미터로 받지 마라!! API 스펙이 엔티티에 종속됨
     * 편해보이지만 큰 장애를 맞을 것이다!
     * */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long joinId = memberSvc.join(member);
        return new CreateMemberResponse(joinId);
    }

    /** 웹계층 파라미터(요청, 응답 모두)와 Entity 분리하여 종속성 제거 */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest reqMember) {
        Member member = new Member();
        member.setName(reqMember.getName());
        Long joinId = memberSvc.join(member);
        return new CreateMemberResponse(joinId);
    }

    /** 생성과 수정은 범위가 다르므로 dto도 분리하는게 좋다 */
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable Long id,
            @RequestBody @Valid UpdateMemberRequest reqMember) {
        memberSvc.update(id, reqMember.getName());
        Member member = memberSvc.findOne(id);
        return new UpdateMemberResponse(member.getId(), member.getName());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class UpdateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class CreateMemberResponse {
        private Long id;
    }
}
