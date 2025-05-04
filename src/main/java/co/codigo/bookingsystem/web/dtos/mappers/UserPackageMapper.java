package co.codigo.bookingsystem.web.dtos.mappers;

import co.codigo.bookingsystem.domain.packageplan.entity.UserPackage;
import co.codigo.bookingsystem.web.dtos.response.UserPackageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserPackageMapper extends BaseMapper<UserPackageDto, UserPackage> {
    @Override
    UserPackage toEntity(UserPackageDto dto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "packageId", source = "packagePlan.id")
    @Mapping(target = "packageName", source = "packagePlan.name")
    @Mapping(target = "isExpired", expression = "java(entity.getPackagePlan().getExpiryDate().isBefore(java.time.LocalDateTime.now()))")
    @Override
    UserPackageDto toDTO(UserPackage entity);

    @Override
    List<UserPackage> toEntities(List<UserPackageDto> dtoList);

    @Override
    List<UserPackageDto> toDTOList(List<UserPackage> entityList);
}
