package com.reelixen.splithelp.workout;

import com.reelixen.splithelp.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "workouts")
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Workout() {
    }

    public Workout(User user, String name, LocalDateTime performedAt) {
        this.user = user;
        this.name = name.trim();
        this.performedAt = performedAt;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
