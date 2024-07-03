package kz.eospatial.GeoForestry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordRequest {
    @NotBlank(message = "The password can't be blank")
    @Size(min = 8, max = 255, message = "The password length must be between 8 and 255 characters long")
    private String newPassword;
}
