package com.zinphraek.leprestigehall.domain.user;

import com.zinphraek.leprestigehall.domain.media.UserMedia;
import com.zinphraek.leprestigehall.utilities.annotations.AllowedValues;
import com.zinphraek.leprestigehall.utilities.annotations.DatePattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;

import static com.zinphraek.leprestigehall.domain.constants.Regex.*;

public record UserDTO(
    Long id,
    @NotBlank String userId,
    String username,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Email String email,
    @Pattern(regexp = PHONE_REGEX) String phone,
    @DatePattern(pattern = DATE_REGEX) String dateOfBirth,
    @AllowedValues(allowedValues = {"Male", "Female", "Non Binary"}) String gender,
    String street,
    String city,
    @AllowedValues(
        allowedValues = {
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
            "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
            "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
            "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
        }) String state,
    @Pattern(regexp = ZIP_CODE_REGEX) String zipCode,
    UserMedia userMedia,
    boolean enabled,
    List<String> requiredActions) {
}

