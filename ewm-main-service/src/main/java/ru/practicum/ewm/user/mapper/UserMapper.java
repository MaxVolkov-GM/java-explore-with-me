package ru.practicum.ewm.user.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;

@UtilityClass
public class UserMapper {
	public User toEntity(NewUserRequest request) {
		return User.builder()
				.name(request.getName())
				.email(request.getEmail())
				.build();
	}

	public UserDto toDto(User user) {
		return UserDto.builder()
				.id(user.getId())
				.name(user.getName())
				.email(user.getEmail())
				.build();
	}
}