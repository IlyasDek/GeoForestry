package kz.eospatial.GeoForestry.exeptions;

public class ForestryNameExistsException extends RuntimeException {
    public ForestryNameExistsException(String message) {
        super(message);
    }
}
