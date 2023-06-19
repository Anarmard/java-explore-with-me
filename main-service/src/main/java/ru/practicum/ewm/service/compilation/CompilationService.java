package ru.practicum.ewm.service.compilation;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    // public
    // получение подборок событий
    List<CompilationDto> getCompilationList(Boolean pinned, Pageable pageable);

    // получение подборки событие по его id
    CompilationDto getCompilation(Long compilationId);

    // admin
    // добавление новой подборки
    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    // удаление подборки
    void deleteCompilation(Long compilationId);

    // обновить информацию о подборке
    CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest updateCompilationRequest);
}
