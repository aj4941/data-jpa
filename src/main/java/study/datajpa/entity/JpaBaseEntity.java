package study.datajpa.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@MappedSuperclass // 이걸 작성해야 Member 필드에 값이 들어감
public class JpaBaseEntity {

    @Column(updatable = false) // 값을 실수로 바꿔도 업데이트 되지 않음
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist // Persist 하기 전에 호출
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
    }

    @PreUpdate // Update 하기 전에 호출
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
