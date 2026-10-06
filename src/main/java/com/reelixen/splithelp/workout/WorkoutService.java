package com.reelixen.splithelp.workout;

import com.reelixen.splithelp.user.User;
import com.reelixen.splithelp.user.UserRepository;
import com.reelixen.splithelp.workout.dto.CreateWorkoutRequest;
import com.reelixen.splithelp.workout.dto.WorkoutResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkoutService {
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    public WorkoutService(WorkoutRepository workoutRepository, UserRepository userRepository) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WorkoutResponse create(String email, CreateWorkoutRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        Workout workout = new Workout(user, request.name(), request.performedAt());
        Workout savedWorkout = workoutRepository.save(workout);

        return new WorkoutResponse(
                savedWorkout.getId(),
                savedWorkout.getName(),
                savedWorkout.getPerformedAt(),
                savedWorkout.getCreatedAt()
        );
    }
}
