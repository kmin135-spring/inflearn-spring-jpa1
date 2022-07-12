package jpabook.jpashop.controller;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class MemberForm {
    @NotEmpty(message = "회원 이름 필수")
    private String name;
    private String city;
    private String street;
    private String zipcode;
}
