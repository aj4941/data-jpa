package study.datajpa.repository;

import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmMultiTenancyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
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
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;

    // 같은 트랜잭션이면 같은 엔티티 매니저를 사용하므로 memberRepository, teamRepository와 같이 사용
    @PersistenceContext EntityManager em;

    // 가져와서 써도 문제가 되지 않음 (사용자 정의 리포지토리 대신 사용)
    @Autowired MemberQueryRepository memberQueryRepository;

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

    @Test
    public void findMemberLazy() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush(); em.clear();

        // 연관관계가 있는 것을 조인으로 한 번에 가져오고 이 값들을 모두 select 절에 넣어줌 (객체 그래프)
        // 그냥 join을 하면 member 필드만 가져오는데 fetch join을 하면 team 필드까지 select 절에 넣어준다는 점이 특징
        List<Member> members = memberRepository.findMemberFetchJoin();
        // List<Member> members = memberRepository.findAll();
        // (상위의 findAll은 N + 1에 걸리지만 EntityGraph가 적용된 메서드는 문제없이 페치조인처럼 호출)

//        N + 1 문제 발생
//        for (Member member : members) {
//            System.out.println("member = " + member.getUsername());
//            System.out.println("team = " + member.getTeam().getClass()); // proxy
//            System.out.println("member.team = " + member.getTeam().getName()); // .getName() 하는 순간 실제 엔티티를 가져옴
//        }

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("team = " + member.getTeam().getClass()); // 실제 엔티티가 나옴
            System.out.println("member.team = " + member.getTeam().getName()); // 쿼리가 나가지 않음
        }
    }

    @Test
    public void quertHint() {
        Member member = memberRepository.save(new Member("member1", 10));
        em.flush(); // 영속성 컨텍스트에 member가 남고 DB에 반영
        em.clear(); // 영속성 컨텍스트 초기화

        // DB에서 가져오면서 1차 캐시에도 findMember이 들어감
        // Member findMember = memberRepository.findById(member.getId()).get();
        // 1차 캐시의 값을 변경하면 쓰기 지연 SQL에 이 정보가 들어감
//        findMember.setUsername("member2");

        // 변경 감지 (더티 체킹)가 발생하면서 DB에 update 쿼리 발생
//        em.flush();

        // 변경 감지 체크를 아예 하지 않음
        // 값이 변경되더라도 스냅샷을 찍지 않으므로 1차 캐시에서 DB로 반영이 되지 않음
        Member findMember = memberRepository.findReadOnlyByUsername(member.getUsername());
        findMember.setUsername("member2");
        em.flush();
    }

    @Test
    public void callCustom() {
        List<Member> result = memberRepository.findMemberCustom();
    }

    @Test
    public void JpaEventBaseEntity() throws Exception {
        Member member = new Member("member1");
        memberRepository.save(member); // 실행 전에 @PrePersist 호출

        Thread.sleep(100);
        member.setUsername("member2");

        em.flush(); // 실행 전에 @PreUpdate 호출
        em.clear();

        Member findMember = memberRepository.findById(member.getId()).get();

        System.out.println("findMember.create = " + findMember.getCreadtedDate());
        System.out.println("findMember.update = " + findMember.getLastModifiedDate());
        System.out.println("findMember.createdBy = " + findMember.getCreatedBy());
        System.out.println("findMember.updatedBy = " + findMember.getLastModifiedBy());
    }
}