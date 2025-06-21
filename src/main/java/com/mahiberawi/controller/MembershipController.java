package com.mahiberawi.controller;

import com.mahiberawi.dto.membership.MembershipRequest;
import com.mahiberawi.dto.membership.MembershipResponse;
import com.mahiberawi.entity.User;
import com.mahiberawi.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/memberships")
@RequiredArgsConstructor
public class MembershipController {
    private final MembershipService membershipService;

    @PostMapping
    public ResponseEntity<MembershipResponse> createMembership(
            @Valid @RequestBody MembershipRequest request,
            @AuthenticationPrincipal User creator) {
        MembershipResponse membership = membershipService.createMembership(request, creator);
        return ResponseEntity.ok(membership);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembershipResponse> getMembership(@PathVariable String id) {
        MembershipResponse membership = membershipService.getMembership(id);
        return ResponseEntity.ok(membership);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MembershipResponse>> getMembershipsByUser(@PathVariable String userId) {
        List<MembershipResponse> memberships = membershipService.getMembershipsByUser(userId);
        return ResponseEntity.ok(memberships);
    }

    @GetMapping("/active")
    public ResponseEntity<List<MembershipResponse>> getActiveMemberships() {
        List<MembershipResponse> memberships = membershipService.getActiveMemberships();
        return ResponseEntity.ok(memberships);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MembershipResponse> updateMembership(
            @PathVariable String id,
            @Valid @RequestBody MembershipRequest request) {
        MembershipResponse membership = membershipService.updateMembership(id, request);
        return ResponseEntity.ok(membership);
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<MembershipResponse> renewMembership(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newEndDate) {
        MembershipResponse membership = membershipService.renewMembership(id, newEndDate);
        return ResponseEntity.ok(membership);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<MembershipResponse> cancelMembership(@PathVariable String id) {
        MembershipResponse membership = membershipService.cancelMembership(id);
        return ResponseEntity.ok(membership);
    }
} 