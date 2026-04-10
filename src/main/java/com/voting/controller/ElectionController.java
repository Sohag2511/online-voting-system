package com.voting.controller;

import com.voting.dto.ElectionResult;
import com.voting.model.Election;
import com.voting.service.ElectionService;
import com.voting.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/elections")
@RequiredArgsConstructor
public class ElectionController {

    private final ElectionService electionService;
    private final VoteService voteService;

    @GetMapping
    public ResponseEntity<List<Election>> getActiveElections() {
        return ResponseEntity.ok(electionService.getActiveElections());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Election> getElection(@PathVariable Long id) {
        return ResponseEntity.ok(electionService.getElectionById(id));
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<ElectionResult> getResults(@PathVariable Long id) {
        return ResponseEntity.ok(voteService.getResults(id));
    }

    @GetMapping("/{id}/has-voted")
    public ResponseEntity<Map<String, Boolean>> hasVoted(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean voted = voteService.hasVoted(userDetails.getUsername(), id);
        return ResponseEntity.ok(Map.of("hasVoted", voted));
    }
}
