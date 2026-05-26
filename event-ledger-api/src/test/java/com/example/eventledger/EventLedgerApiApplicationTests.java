package com.example.eventledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class EventLedgerApiApplicationTests {

	
	@Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
	void contextLoads() {
	}
    @Test
    public void testIdempotency() throws Exception {
        String evt = "{\n" +
                "  \"eventId\": \"evt-100\",\n" +
                "  \"accountId\": \"acct-1\",\n" +
                "  \"type\": \"CREDIT\",\n" +
                "  \"amount\": 150.0000,\n" +
                "  \"currency\": \"USD\",\n" +
                "  \"eventTimestamp\": \"2026-05-15T14:02:11Z\"\n" +
                "}";

        // first submit -> created
        String createdBody = mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(evt))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // duplicate submit -> ok and same
        String dupBody = mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(evt))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(createdBody).isEqualTo(dupBody);

        // balance should be 150
        String balance = mvc.perform(get("/accounts/acct-1/balance")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(new BigDecimal(balance)).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    public void testOutOfOrderArrivalAndBalance() throws Exception {
        String a = "{\n" +
                "  \"eventId\": \"evt-201\",\n" +
                "  \"accountId\": \"acct-2\",\n" +
                "  \"type\": \"CREDIT\",\n" +
                "  \"amount\": 200.00,\n" +
                "  \"currency\": \"USD\",\n" +
                "  \"eventTimestamp\": \"2026-05-20T10:00:00Z\"\n" +
                "}";

        String b = "{\n" +
                "  \"eventId\": \"evt-200\",\n" +
                "  \"accountId\": \"acct-2\",\n" +
                "  \"type\": \"DEBIT\",\n" +
                "  \"amount\": 50.00,\n" +
                "  \"currency\": \"USD\",\n" +
                "  \"eventTimestamp\": \"2026-05-19T09:00:00Z\"\n" +
                "}";

        // send in order: credit (later), then debit (earlier)
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(a)).andExpect(status().isCreated());
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(b)).andExpect(status().isCreated());

        // list should be ordered by timestamp asc: b then a
        String list = mvc.perform(get("/events?account=acct-2")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // quick check: eventId order
        assertThat(list).containsSequence("evt-200", "evt-201");

        // balance should be 150 (200 - 50)
        String balance = mvc.perform(get("/accounts/acct-2/balance")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(new BigDecimal(balance)).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    public void testValidation() throws Exception {
        // missing fields
        String bad = "{ \"eventId\": \"x\" }";
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(bad)).andExpect(status().isBadRequest());

        // zero amount
        String zero = "{\n" +
                "  \"eventId\": \"evt-300\",\n" +
                "  \"accountId\": \"acct-3\",\n" +
                "  \"type\": \"CREDIT\",\n" +
                "  \"amount\": 0.00,\n" +
                "  \"currency\": \"USD\",\n" +
                "  \"eventTimestamp\": \"2026-05-15T14:02:11Z\"\n" +
                "}";
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(zero)).andExpect(status().isBadRequest());

        // unknown type
        String badType = "{\n" +
                "  \"eventId\": \"evt-301\",\n" +
                "  \"accountId\": \"acct-3\",\n" +
                "  \"type\": \"XFR\",\n" +
                "  \"amount\": 10.00,\n" +
                "  \"currency\": \"USD\",\n" +
                "  \"eventTimestamp\": \"2026-05-15T14:02:11Z\"\n" +
                "}";
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(badType)).andExpect(status().isBadRequest());
    }


}
