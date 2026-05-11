package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.common.OffsetPageRequest;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
	private final CategoryRepository categoryRepository;

	@Transactional
	public CategoryDto create(NewCategoryDto dto) {
		Category category = CategoryMapper.toEntity(dto);
		Category savedCategory = categoryRepository.save(category);
		return CategoryMapper.toDto(savedCategory);
	}

	@Transactional
	public CategoryDto update(Long categoryId, CategoryDto dto) {
		Category category = getCategoryOrThrow(categoryId);
		category.setName(dto.getName());
		Category updatedCategory = categoryRepository.save(category);
		return CategoryMapper.toDto(updatedCategory);
	}

	@Transactional
	public void delete(Long categoryId) {
		if (!categoryRepository.existsById(categoryId)) {
			throw new NotFoundException(String.format("Category with id=%d was not found", categoryId));
		}

		if (categoryRepository.existsEventByCategoryId(categoryId)) {
			throw new ConflictException("The category is not empty");
		}

		categoryRepository.deleteById(categoryId);
	}

	public List<CategoryDto> getCategories(int from, int size) {
		Pageable pageable = new OffsetPageRequest(from, size);

		return categoryRepository.findAll(pageable).getContent()
				.stream()
				.map(CategoryMapper::toDto)
				.collect(Collectors.toList());
	}

	public CategoryDto getCategory(Long categoryId) {
		return CategoryMapper.toDto(getCategoryOrThrow(categoryId));
	}

	private Category getCategoryOrThrow(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new NotFoundException(
						String.format("Category with id=%d was not found", categoryId)
				));
	}
}