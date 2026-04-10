package com.voting.service;

import com.voting.dto.CandidateRequest;
import com.voting.dto.ElectionRequest;
import com.voting.model.*;
import com.voting.repository.CandidateRepository;
import com.voting.repository.ElectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElectionService {

    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;

    // ── Elections ─────────────────────────────────────────────────────────────

    public Election createElection(ElectionRequest req) {
        if (req.getEndTime().isBefore(req.getStartTime()))
            throw new IllegalArgumentException("End time must be after start time.");

        Election election = Election.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .status(ElectionStatus.DRAFT)
                .build();

        return electionRepository.save(election);
    }

    public Election updateElection(Long id, ElectionRequest req) {
        Election election = getElectionById(id);
        if (election.getStatus() == ElectionStatus.ACTIVE)
            throw new IllegalStateException("Cannot edit an active election.");

        election.setTitle(req.getTitle());
        election.setDescription(req.getDescription());
        election.setStartTime(req.getStartTime());
        election.setEndTime(req.getEndTime());
        return electionRepository.save(election);
    }

    public void deleteElection(Long id) {
        Election election = getElectionById(id);
        if (election.getStatus() == ElectionStatus.ACTIVE)
            throw new IllegalStateException("Cannot delete an active election.");
        electionRepository.delete(election);
    }

    public Election activateElection(Long id) {
        Election election = getElectionById(id);
        if (election.getCandidates().size() < 2)
            throw new IllegalStateException("Election must have at least 2 candidates.");
        election.setStatus(ElectionStatus.ACTIVE);
        return electionRepository.save(election);
    }

    public Election closeElection(Long id) {
        Election election = getElectionById(id);
        election.setStatus(ElectionStatus.CLOSED);
        return electionRepository.save(election);
    }

    public List<Election> getAllElections() {
        return electionRepository.findAll();
    }

    public List<Election> getActiveElections() {
        return electionRepository.findByStatus(ElectionStatus.ACTIVE);
    }

    public Election getElectionById(Long id) {
        return electionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + id));
    }

    // ── Candidates ────────────────────────────────────────────────────────────

    public Candidate addCandidate(Long electionId, CandidateRequest req) {
        Election election = getElectionById(electionId);
        if (election.getStatus() == ElectionStatus.CLOSED)
            throw new IllegalStateException("Cannot add candidates to a closed election.");

        Candidate candidate = Candidate.builder()
                .name(req.getName())
                .bio(req.getBio())
                .party(req.getParty())
                .imageUrl(req.getImageUrl())
                .election(election)
                .build();

        return candidateRepository.save(candidate);
    }

    public void removeCandidate(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found."));
        if (candidate.getElection().getStatus() == ElectionStatus.ACTIVE)
            throw new IllegalStateException("Cannot remove candidates from an active election.");
        candidateRepository.delete(candidate);
    }

    // ── Auto Status Update (every 60 seconds) ─────────────────────────────────

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void autoUpdateElectionStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // Auto-activate elections whose start time has arrived
        electionRepository.findElectionsReadyToStart(now).forEach(e -> {
            if (e.getCandidates().size() >= 2) {
                e.setStatus(ElectionStatus.ACTIVE);
                electionRepository.save(e);
                log.info("Auto-activated election: {}", e.getTitle());
            }
        });

        // Auto-close elections whose end time has passed
        electionRepository.findExpiredActiveElections(now).forEach(e -> {
            e.setStatus(ElectionStatus.CLOSED);
            electionRepository.save(e);
            log.info("Auto-closed election: {}", e.getTitle());
        });
    }
}
