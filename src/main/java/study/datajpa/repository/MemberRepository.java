package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
    // List<Member> findTop3HelloBy();

    // 쿼리가 길어질 때 유용 (오타를 치는 경우 오류가 발생)
    // 간단할 때는 위의 방식을 사용하고 쿼리가 길어지면 @Query 사용
    // 동적 쿼리는 Querydsl을 사용하는 것이 좋다.
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    // Querydsl로 편리하게 하는 방법이 존재
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    // @Param에 들어온 값이 :names에 들어감 (where in 쿼리 지원)
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    // 반환 타입을 유연하게 작성할 수 있음
    // 여기서 나온 List, Member, Optional은 시작을 대문자로 하고 임의로 적은 내용으로 메서드에 영향 X
    List<Member> findListByUsername(String username); // 컬렉션
    Member findMemberByUsername(String username); // 단건
    Optional<Member> findOptionalByUsername(String username); // 단건 Optional

    // 추가로 컬렉션을 조회하는데 아무 것도 선택되지 않는 경우 빈 컬렉션을 제공 (NULL이 아님)

}
