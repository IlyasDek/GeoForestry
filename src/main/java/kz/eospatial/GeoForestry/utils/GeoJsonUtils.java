package kz.eospatial.GeoForestry.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;

public class GeoJsonUtils {

    public static MultiPolygon parseGeoJson(String geoJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(geoJson);

        // Переходим к координатам MultiPolygon
        JsonNode coordinatesNode = root.get("features").get(0)
                .get("geometry").get("coordinates");

        GeometryFactory geometryFactory = new GeometryFactory();
        Polygon[] polygons = new Polygon[coordinatesNode.size()];

        for (int i = 0; i < coordinatesNode.size(); i++) {
            JsonNode polygonNode = coordinatesNode.get(i).get(0);
            Coordinate[] coordinates = new Coordinate[polygonNode.size()];
            for (int j = 0; j < polygonNode.size(); j++) {
                JsonNode coord = polygonNode.get(j);
                double lon = coord.get(0).asDouble();
                double lat = coord.get(1).asDouble();
                coordinates[j] = new Coordinate(lon, lat);
            }

            // Замыкаем полигон, если необходимо
            if (!coordinates[0].equals2D(coordinates[coordinates.length - 1])) {
                coordinates = Arrays.copyOf(coordinates, coordinates.length + 1);
                coordinates[coordinates.length - 1] = coordinates[0];
            }

            LinearRing shell = geometryFactory.createLinearRing(coordinates);
            polygons[i] = geometryFactory.createPolygon(shell);
        }

        // Создаем MultiPolygon
        return geometryFactory.createMultiPolygon(polygons);
    }
}
