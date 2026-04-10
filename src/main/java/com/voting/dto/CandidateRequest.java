package com.voting.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data public class CandidateRequest {
    @NotBlank private String name;
    private String bio;
    private String party;
    private String imageUrl;
}
