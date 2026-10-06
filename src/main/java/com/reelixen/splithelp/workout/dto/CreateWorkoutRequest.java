package com.reelixen.splithelp.workout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateWorkoutRequest(
        @NotBlank
        @Size(max=100)
        String name,

        @NotNull
        LocalDateTime performedAt
) {
}
