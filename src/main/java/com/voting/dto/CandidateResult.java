package com.voting.dto;
import lombok.Data;
@Data public class CandidateResult {
    private Long candidateId;
    private String candidateName;
    private String party;
    private long votes;
    private double percentage;
}
