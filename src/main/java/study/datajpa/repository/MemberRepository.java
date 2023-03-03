package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // @Param에 들어온 값이 :names에 들어감 (where in 쿼리 지원)
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    Member findByUsername(String username);

    // 반환 타입을 유연하게 작성할 수 있음
    // 여기서 나온 List, Member, Optional은 시작을 대문자로 하고 임의로 적은 내용으로 메서드에 영향 X
//    List<Member> findListByUsername(String username); // 컬렉션
//    Member findMemberByUsername(String username); // 단건
//    Optional<Member> findOptionalByUsername(String username); // 단건 Optional

    // 추가로 컬렉션을 조회하는데 아무 것도 선택되지 않는 경우 빈 컬렉션을 제공 (NULL이 아님)

    // Pageable 인터페이스를 넘긴다
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m.username) from Member m") // 카운트 쿼리만 따로 분리하면 성능 향상
    Page<Member> findByAge(int age, Pageable pageable);
    Slice<Member> findSliceByAge(int age, Pageable pageable);

    @Modifying(clearAutomatically = true)
    // 쿼리가 나간 다음에 em.clear(); 를 자동으로 실행
    // em.flush()는 업데이트할 때 자동 DB 반영되므로 안해도 됨
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);


    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    @Override // 상위에 있으므로 인터페이스 override 필요
    @EntityGraph(attributePaths = ("team"))
    @Query("select m from Member m") // 쿼리를 짠 후에 위에 엔티티 그래프를 추가해도 문제 없음
    List<Member> findAll();

    // username으로 회원 조회만 하는데 team 데이터도 필요한 경우 적용
    @EntityGraph(attributePaths = ("team"))
    List<Member> findEntityGraphByUsername(@Param("username") String username);
}
