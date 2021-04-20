package com.fetch.rewards.controller;


import com.fetch.rewards.dto.PointsDTO;
import com.fetch.rewards.dto.SpendRequest;
import com.fetch.rewards.service.PointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rewards")
@Slf4j
public class RewardsController {

    private final PointsService pointsService;

    public RewardsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @PostMapping("/")
    public ResponseEntity addPoints(@RequestBody List<PointsDTO> pointsList) {
        log.info("Received add points request {}", pointsList);
        pointsService.addPoints(pointsList);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getBalances() {
        log.info("Retrieving rewards points");
        var rewards = pointsService.getBalances();
        return new ResponseEntity(rewards, HttpStatus.OK);
    }

    @PostMapping("/spend/")
    public ResponseEntity spendPoints(@RequestBody SpendRequest request) {
        log.info("Attempting to spend {} points", request.getPoints());
        var spent = pointsService.attemptPayment(request.getPoints());
        return new ResponseEntity(spent, HttpStatus.OK);
    }

}
