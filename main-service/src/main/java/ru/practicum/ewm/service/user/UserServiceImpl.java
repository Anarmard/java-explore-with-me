package ru.practicum.ewm.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.errorHandler.exceptions.AlreadyExistsException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    // admin
    // получение инфо о пользователях
    @Override
    public List<UserDto> getUserList(List<Long> idList, Pageable pageable) {
        if (idList == null) {
            // выгрузить всех пользователей постранично
            return userMapper.toUserDtoList(userRepository.findAll(pageable).getContent());
        } else {
            return userMapper.toUserDtoList(userRepository.findAllByIdIn(idList, pageable).getContent());
        }
    }

    // добавление нового пользователя
    @Override
    public UserDto addUser(UserDto userDto) {
        if (userRepository.existsByName(userDto.getName())) {
            throw new AlreadyExistsException("User already exists: " + userDto.getName());
        }
        User userToSave = userMapper.toUser(userDto);
        userRepository.save(userToSave);
        return userMapper.toUserDto(userToSave);
    }

    // удаление пользователя
    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}