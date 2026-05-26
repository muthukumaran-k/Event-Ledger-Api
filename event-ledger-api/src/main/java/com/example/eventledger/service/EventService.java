package com.example.eventledger.service;

import com.example.eventledger.dto.EventRequest;
import com.example.eventledger.model.EventEntity;
import com.example.eventledger.repository.EventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventService {

	private final EventRepository repository;

	public EventService(EventRepository repository) {
		this.repository = repository;
	}

	public EventEntity createEvent(EventRequest req) {
		// parse timestamp
		Instant ts;
		try {
			ts = Instant.parse(req.getEventTimestamp());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Invalid eventTimestamp format, must be ISO-8601 UTC");
		}

		// check duplicate
		Optional<EventEntity> existing = repository.findByEventId(req.getEventId());
		if (existing.isPresent()) {
			return existing.get();
		}

		EventEntity e = new EventEntity();
		e.setEventId(req.getEventId());
		e.setAccountId(req.getAccountId());
		e.setType(req.getType());
		e.setAmount(req.getAmount());
		e.setCurrency(req.getCurrency());
		e.setEventTimestamp(ts);
		if (req.getMetadata() != null) {
			e.setMetadata(req.getMetadata().toString());
		}

		try {
			return repository.save(e);
		} catch (DataIntegrityViolationException ex) {
			// race: another request created it concurrently -> return existing
			return repository.findByEventId(req.getEventId()).orElseThrow(() -> ex);
		}
	}

	public Optional<EventEntity> getByEventId(String eventId) {
		return repository.findByEventId(eventId);
	}

	public List<EventEntity> listByAccount(String accountId) {
		return repository.findByAccountIdOrderByEventTimestampAsc(accountId);
	}

	public BigDecimal computeBalance(String accountId) {
		List<EventEntity> events = listByAccount(accountId);
		BigDecimal balance = BigDecimal.ZERO;
		for (EventEntity e : events) {
			if ("CREDIT".equals(e.getType())) {
				balance = balance.add(e.getAmount());
			} else if ("DEBIT".equals(e.getType())) {
				balance = balance.subtract(e.getAmount());
			}
		}
		return balance;
	}
}
