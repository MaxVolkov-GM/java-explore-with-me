package ru.practicum.ewm.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	@Query(value = "SELECT EXISTS (SELECT 1 FROM events WHERE category_id = :categoryId)", nativeQuery = true)
	boolean existsEventByCategoryId(Long categoryId);
}