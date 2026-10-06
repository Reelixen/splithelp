package com.reelixen.splithelp.workout;

public class WorkoutNotFoundException extends RuntimeException {
    public WorkoutNotFoundException() {
        super("Workout not found");
    }
}
