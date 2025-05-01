package co.codigo.bookingsystem.web.dtos.mappers;

import co.codigo.bookingsystem.domain.waitlist.entity.Waitlist;
import co.codigo.bookingsystem.web.dtos.response.WaitlistDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WaitlistMapper extends BaseMapper<WaitlistDto, Waitlist>{
    @Override
    Waitlist toEntity(WaitlistDto dto);

    @Override
    List<WaitlistDto> toDTOList(List<Waitlist> entityList);

    @Override
    List<Waitlist> toEntities(List<WaitlistDto> dtoList);

    @Override
    WaitlistDto toDTO(Waitlist entity);
}
