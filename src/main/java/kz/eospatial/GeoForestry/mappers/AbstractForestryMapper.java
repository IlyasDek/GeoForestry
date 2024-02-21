package kz.eospatial.GeoForestry.mappers;

import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.models.Forestry;
import kz.eospatial.GeoForestry.models.GeoCoordinate;
import org.locationtech.jts.geom.*;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring", uses = AbstractForestryMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class AbstractForestryMapper {

    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    // Определяем абстрактные методы для маппинга, которые будут реализованы MapStruct
    public abstract ForestryDto toDto(Forestry forestry);

    // Используем @AfterMapping для обновления сущности с новыми гео-данными
    @AfterMapping
    public void updateForestryFromDto(ForestryDto dto, @MappingTarget Forestry forestry) {
        forestry.setCenter(createPointFromGeoCoordinate(dto.getCenter()));
//        forestry.setBoundaries(createPolygonFromGeoCoordinates(dto.getBoundaries()));
    }

    protected Point createPointFromGeoCoordinate(GeoCoordinate geoCoordinate) {
        return geometryFactory.createPoint(new Coordinate(geoCoordinate.getLongitude(), geoCoordinate.getLatitude()));
    }

    protected Polygon createPolygonFromGeoCoordinates(List<GeoCoordinate> geoCoordinates) {
        Coordinate[] coordinates = geoCoordinates.stream()
                .map(geoCoord -> new Coordinate(geoCoord.getLongitude(), geoCoord.getLatitude()))
                .toArray(Coordinate[]::new);
        if (coordinates.length > 0 && !coordinates[coordinates.length - 1].equals2D(coordinates[0])) {
            coordinates = Arrays.copyOf(coordinates, coordinates.length + 1);
            coordinates[coordinates.length - 1] = coordinates[0]; // Замыкание полигона
        }
        return geometryFactory.createPolygon(coordinates);
    }
}