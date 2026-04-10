package com.voting.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a single immutable vote.
 * The unique constraint on (voter, election) enforces one-vote-per-user.
 */
@Entity
@Table(name = "votes",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_voter_election",
           columnNames = {"voter_id", "election_id"}
       ))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    /** Hashed receipt for voter verification without revealing their choice. */
    @Column(nullable = false, unique = true, length = 64)
    private String receiptHash;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime votedAt;
}
