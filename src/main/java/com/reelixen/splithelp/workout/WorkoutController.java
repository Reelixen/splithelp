package com.reelixen.splithelp.workout;

import com.reelixen.splithelp.workout.dto.CreateWorkoutRequest;
import com.reelixen.splithelp.workout.dto.WorkoutResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workouts")
public class WorkoutController {
    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping
    public ResponseEntity<WorkoutResponse> create(
            @Valid @RequestBody CreateWorkoutRequest request,
            Authentication authentication){
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutService.create(authentication.getName(), request));
    }
}
