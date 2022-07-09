package jpabook.jpashop;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class Member {
    @Id @GeneratedValue
    private Long Id;
    private String username;
}
