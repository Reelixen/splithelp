package com.reelixen.splithelp.workout;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findAllByUser_IdOrderByPerformedAtDesc(Long userId);

    Optional<Workout> findByIdAndUser_Id(Long id, Long userId);
}
