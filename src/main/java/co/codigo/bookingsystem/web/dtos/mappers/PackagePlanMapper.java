package co.codigo.bookingsystem.web.dtos.mappers;

import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.web.dtos.response.PackagePlanDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PackagePlanMapper extends BaseMapper<PackagePlanDto, PackagePlan> {
    @Override
    PackagePlan toEntity(PackagePlanDto dto);

    @Override
    PackagePlanDto toDTO(PackagePlan entity);

    @Override
    List<PackagePlan> toEntities(List<PackagePlanDto> dtoList);

    @Override
    List<PackagePlanDto> toDTOList(List<PackagePlan> entityList);
}
