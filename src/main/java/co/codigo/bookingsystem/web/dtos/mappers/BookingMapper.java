package co.codigo.bookingsystem.web.dtos.mappers;

import co.codigo.bookingsystem.domain.booking.entity.Booking;
import co.codigo.bookingsystem.web.dtos.response.BookingDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ClassScheduleMapper.class})
public interface BookingMapper extends BaseMapper<BookingDto, Booking> {
    @Override
    Booking toEntity(BookingDto dto);

    @Override
    BookingDto toDTO(Booking entity);

    @Override
    List<Booking> toEntities(List<BookingDto> dtoList);

    @Override
    List<BookingDto> toDTOList(List<Booking> entityList);
}
