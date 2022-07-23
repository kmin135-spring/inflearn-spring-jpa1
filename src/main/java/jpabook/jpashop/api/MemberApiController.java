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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberSvc;

    /**
     * 엔티티를 그대로 리턴하면 불필요한 정보까지 노출되버림. (Order 등)
     * 엔티티와 프레젠테이션 계층이 상호종속됨
     *
     * @JsonIgnore 로 써서 빼는 방법도 있긴한데
     * 해당 필드가 필요한 API를 추가로 만들려면 다시 문제가 생기게되고
     * 무엇보다도 엔티티에 프레젠테이션 계층을 위한 불필요한 종속성이 생기게 됨 -> 하지마라
     *
     * 또한 API 응답은 루트가 객체로 시작해야한다.
     * 그래야 API에 추가 속성을 추가하기 용이하다.
     * v1은 json 루트가 배열이라 기능추가가 어렵다. (cnt 속성을 추가한다거나)
     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberSvc.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberSvc.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

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
