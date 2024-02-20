package kz.eospatial.GeoForestry.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class TokenValidationResult {
    private final boolean valid;
    private final String message;

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
}
