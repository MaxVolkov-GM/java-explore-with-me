package ru.practicum.ewm.category.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;

@UtilityClass
public class CategoryMapper {
	public Category toEntity(NewCategoryDto dto) {
		return Category.builder()
				.name(dto.getName())
				.build();
	}

	public CategoryDto toDto(Category category) {
		return CategoryDto.builder()
				.id(category.getId())
				.name(category.getName())
				.build();
	}
}