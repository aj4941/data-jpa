package study.datajpa.entity;

import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class) // xml 파일을 넣어서 한 번에 해결할 수도 있음
@MappedSuperclass
@Getter
public class BaseEntity extends BaseTimeEntity { // 등록자, 수정자까지 필요할 때 사용

    @CreatedBy
    @Column(updatable = false)
    private String createdBy; // 등록자 세션

    @LastModifiedBy
    private String lastModifiedBy; // 수정자 세션
}
