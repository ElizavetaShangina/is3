package organization.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.ZonedDateTime;
import java.io.Serializable;

@Entity
@Table(name = "import_operation")
@Getter @Setter
public class ImportOperation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ImportStatus status;

    @Column(name = "start_time", nullable = false)
    private ZonedDateTime startTime;

    @Column(name = "end_time")
    private ZonedDateTime endTime;

    @Column(name = "added_objects_count")
    private Integer addedObjectsCount;

    @Column(name = "error_message", length = 4096)
    private String errorMessage;

    // НОВОЕ ПОЛЕ: Имя файла в MinIO
    @Column(name = "minio_object_name")
    private String minioObjectName;
}