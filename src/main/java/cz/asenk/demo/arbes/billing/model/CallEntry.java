package cz.asenk.demo.arbes.billing.model;

import java.time.Duration;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * @author Alois Šenkyřík asenkyrik@monetplus.cz
 * Created: 07.09.2023
 */
public record CallEntry(
        @NotBlank
        @Pattern(regexp="[\\d]{6}")
        String number,

        @NotNull
        LocalDateTime callStart,

        @NotNull
        LocalDateTime callEnd
) {


    public CallEntry {
        if(callStart.isAfter(callEnd))
            throw new IllegalArgumentException("Call start has to be before the end of the call.");
    }

    public long getCalledMinutes(){
        return Duration.between(callStart, callEnd).toMinutes();
    }
}
