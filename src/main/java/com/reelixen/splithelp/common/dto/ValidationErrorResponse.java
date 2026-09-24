package com.reelixen.splithelp.common.dto;

import java.time.Instant;
import java.util.Map;

public record ValidationErrorResponse(
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors,
        String path,
        Instant timestamp
) {}
