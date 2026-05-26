package com.example.eventledger.controller;

import com.example.eventledger.dto.EventRequest;
import com.example.eventledger.dto.EventResponse;
import com.example.eventledger.model.EventEntity;
import com.example.eventledger.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@Validated
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @PostMapping("/events")
    public ResponseEntity<EventResponse> postEvent(@Valid @RequestBody EventRequest req) {
        // Idempotency: if event already exists, return original with 200 OK
        Optional<EventEntity> existing = service.getByEventId(req.getEventId());
        if (existing.isPresent()) {
            return ResponseEntity.ok(toResponse(existing.get()));
        }

        // otherwise create and return 201 Created
        EventEntity created = service.createEvent(req);
        EventResponse resp = toResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable("id") String eventId) {
        Optional<EventEntity> e = service.getByEventId(eventId);
        return e.map(entity -> ResponseEntity.ok(toResponse(entity)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping(value = "/events", params = "account")
    public List<EventResponse> listEvents(@RequestParam("account") String accountId) {
        return service.listByAccount(accountId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/accounts/{accountId}/balance")
    public BigDecimal getBalance(@PathVariable String accountId) {
        return service.computeBalance(accountId);
    }

    private EventResponse toResponse(EventEntity e) {
        EventResponse r = new EventResponse();
        r.setEventId(e.getEventId());
        r.setAccountId(e.getAccountId());
        r.setType(e.getType());
        r.setAmount(e.getAmount());
        r.setCurrency(e.getCurrency());
        r.setEventTimestamp(e.getEventTimestamp());
        r.setMetadata(e.getMetadata());
        return r;
    }
}
