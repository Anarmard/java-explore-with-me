package ru.practicum.ewm.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.service.compilation.CompilationService;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class AdmCompilationController {
    private final CompilationService compilationService;

    // добавление новой подборки
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("AdmCompilationController / addCompilation: добавление новой подборки {}", newCompilationDto);
        return compilationService.addCompilation(newCompilationDto);
    }

    // удаление подборки
    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("AdmCompilationController / deleteCompilation: удаление подборки {}", compId);
        compilationService.deleteCompilation(compId);
    }

    // обновить информацию о подборке
    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        log.info("AdmCompilationController / updateCompilation: обновить информацию о подборке {}", compId);
        return compilationService.updateCompilation(compId, updateCompilationRequest);
    }
}