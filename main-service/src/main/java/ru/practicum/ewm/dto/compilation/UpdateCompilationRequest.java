package ru.practicum.ewm.dto.compilation;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

// Изменение информации о подборке событий.
// Если поле в запросе не указано (равно null) - значит изменение этих данных не треубется.

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCompilationRequest {
    Boolean pinned;
    String title;
    Set<Long> events; // здесь id event
}
