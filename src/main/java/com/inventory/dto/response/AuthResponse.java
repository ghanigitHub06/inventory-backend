package com.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    // The JWT token — frontend stores this and sends it as
    // "Authorization: Bearer <token>" on every subsequent request
    private String token;

    // "ADMIN" or "CUSTOMER" — frontend uses this to decide
    // which pages/routes to show
    private String role;

    private String name;
    private String email;
}