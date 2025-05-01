package co.codigo.bookingsystem.web.dtos.mappers;

import co.codigo.bookingsystem.domain.purchasedpkg.entity.PurchasedPackage;
import co.codigo.bookingsystem.web.dtos.response.PurchasedPackageDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PurchasedPackageMapper extends BaseMapper<PurchasedPackageDto, PurchasedPackage> {
    @Override
    PurchasedPackage toEntity(PurchasedPackageDto dto);

    @Override
    PurchasedPackageDto toDTO(PurchasedPackage entity);

    @Override
    List<PurchasedPackage> toEntities(List<PurchasedPackageDto> dtoList);

    @Override
    List<PurchasedPackageDto> toDTOList(List<PurchasedPackage> entityList);
}
