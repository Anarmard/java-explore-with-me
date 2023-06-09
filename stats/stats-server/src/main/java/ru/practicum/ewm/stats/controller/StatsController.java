package ru.practicum.ewm.stats.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.service.StatsService;

import java.util.List;

@Controller
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Validated
public class StatsController {
    private final StatsService statsService;

    // Сохранение информации о том, что на uri конкретного сервиса был отправлен запрос пользователем
    @PostMapping("/hit")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void hit(@RequestBody EndpointHit endpointHit) {
        statsService.recordHit(endpointHit);
    }

    // Получение статистики по посещениям
    @GetMapping("/stats")
    public ResponseEntity<List<ViewStats>> getStats(@RequestParam @NonNull String start,
                                                    @RequestParam @NonNull String end,
                                                    @RequestParam @NonNull List<String> uris,
                                                    @RequestParam(defaultValue = "false") boolean unique) {
        List<ViewStats> results = statsService.calculateViews(start, end, uris, unique);
        return ResponseEntity.ok(results);
    }

}
