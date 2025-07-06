package com.mahiberawi.controller;

import com.mahiberawi.dto.equb.EqubGroupRequest;
import com.mahiberawi.dto.equb.EqubRoundResponse;
import com.mahiberawi.entity.EqubRound;
import com.mahiberawi.entity.Group;
import com.mahiberawi.entity.User;
import com.mahiberawi.service.EqubService;
import com.mahiberawi.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/equb")
@RequiredArgsConstructor
@Tag(name = "Equb", description = "Equb (rotating savings) management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class EqubController {
    private final EqubService equbService;
    private final GroupService groupService;

    @Operation(
        summary = "Configure group as Equb group",
        description = "Converts a regular group to an Equb group with specified settings"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Group configured as Equb successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid Equb configuration"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to configure Equb"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/groups/{groupId}/configure")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> configureEqubGroup(
            @Parameter(description = "Group ID", required = true)
            @PathVariable String groupId,
            @Parameter(description = "Equb configuration", required = true)
            @Valid @RequestBody EqubGroupRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        
        // For now, we'll use a simplified approach
        // In a real implementation, you'd need to add these methods to GroupService
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Equb configuration would be applied here")
                .data(Map.of("groupId", groupId, "equbAmount", request.getEqubAmount()))
                .build());
    }

    @Operation(
        summary = "Start new Equb round",
        description = "Starts a new Equb round and creates payment requests for all members"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Equb round started successfully",
            content = @Content(schema = @Schema(implementation = EqubRoundResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid round configuration"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to start rounds"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/groups/{groupId}/rounds/start")
    public ResponseEntity<EqubRoundResponse> startEqubRound(
            @Parameter(description = "Group ID", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        
        EqubRound round = equbService.startNewRound(groupId, user);
        return ResponseEntity.ok(mapToEqubRoundResponse(round));
    }

    @Operation(
        summary = "Select Equb winner",
        description = "Selects a winner for the current Equb round (admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Winner selected successfully",
            content = @Content(schema = @Schema(implementation = EqubRoundResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Cannot select winner yet"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to select winner"),
        @ApiResponse(responseCode = "404", description = "Group or round not found")
    })
    @PostMapping("/groups/{groupId}/rounds/select-winner")
    public ResponseEntity<EqubRoundResponse> selectEqubWinner(
            @Parameter(description = "Group ID", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        
        EqubRound round = equbService.selectWinner(groupId, user);
        return ResponseEntity.ok(mapToEqubRoundResponse(round));
    }

    @Operation(
        summary = "Get Equb rounds",
        description = "Retrieves all Equb rounds for a group"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Equb rounds retrieved successfully",
            content = @Content(schema = @Schema(implementation = EqubRoundResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/groups/{groupId}/rounds")
    public ResponseEntity<List<EqubRoundResponse>> getEqubRounds(
            @Parameter(description = "Group ID", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        
        List<EqubRound> rounds = equbService.getGroupRounds(groupId, user);
        List<EqubRoundResponse> responses = rounds.stream()
                .map(this::mapToEqubRoundResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @Operation(
        summary = "Get current Equb round",
        description = "Retrieves the current active Equb round for a group"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Current round retrieved successfully",
            content = @Content(schema = @Schema(implementation = EqubRoundResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/groups/{groupId}/rounds/current")
    public ResponseEntity<EqubRoundResponse> getCurrentEqubRound(
            @Parameter(description = "Group ID", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        
        EqubRound round = equbService.getCurrentRound(groupId, user);
        if (round == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(mapToEqubRoundResponse(round));
    }

    private EqubRoundResponse mapToEqubRoundResponse(EqubRound round) {
        return EqubRoundResponse.builder()
                .id(round.getId())
                .groupId(round.getGroup().getId())
                .groupName(round.getGroup().getName())
                .roundNumber(round.getRoundNumber())
                .totalAmount(round.getTotalAmount())
                .expectedAmount(round.getExpectedAmount())
                .winnerId(round.getWinner() != null ? round.getWinner().getId() : null)
                .winnerName(round.getWinner() != null ? round.getWinner().getName() : null)
                .status(round.getStatus())
                .startDate(round.getStartDate())
                .endDate(round.getEndDate())
                .winnerSelectedAt(round.getWinnerSelectedAt())
                .paymentDeadline(round.getPaymentDeadline())
                .gracePeriodDays(round.getGracePeriodDays())
                .penaltyAmount(round.getPenaltyAmount())
                .createdAt(round.getCreatedAt())
                .updatedAt(round.getUpdatedAt())
                .build();
    }
} 