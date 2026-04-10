package com.voting.controller;

import com.voting.dto.CandidateRequest;
import com.voting.dto.ElectionRequest;
import com.voting.dto.ElectionResult;
import com.voting.model.Candidate;
import com.voting.model.Election;
import com.voting.model.User;
import com.voting.service.ElectionService;
import com.voting.service.UserService;
import com.voting.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ElectionService electionService;
    private final UserService userService;
    private final VoteService voteService;

    // ── Election Management ───────────────────────────────────────────────────

    @GetMapping("/elections")
    public ResponseEntity<List<Election>> getAllElections() {
        return ResponseEntity.ok(electionService.getAllElections());
    }

    @PostMapping("/elections")
    public ResponseEntity<Election> createElection(@Valid @RequestBody ElectionRequest request) {
        return ResponseEntity.ok(electionService.createElection(request));
    }

    @PutMapping("/elections/{id}")
    public ResponseEntity<Election> updateElection(@PathVariable Long id,
                                                   @Valid @RequestBody ElectionRequest request) {
        return ResponseEntity.ok(electionService.updateElection(id, request));
    }

    @DeleteMapping("/elections/{id}")
    public ResponseEntity<Map<String, String>> deleteElection(@PathVariable Long id) {
        electionService.deleteElection(id);
        return ResponseEntity.ok(Map.of("message", "Election deleted."));
    }

    @PatchMapping("/elections/{id}/activate")
    public ResponseEntity<Election> activateElection(@PathVariable Long id) {
        return ResponseEntity.ok(electionService.activateElection(id));
    }

    @PatchMapping("/elections/{id}/close")
    public ResponseEntity<Election> closeElection(@PathVariable Long id) {
        return ResponseEntity.ok(electionService.closeElection(id));
    }

    @GetMapping("/elections/{id}/results")
    public ResponseEntity<ElectionResult> getResults(@PathVariable Long id) {
        return ResponseEntity.ok(voteService.getResults(id));
    }

    // ── Candidate Management ──────────────────────────────────────────────────

    @PostMapping("/elections/{electionId}/candidates")
    public ResponseEntity<Candidate> addCandidate(@PathVariable Long electionId,
                                                  @Valid @RequestBody CandidateRequest request) {
        return ResponseEntity.ok(electionService.addCandidate(electionId, request));
    }

    @DeleteMapping("/candidates/{id}")
    public ResponseEntity<Map<String, String>> removeCandidate(@PathVariable Long id) {
        electionService.removeCandidate(id);
        return ResponseEntity.ok(Map.of("message", "Candidate removed."));
    }

    // ── User Management ───────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/users/{id}/toggle")
    public ResponseEntity<Map<String, String>> toggleUser(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok(Map.of("message", "User status toggled."));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted."));
    }
}
