package com.ofi.simplerotamanager;

import jakarta.servlet.http.HttpSession;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TjenesteplanController.class)
class TjenesteplanControllerTest {

    private static final byte[] TJENESTEPLAN_FILE = new byte[]{'a', 'b', 'c'};
    private static final String REQUEST_CONTENT = "[" +
            "  {" +
            "    \"title\":\"Stian\"," +
            "    \"startsAt\":\"01.01.2017, 01:00:00\"," +
            "    \"endsAt\":\"01.01.2017, 18:00:00\"," +
            "    \"calendarEventId\":1" +
            "  }," +
            "  {" +
            "    \"title\":\"Omar\"," +
            "    \"startsAt\":\"18.01.2017, 14:00:00\"," +
            "    \"endsAt\":\"18.01.2017, 23:00:00\"," +
            "    \"calendarEventId\":2" +
            "  }" +
            "]";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TjenesteplanExcelService tjenesteplanExcelService;

    @Test
    void genererTjenesteplan() throws Exception {
        ArgumentCaptor<Tjenesteplan> tjenesteplanArgumentCaptor = ArgumentCaptor.forClass(Tjenesteplan.class);

        when(tjenesteplanExcelService.writeTjenesteplanToExcel(tjenesteplanArgumentCaptor.capture())).thenReturn(TJENESTEPLAN_FILE);

        MvcResult mvcResult = mvc.perform(post("/genererTjenesteplan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_CONTENT))
                .andExpect(status().isOk())
                .andExpect(content().string("Tjenesteplan generert"))
                .andReturn();

        Tjenesteplan tjenesteplan = tjenesteplanArgumentCaptor.getValue();

        HttpSession session = mvcResult.getRequest().getSession();
        assertEquals(session.getAttribute(TjenesteplanController.SESSION_ATTR_TJENESTEPLAN), TJENESTEPLAN_FILE);
        assertEquals(session.getAttribute(TjenesteplanController.SESSION_ATTR_TJENESTEPLAN_FILNAVN), tjenesteplan + ".xlsx");

        assertEquals(tjenesteplan.getManed(), Month.JANUARY);
        assertEquals(tjenesteplan.getAar(), 2017);
        assertTrue(tjenesteplan.getAnsatte().containsAll(
                Lists.newArrayList("Omar", "Stian")));

        List<Skift> skiferForOmar = tjenesteplan.getSkifterForAnsattForDato(
                "Omar", LocalDate.of(2017, Month.JANUARY, 18));
        assertEquals(skiferForOmar.size(), 1);
        assertEquals(skiferForOmar.get(0).getStartTid().getHour(), 14);
        assertEquals(skiferForOmar.get(0).getSluttTid().getHour(), 23);

        List<Skift> skifterForStian = tjenesteplan.getSkifterForAnsattForDato(
                "Stian", LocalDate.of(2017, Month.JANUARY, 1));
        assertEquals(skifterForStian.size(), 1);
        assertEquals(skifterForStian.get(0).getStartTid().getHour(), 1);
        assertEquals(skifterForStian.get(0).getSluttTid().getHour(), 18);

        List<Skift> potensielleSkiferForOmar = tjenesteplan.getSkifterForAnsattForDato(
                "Omar", LocalDate.of(2017, Month.JANUARY, 30));
        assertEquals(potensielleSkiferForOmar.size(), 0);
    }

    @Test
    void lastNedTjenesteplan() throws Exception {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(TjenesteplanController.SESSION_ATTR_TJENESTEPLAN, TJENESTEPLAN_FILE);
        sessionAttrs.put(TjenesteplanController.SESSION_ATTR_TJENESTEPLAN_FILNAVN, "filnavn.xlsx");

        mvc.perform(get("/lastned")
                .sessionAttrs(sessionAttrs))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(TJENESTEPLAN_FILE));
    }
}