package ru.practicum.ewm.dto.compilation;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewCompilationDto {
    Boolean pinned;
    @NotNull
    String title;
    Set<Long> events; // здесь id event
}
