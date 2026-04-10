package com.voting.service;

import com.voting.dto.CandidateResult;
import com.voting.dto.ElectionResult;
import com.voting.dto.VoteRequest;
import com.voting.dto.VoteResponse;
import com.voting.model.*;
import com.voting.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;

    @Transactional
    public VoteResponse castVote(String username, VoteRequest request) {
        User voter = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Election election = electionRepository.findById(request.getElectionId())
                .orElseThrow(() -> new IllegalArgumentException("Election not found."));

        if (election.getStatus() != ElectionStatus.ACTIVE)
            throw new IllegalStateException("This election is not currently active.");

        if (voteRepository.existsByVoterIdAndElectionId(voter.getId(), election.getId()))
            throw new IllegalStateException("You have already voted in this election.");

        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found."));

        if (!candidate.getElection().getId().equals(election.getId()))
            throw new IllegalArgumentException("Candidate does not belong to this election.");

        String receiptHash = generateReceiptHash(voter.getId(), election.getId(), candidate.getId());

        Vote vote = Vote.builder()
                .voter(voter)
                .election(election)
                .candidate(candidate)
                .receiptHash(receiptHash)
                .build();

        voteRepository.save(vote);

        return new VoteResponse(receiptHash,
                "Vote cast successfully! Save your receipt hash to verify your vote.");
    }

    public ElectionResult getResults(Long electionId) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found."));

        long totalVotes = voteRepository.countByElectionId(electionId);

        List<Object[]> rawCounts = voteRepository.countVotesByCandidate(electionId);
        Map<Long, Long> voteCounts = new HashMap<>();
        for (Object[] row : rawCounts) {
            voteCounts.put((Long) row[0], (Long) row[1]);
        }

        List<CandidateResult> results = new ArrayList<>();
        for (Candidate c : election.getCandidates()) {
            CandidateResult cr = new CandidateResult();
            cr.setCandidateId(c.getId());
            cr.setCandidateName(c.getName());
            cr.setParty(c.getParty());
            long votes = voteCounts.getOrDefault(c.getId(), 0L);
            cr.setVotes(votes);
            cr.setPercentage(totalVotes > 0 ? (votes * 100.0) / totalVotes : 0.0);
            results.add(cr);
        }

        results.sort((a, b) -> Long.compare(b.getVotes(), a.getVotes()));

        ElectionResult result = new ElectionResult();
        result.setElectionId(election.getId());
        result.setElectionTitle(election.getTitle());
        result.setTotalVotes(totalVotes);
        result.setResults(results);

        return result;
    }

    public boolean verifyReceipt(String receiptHash) {
        return voteRepository.findByReceiptHash(receiptHash).isPresent();
    }

    public boolean hasVoted(String username, Long electionId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return voteRepository.existsByVoterIdAndElectionId(user.getId(), electionId);
    }

    private String generateReceiptHash(Long userId, Long electionId, Long candidateId) {
        try {
            String raw = userId + ":" + electionId + ":" + candidateId + ":" + LocalDateTime.now();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Receipt generation failed", e);
        }
    }
}
