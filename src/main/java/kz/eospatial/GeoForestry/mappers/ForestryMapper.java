package kz.eospatial.GeoForestry.mappers;

import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.models.Forestry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ForestryMapper {

    ForestryMapper INSTANCE = Mappers.getMapper(ForestryMapper.class);

    @Mapping(target = "token", ignore = true)
    Forestry toModel(ForestryDto forestryDto);

    ForestryDto toDto(Forestry forestry);
}

