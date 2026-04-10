package com.voting.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class VoteResponse {
    private String receiptHash;
    private String message;
}
