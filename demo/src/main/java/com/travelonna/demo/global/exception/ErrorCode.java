package com.travelonna.demo.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid Input Value"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "Method Not Allowed"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "Internal Server Error"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "Invalid Type Value"),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "Access is Denied"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C006", "Resource Not Found"),
    
    // Authentication
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "Unauthorized"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "Invalid Token"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "Expired Token"),
    
    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User not found"),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "U002", "Email already exists"),
    USERNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "U003", "Username already exists"),
    
    // Plan
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "Plan not found"),
    PLAN_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P002", "You don't have permission to access this plan"),
    
    // Place
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PL001", "Place not found"),
    PLACE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PL002", "You don't have permission to access this place"),
    
    // Group
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "G001", "Group not found"),
    GROUP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "G002", "You don't have permission to access this group"),
    
    // Follow
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "Follow relationship not found"),
    ALREADY_FOLLOWING(HttpStatus.BAD_REQUEST, "F002", "Already following this user"),
    CANNOT_FOLLOW_YOURSELF(HttpStatus.BAD_REQUEST, "F003", "Cannot follow yourself");
    
    private final HttpStatus status;
    private final String code;
    private final String message;
} 