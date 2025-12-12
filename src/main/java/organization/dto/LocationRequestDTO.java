package organization.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequestDTO {

    @NotBlank
    private Double x;

    private int y;

    private String name;
    //здесь указываются поля, которые принимаются от пользователя.
    //валидируем поля с помозью аннотаций JPA. эта валидация должна соответствовать ребованиям
    //в варинате на сайте итмо
}
