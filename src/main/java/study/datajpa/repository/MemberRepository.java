package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.List;

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
}
