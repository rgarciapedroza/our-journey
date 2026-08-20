package com.ourjourney.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignedUrlResponse {
    @JsonProperty("signedURL")
    private String signedUrl;
}
