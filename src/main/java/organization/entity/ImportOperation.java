package organization.entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.io.Serializable;

@Entity
@Table(name = "import_operation")
public class ImportOperation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь с таблицей User
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

    // Геттеры и Сеттеры...

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public ImportStatus getStatus() { return status; }
    public void setStatus(ImportStatus status) { this.status = status; }
    public ZonedDateTime getStartTime() { return startTime; }
    public void setStartTime(ZonedDateTime startTime) { this.startTime = startTime; }
    public ZonedDateTime getEndTime() { return endTime; }
    public void setEndTime(ZonedDateTime endTime) { this.endTime = endTime; }
    public Integer getAddedObjectsCount() { return addedObjectsCount; }
    public void setAddedObjectsCount(Integer addedObjectsCount) { this.addedObjectsCount = addedObjectsCount; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
