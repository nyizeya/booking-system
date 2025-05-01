package co.codigo.bookingsystem.web.dtos.mappers;

import co.codigo.bookingsystem.common.enumerations.BookingStatus;
import co.codigo.bookingsystem.domain.availableclass.entity.AvailableClass;
import co.codigo.bookingsystem.web.dtos.response.AvailableClassDto;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AvailableClassMapper extends BaseMapper<AvailableClassDto, AvailableClass> {
    @Override
    AvailableClass toEntity(AvailableClassDto dto);

    @Mapping(target = "availableSlots", expression = "java(calculateAvailableSlots(classEntity))")
    @Override
    AvailableClassDto toDTO(AvailableClass classEntity);

    @Override
    List<AvailableClass> toEntities(List<AvailableClassDto> dtoList);

    @IterableMapping(qualifiedByName = "toDTO")
    @Override
    List<AvailableClassDto> toDTOList(List<AvailableClass> entityList);

    @Named("toDTO")
    default AvailableClassDto mapToDTO(AvailableClass classEntity) {
        return toDTO(classEntity); // Call the main toDTO method here
    }

    default int calculateAvailableSlots(AvailableClass availableClass) {
        long count = availableClass.getMaxCapacity() - availableClass.getBookings().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .count();

        return Integer.parseInt(String.valueOf(count));
    }
}
