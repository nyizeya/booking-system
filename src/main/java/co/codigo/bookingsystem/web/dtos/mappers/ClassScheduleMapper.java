package co.codigo.bookingsystem.web.dtos.mappers;

import co.codigo.bookingsystem.common.enumerations.BookingStatus;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.web.dtos.response.ClassScheduleDto;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClassScheduleMapper extends BaseMapper<ClassScheduleDto, ClassSchedule> {
    @Override
    ClassSchedule toEntity(ClassScheduleDto dto);

    @Mapping(target = "availableSlots", expression = "java(calculateAvailableSlots(classEntity))")
    @Override
    ClassScheduleDto toDTO(ClassSchedule classEntity);

    @Override
    List<ClassSchedule> toEntities(List<ClassScheduleDto> dtoList);

    @IterableMapping(qualifiedByName = "toDTO")
    @Override
    List<ClassScheduleDto> toDTOList(List<ClassSchedule> entityList);

    @Named("toDTO")
    default ClassScheduleDto mapToDTO(ClassSchedule classEntity) {
        return toDTO(classEntity);
    }

    default int calculateAvailableSlots(ClassSchedule classSchedule) {
        long count = classSchedule.getMaxCapacity() - classSchedule.getBookings().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .count();

        return Integer.parseInt(String.valueOf(count));
    }
}
