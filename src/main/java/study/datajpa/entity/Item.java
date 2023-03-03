package study.datajpa.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class) // Persistable 사용 시 작성 필요
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// Persistable 인터페이스로 판단 로직을 변경할 수 있게 함
public class Item implements Persistable<String> {

    @Id
    // @GeneratedValue : GeneratedValue를 안쓰는 경우 save 시 문제 발생
    private String id;

    @CreatedDate
    private LocalDateTime createdDate;

    public Item(String id) {
        this.id = id;
    }

    // Persistable을 Override
    @Override
    public String getId() {
        return id;
    }

    // Persistable을 Override
    @Override
    public boolean isNew() {
        return createdDate == null; // 생성되지 않았다면 true를 반환하여 새로운 객체로 판단
    }
}
