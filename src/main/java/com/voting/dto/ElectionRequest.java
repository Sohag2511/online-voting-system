package com.voting.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data public class ElectionRequest {
    @NotBlank private String title;
    private String description;
    @NotNull private LocalDateTime startTime;
    @NotNull private LocalDateTime endTime;
}
