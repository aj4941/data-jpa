package study.datajpa.dto;

import lombok.Data;
import study.datajpa.entity.Member;

@Data
public class MemberDto {

    private Long id;
    private String username;
    private String teamName;

    public MemberDto(Long id, String username, String teamName) {
        this.id = id;
        this.username = username;
        this.teamName = teamName;
    }

    // dto에서 필드로 Member(엔티티)를 사용하면 안되지만 매개변수로 엔티티를 넣어서 데이터를 사용할 수는 있음
    public MemberDto(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
    }
}
