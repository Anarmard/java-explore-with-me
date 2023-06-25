package ru.practicum.ewm.dto.compilation;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.dto.event.EventShortDto;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationDto {
    @NotNull
    Long id;
    @NotNull
    Boolean pinned;
    @NotNull
    String title;
    Set<EventShortDto> events; // здесь сами event
}
