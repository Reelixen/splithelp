package com.reelixen.splithelp.workout.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record WorkoutResponse(
        Long id,
        String name,
        LocalDateTime performedAt,
        Instant createdAt
) {
}
