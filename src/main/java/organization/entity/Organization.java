package organization.entity;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "organizations")
public class Organization implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable=false)
    @NotBlank
    private String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "COORDINATES_ID", nullable=false)
    private Coordinates coordinates;

    @Column(name = "CREATIONDATE", nullable=false)
    private ZonedDateTime creationDate;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "OFFICIALADDRESS_ID", nullable=false)
    private Address officialAddress;

    @Column(name = "ANNUALTURNOVER")
    @Min(value = 1, message = "Значение поля должно быть больше 0")
    private Double annualTurnover;

    @Column(name = "EMPLOYEESCOUNT")
    @Min(value = 1, message = "Значение поля должно быть больше 0")
    private int employeesCount;

    @Column(name = "RATING")
    @Min(value = 1, message = "Значение поля должно быть больше 0")
    private Float rating;

    @Column(name = "FULLNAME", nullable = false)
    @Size(max = 1334, message = "Длина строки не должна превышать 1334 символа")
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private OrganizationType type;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "POSTALADDRESS_ID", nullable = false)
    private Address postalAddress;

    @PrePersist //непосредственно перед сохранением нового объекта
    protected void onCreate() {
        this.creationDate = ZonedDateTime.now();
    }
}




