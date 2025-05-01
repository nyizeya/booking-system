package co.codigo.bookingsystem.web.dtos.mappers;

import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.web.dtos.response.UserDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper extends BaseMapper<UserDTO, User> {
    @Override
    User toEntity(UserDTO dto);

    @Override
    UserDTO toDTO(User entity);

    @Override
    List<User> toEntities(List<UserDTO> dtoList);

    @Override
    List<UserDTO> toDTOList(List<User> entityList);
}