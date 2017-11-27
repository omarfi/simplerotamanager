package com.ofi.simplerotamanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

@SpringBootApplication
@RestController
public class SimplerotamanagerApplication {

    private final TjenesteplanService tjenesteplanService;

    @Autowired
    public SimplerotamanagerApplication(TjenesteplanService tjenesteplanService) {
        this.tjenesteplanService = tjenesteplanService;
    }

    @RequestMapping(path = "/genererTjenesteplan", method = RequestMethod.POST)
    public String genererTjenesteplan(@RequestBody List<Skift> skifter) {
        if (skifter.isEmpty()) {
            return "Ingen skifter registrert";
        }
        tjenesteplanService.genererExcel(new Tjenesteplan(skifter));
        return "SUCCESS";
    }

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        return new Jackson2ObjectMapperBuilder().timeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
    }

    public static void main(String[] args) {
        SpringApplication.run(SimplerotamanagerApplication.class, args);
    }
}
