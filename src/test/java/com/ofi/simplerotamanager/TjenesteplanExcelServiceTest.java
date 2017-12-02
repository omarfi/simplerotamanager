package com.ofi.simplerotamanager;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class TjenesteplanExcelServiceTest {

    @Test
    public void convertToHoursRoundedToNearestQuarter() {
        assertEquals(TjenesteplanExcelService.convertToHoursRoundedToNearestQuarter(Duration.ZERO), 0.0, 0);

        assertEquals(TjenesteplanExcelService.convertToHoursRoundedToNearestQuarter(
                Duration.between(LocalDateTime.now(), LocalDateTime.now().plusHours(4).plusMinutes(35))), 4.75, 0);

        assertEquals(TjenesteplanExcelService.convertToHoursRoundedToNearestQuarter(
                Duration.between(LocalDateTime.now(), LocalDateTime.now().plusMinutes(90))), 1.5, 0);

        assertEquals(TjenesteplanExcelService.convertToHoursRoundedToNearestQuarter(
                Duration.between(LocalDateTime.now(), LocalDateTime.now().plusHours(8).plusMinutes(1))), 8.25, 0);

    }

}