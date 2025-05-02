package co.codigo.bookingsystem.web.dtos.mappers;

import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.web.dtos.response.ClassScheduleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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

    @Override
    List<ClassScheduleDto> toDTOList(List<ClassSchedule> entityList);

    default int calculateAvailableSlots(ClassSchedule classSchedule) {
        long count = classSchedule.getMaxCapacity() - (long) classSchedule.getBookings().size();
        return Integer.parseInt(String.valueOf(count));
    }
}
