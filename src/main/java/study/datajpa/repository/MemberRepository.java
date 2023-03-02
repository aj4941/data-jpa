package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
}
