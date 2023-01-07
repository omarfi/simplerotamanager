package com.ofi.simplerotamanager;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Controller
public class TjenesteplanController {

    static final String SESSION_ATTR_TJENESTEPLAN = "tjenesteplan";
    static final String SESSION_ATTR_TJENESTEPLAN_FILNAVN = "tjenesteplan-filnavn";

    private final TjenesteplanExcelService tjenesteplanExcelService;

    @Autowired
    public TjenesteplanController(TjenesteplanExcelService tjenesteplanExcelService) {
        this.tjenesteplanExcelService = tjenesteplanExcelService;
    }

    @PostMapping(path = "genererTjenesteplan")
    public ResponseEntity<String> genererTjenesteplan(@RequestBody List<Skift> skifter, HttpServletRequest request) throws IOException {
        if (skifter.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        Tjenesteplan tjenesteplan = new Tjenesteplan(skifter);
        byte[] data = tjenesteplanExcelService.writeTjenesteplanToExcel(tjenesteplan);

        String filnavn = tjenesteplan + ".xlsx";

        request.getSession().setAttribute(SESSION_ATTR_TJENESTEPLAN, data);
        request.getSession().setAttribute(SESSION_ATTR_TJENESTEPLAN_FILNAVN, filnavn);

        return ResponseEntity.ok("Tjenesteplan generert");
    }

    @GetMapping(path = "lastned")
    public void lastNedTjenesteplan(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object sessionedTjenesteplan = request.getSession().getAttribute(SESSION_ATTR_TJENESTEPLAN);
        if (sessionedTjenesteplan != null) {
            byte[] tjenesteplan = (byte[]) sessionedTjenesteplan;

            response.reset();
            response.setContentType("application/octet-stream");
            response.setContentLength((tjenesteplan.length));
            response.setHeader("Content-Disposition", "attachment; filename="
                    + request.getSession().getAttribute(SESSION_ATTR_TJENESTEPLAN_FILNAVN));

            final ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(new ByteArrayInputStream(tjenesteplan), outputStream);
            outputStream.flush();

            request.getSession().removeAttribute(SESSION_ATTR_TJENESTEPLAN);
            request.getSession().removeAttribute(SESSION_ATTR_TJENESTEPLAN_FILNAVN);
        }
    }
}
