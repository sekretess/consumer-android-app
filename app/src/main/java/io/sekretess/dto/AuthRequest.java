package io.sekretess.dto;

public record AuthRequest(
        String username,
        String password
) {
}
