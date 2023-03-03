package study.datajpa.repository;

import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmMultiTenancyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;

    // 같은 트랜잭션이면 같은 엔티티 매니저를 사용하므로 memberRepository, teamRepository와 같이 사용
    @PersistenceContext EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");

        // Spring Data JPA가 entityManager도 다 넣어줌
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        // List<Member> top3HelloBy = memberRepository.findTop3HelloBy(); // Hello 필드가 없으므로 나오지 않음
        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void findUsernameList() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findMemberDto() {

        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();

        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findByNames() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void paging() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        // 페이지 시작은 0번부터 진행
        // size를 지정하면 한 페이지에 담긴 데이터 개수가 결정
        // 0번째 페이지에서 3개의 데이터를 조회한다는 의미 (2, 3 이라면 2페이지에서 3개의 데이터를 가져온다는 것)
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.Direction.DESC, "username");

        // when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        // then
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        for (Member member : content) {
            System.out.println("member = " + member);
        }

        // 5개 : size와 다르게 totalCount를 출력
        System.out.println("totalElements = " + totalElements);

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2); // 3 + 2 -> 2페이지 (0번, 1번)
        assertThat(page.isFirst()).isTrue(); // 현재 0페이지이므로 true
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는지 확인
    }

    @Test
    public void slicing() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        // slice는 3개를 가져온다고 해도 1개를 더 가져옴 (limit 4)
        // 다음 페이지가 있는지 없는지 알 수 있는 장점
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.Direction.DESC, "username");

        // when
        Slice<Member> page = memberRepository.findSliceByAge(age, pageRequest);

        // Member -> MemberDto (페이지를 유지하면서 엔티티를 dto로 변환)
        Slice<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        // then
        List<Member> content = page.getContent();

        for (Member member : content) {
            System.out.println("member = " + member);
        }

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.isFirst()).isTrue(); // 현재 0페이지이므로 true
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는지 확인
    }

    @Test
    public void bulkUpdate() {
        // given : 여기까지는 영속성 컨텍스트에 들어간 것이지 DB에 반영된 것은 아님
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        // when : DB에 들어가지 않은 상태에서 벌크연산 진행
        // 이때, jpql 쿼리 실행 전 쿼리와 관련있는 엔티티에 대한 flush()가 호출되어 save된 5개 데이터가 영속성 컨텍스트에서 DB로 반영
        int resultCount = memberRepository.bulkAgePlus(20);
        // DB에 10, 19, 21, 22, 41이 있는 상황 (영속성 컨텍스트에는 10, 19, 20, 21, 40)

        Member findMember = memberRepository.findByUsername("member5");

        // em.flush(); : 위에서 flush()가 실행되므로 넣지 않아도 OK, 넣더라도 영속성 컨텍스트에 변경감지가 일어나지 않았으므로 DB 반영 X
        em.clear(); // clear를 하지 않으면 find 할 때 1차 캐시에서 값을 가져오므로 1차 캐시를 초기화해야 DB에 있는 값을 가져올 수 있음

        // then
        assertThat(resultCount).isEqualTo(3);
//      assertThat(findMember.getAge()).isEqualTo(40);
        // 벌크 연산시에 반영이 되지 않은 상황
        // -> 벌크 연산을 하면 영속성 컨텍스트를 무시하고 바로 DB에 +1 을 처리해버림
        // 벌크 연산 이후에는 영속성 컨텍스트를 모두 초기화해야 함 (em.flush(), em.clear() 필요)

        assertThat(findMember.getAge()).isEqualTo(41);
    }
}