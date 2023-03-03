package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.datajpa.entity.Item;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
// @Transactional 이 없어도 save에는 @Transactional이 달려있으므로 문제되지는 않는다
class ItemRepositoryTest {

    @Autowired ItemRepository itemRepository;

    // id : Long인 상태
    @Test
    public void save() {
        Item item = new Item("A"); // 현재 Item 값 id 세팅이 되지 않음 (id = null)
        itemRepository.save(item); // save에서 id 값이 null이라면 새로운 엔티티로 간주
        // if. 식별자가 기본 타입 (long, int) 이라면 0일 때 새로운 엔티티로 간주
        // em.persist가 되어야 @GeneratedValue가 적용되어 값이 1 올라간다.
    }

    @Test
    public void save2() {
        Item item = new Item("A"); // PK에 값이 있는 상황 -> persist 호출이 되지 않음
        itemRepository.save(item);
        // 결국 persist가 아닌 merge가 호출되는데 merge는 DB에 값이 있다는 것을 전제로 실행됨
        // DB에서 없다는 것을 판단하고 다시 새로 넣음 -> 비효율적
    }
}