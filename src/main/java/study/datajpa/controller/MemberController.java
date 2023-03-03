package study.datajpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    // 위와 동일한 결과가 나옴 (권장 X)
//    @GetMapping("/members/{id}")
//    public String findMember2(@PathVariable("id") Member member) {
//        return member.getUsername();
//    }

    @GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size = 5, sort = "username") Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        // entity -> dto로 변환
        Page<MemberDto> map = page.map(member -> new MemberDto(member)); // MemberDto::new
        return map;
    }
    // localhost:8080/members?page=0 : 0페이지에서 20개의 데이터를 가져옴 (id = 1 ~ 20)
    // localhost:8080/members?page=1 : 1페이지에서 20개의 데이터를 가져옴 (id = 21 ~ 40)
    // localhost:8080/members?page=0&size=3 : 0페이지에서 3개의 데이터를 가져옴 (id = 1 ~ 3)
    // localhost:8080/members?page=0&size=3&sort=id,desc : (id : 100, 99, 98)
    // Spring Data JPA가 파라미터 바인딩이 될 때 매개변수로 Pageable이 있으면 PageRequest 객체를 생성하여 그걸 가지고 값을 채워서 injection
    // @PageableDefault(size = 5, sort = "username") : 기본 설정도 가능 (한 페이지 사이즈를 20개에서 5개로 수정)

    @PostConstruct
    public void init() {
        for (int i = 0; i < 100; i++) {
            memberRepository.save(new Member("user" + i, i));
        }
    }
}
