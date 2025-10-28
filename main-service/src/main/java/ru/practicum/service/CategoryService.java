package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Category with name=" + newCategoryDto.getName() + " already exists");
        }

        Category category = CategoryMapper.toCategory(newCategoryDto);
        Category saved = categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(saved);
    }

    public CategoryDto updateCategory(NewCategoryDto newCategoryDto, Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " not found"));

        if (!category.getName().equals(newCategoryDto.getName())) {
            if (categoryRepository.existsByName(newCategoryDto.getName())) {
                throw new ConflictException("Category with name=" + newCategoryDto.getName() + " already exists");
            }
        }

        category.setName(newCategoryDto.getName());
        Category saved = categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(saved);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " not found"));
        if (eventRepository.existsEventByCategory(category)) {
            throw new ConflictException("Category is not empty");
        }
        categoryRepository.delete(category);
    }

    public List<CategoryDto> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).getContent().stream()
                .map(CategoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    public CategoryDto getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " not found"));
        return CategoryMapper.toCategoryDto(category);
    }
}