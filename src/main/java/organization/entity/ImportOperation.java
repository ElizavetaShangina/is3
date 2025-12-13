package organization.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "import_operation")
@Getter
@Setter
public class ImportOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportStatus status;

    @ManyToOne(fetch = FetchType.EAGER) // EAGER, чтобы имя пользователя загружалось сразу
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_time", nullable = false)
    private ZonedDateTime startTime;

    @Column(name = "end_time")
    private ZonedDateTime endTime;

    @Column(name = "added_objects_count")
    private Integer addedObjectsCount = 0;

    // Ошибка может быть длинной, даем ей место (TEXT в Postgres)
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}