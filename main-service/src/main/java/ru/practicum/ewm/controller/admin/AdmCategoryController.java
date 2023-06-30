package ru.practicum.ewm.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.service.category.CategoryService;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class AdmCategoryController {
    private final CategoryService categoryService;

    // добавление новой категории
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("AdmCategoryController / addCategory: добавление новой категории " + newCategoryDto);
        return categoryService.addCategory(newCategoryDto);
    }

    // удаление категории
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("AdmCategoryController / deleteCategory: удаление категории c id " + catId);
        categoryService.deleteCategory(catId);
    }

    // изменение категории
    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("AdmCategoryController / updateCategory: изменение категории " + catId + newCategoryDto);
        return categoryService.updateCategory(catId, newCategoryDto);
    }
}


