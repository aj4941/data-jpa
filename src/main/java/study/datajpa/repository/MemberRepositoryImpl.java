package study.datajpa.repository;

import lombok.RequiredArgsConstructor;
import study.datajpa.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

@RequiredArgsConstructor
// MemberRepository 이름을 맞춰줘야 한다는 점에 유의 (+ Impl을 붙여줘야 함)
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final EntityManager em;

//    public MemberRepositoryImpl(EntityManager em) {
//        this.em = em;
//    }
    @Override
    public List<Member> findMemberCustom() {
        return em.createQuery("select m from Member m")
                .getResultList();
    }
}
