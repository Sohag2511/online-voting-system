package com.voting.dto;
import lombok.Data;
import java.util.List;
@Data public class ElectionResult {
    private Long electionId;
    private String electionTitle;
    private long totalVotes;
    private List<CandidateResult> results;
}
