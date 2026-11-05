package com.matt1479.medicationorganizer.auth.dto;

public record TokenResponse(String accessToken, long expiresInSeconds) {}
