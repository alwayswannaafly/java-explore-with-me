package ru.practicum.controller;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAllCategories(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        if (from < 0 || size <= 0) {
            throw new ValidationException("Parameter 'from' and 'size' must be greater than 0.");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        return categoryService.getAllCategories(pageable);
    }

    @GetMapping("/{id}")
    public CategoryDto getCategory(@PathVariable Long id) {
        return categoryService.getCategory(id);
    }
}
