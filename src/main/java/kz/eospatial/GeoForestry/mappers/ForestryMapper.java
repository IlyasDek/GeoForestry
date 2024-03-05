package kz.eospatial.GeoForestry.mappers;

import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.models.Forestry;
import kz.eospatial.GeoForestry.models.GeoCoordinate;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = ForestryMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ForestryMapper {

    ForestryMapper INSTANCE = Mappers.getMapper(ForestryMapper.class);


    @Mapping(target = "token", ignore = true)
    @Mapping(target = "boundaries", ignore = true)
    @Mapping(target = "center", ignore = true)
    Forestry toModel(ForestryDto forestryDto);

    @Mapping(target = "boundaries", ignore = true)
    @Mapping(target = "center", ignore = true)
    @Mapping(target = "token", ignore = true)
    ForestryDto toDto(Forestry forestry);

    @Mapping(target = "boundaries", ignore = true)
    @Mapping(target = "center", ignore = true)
    ForestryDto toDtoWithToken(Forestry forestry);

    @AfterMapping
    default void toModelPostMapping(ForestryDto dto, @MappingTarget Forestry entity) {
        // Преобразование List<GeoCoordinate> в строку для сохранения
        String boundariesAsString = dto.getBoundaries().stream()
                .map(coord -> coord.getLatitude() + "," + coord.getLongitude())
                .reduce((acc, coord) -> acc + ";" + coord)
                .orElse("");
        entity.setBoundaries(boundariesAsString);

        // Преобразование GeoCoordinate в строку для сохранения
        GeoCoordinate center = dto.getCenter();
        if (center != null) {
            String centerAsString = center.getLatitude() + "," + center.getLongitude();
            entity.setCenter(centerAsString);
        }
    }

    @AfterMapping
    default void toDtoPostMapping(Forestry entity, @MappingTarget ForestryDto dto) {
        // Преобразование строки в List<GeoCoordinate>
        if (entity.getBoundaries() != null && !entity.getBoundaries().isEmpty()) {
            String[] coordsArray = entity.getBoundaries().split(";");
            List<GeoCoordinate> boundaries = Arrays.stream(coordsArray)
                    .map(coord -> {
                        String[] latLong = coord.split(",");
                        return new GeoCoordinate(Double.parseDouble(latLong[0]), Double.parseDouble(latLong[1]));
                    })
                    .collect(Collectors.toList());
            dto.setBoundaries(boundaries);
        }

        // Преобразование строки в GeoCoordinate
        if (entity.getCenter() != null && !entity.getCenter().isEmpty()) {
            String[] latLong = entity.getCenter().split(",");
            GeoCoordinate center = new GeoCoordinate(Double.parseDouble(latLong[0]), Double.parseDouble(latLong[1]));
            dto.setCenter(center);
        }
    }
}