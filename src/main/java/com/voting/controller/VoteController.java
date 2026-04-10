package com.voting.controller;

import com.voting.dto.VoteRequest;
import com.voting.dto.VoteResponse;
import com.voting.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping
    public ResponseEntity<VoteResponse> castVote(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VoteRequest request) {
        return ResponseEntity.ok(voteService.castVote(userDetails.getUsername(), request));
    }

    @GetMapping("/verify/{receiptHash}")
    public ResponseEntity<Map<String, Object>> verifyReceipt(@PathVariable String receiptHash) {
        boolean valid = voteService.verifyReceipt(receiptHash);
        return ResponseEntity.ok(Map.of(
                "valid", valid,
                "message", valid ? "✅ Your vote is recorded." : "❌ Receipt not found."
        ));
    }
}
