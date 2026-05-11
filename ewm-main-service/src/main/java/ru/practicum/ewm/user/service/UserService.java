package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.common.OffsetPageRequest;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
	private final UserRepository userRepository;

	@Transactional
	public UserDto create(NewUserRequest request) {
		User user = UserMapper.toEntity(request);
		User savedUser = userRepository.save(user);
		return UserMapper.toDto(savedUser);
	}

	public List<UserDto> getUsers(List<Long> ids, int from, int size) {
		Pageable pageable = new OffsetPageRequest(from, size);

		List<User> users;
		if (ids == null || ids.isEmpty()) {
			users = userRepository.findAll(pageable).getContent();
		} else {
			users = userRepository.findAllByIdIn(ids, pageable);
		}

		return users.stream()
				.map(UserMapper::toDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public void delete(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new NotFoundException(String.format("User with id=%d was not found", userId));
		}

		userRepository.deleteById(userId);
	}
}