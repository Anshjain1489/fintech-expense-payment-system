package com.ansh.fintech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ApprovalDecisionRequest {

    @NotBlank(message = "Status decision is required")
    @Pattern(regexp = "^(approved|rejected)$", message = "Status must be either 'approved' or 'rejected'")
    private String status;

    private String comment;

    public ApprovalDecisionRequest() {}

    public ApprovalDecisionRequest(String status, String comment) {
        this.status = status;
        this.comment = comment;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
