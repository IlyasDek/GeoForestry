package kz.eospatial.GeoForestry.models;

public class TokenValidationResult {
    private final boolean valid;
    private final String message;

    private TokenValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public static TokenValidationResult valid() {
        return new TokenValidationResult(true, "Token is valid.");
    }

    public static TokenValidationResult expired() {
        return new TokenValidationResult(false, "Token has expired.");
    }

    public static TokenValidationResult notFound() {
        return new TokenValidationResult(false, "Token not found.");
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }
}
