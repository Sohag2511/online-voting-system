package com.voting.repository;

import com.voting.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByVoterIdAndElectionId(Long voterId, Long electionId);

    Optional<Vote> findByReceiptHash(String receiptHash);

    long countByElectionId(Long electionId);

    long countByCandidateId(Long candidateId);

    @Query("SELECT v.candidate.id, COUNT(v) FROM Vote v WHERE v.election.id = :electionId GROUP BY v.candidate.id")
    List<Object[]> countVotesByCandidate(@Param("electionId") Long electionId);

    List<Vote> findByVoterIdOrderByVotedAtDesc(Long voterId);
}
