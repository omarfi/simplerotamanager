package com.ofi.simplerotamanager;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Controller
public class TjenesteplanController {

    static final String SESSION_ATTR_TJENESTEPLAN = "tjenesteplan";
    static final String SESSION_ATTR_TJENESTEPLAN_FILNAVN = "tjenesteplan-filnavn";

    private final TjenesteplanService tjenesteplanService;

    @Autowired
    public TjenesteplanController(TjenesteplanService tjenesteplanService) {
        this.tjenesteplanService = tjenesteplanService;
    }

    @RequestMapping(path = "/genererTjenesteplan", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity genererTjenesteplan(@RequestBody List<Skift> skifter, HttpServletRequest request) throws IOException {
        if (skifter.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        Tjenesteplan tjenesteplan = new Tjenesteplan(skifter);
        byte[] data = tjenesteplanService.genererTjenesteplan(tjenesteplan);

        String filnavn = "Tjenesteplan "
                + tjenesteplan.getManed()
                + " " + tjenesteplan.getAar()
                + ".xlsx";

        request.getSession().setAttribute(SESSION_ATTR_TJENESTEPLAN, data);
        request.getSession().setAttribute(SESSION_ATTR_TJENESTEPLAN_FILNAVN, filnavn);

        return ResponseEntity.ok("Tjenesteplan generert");
    }

    @RequestMapping(path = "/lastned", method = RequestMethod.GET)
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
