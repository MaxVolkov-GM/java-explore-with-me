package ru.practicum.ewm.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
	private final CategoryService categoryService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CategoryDto create(@Valid @RequestBody NewCategoryDto dto) {
		return categoryService.create(dto);
	}

	@PatchMapping("/{catId}")
	public CategoryDto update(@PathVariable Long catId,
	                          @Valid @RequestBody CategoryDto dto) {
		return categoryService.update(catId, dto);
	}

	@DeleteMapping("/{catId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long catId) {
		categoryService.delete(catId);
	}
}