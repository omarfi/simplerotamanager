package com.ofi.simplerotamanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;

@SpringBootApplication
public class App {

    static final Locale LOCALE = Locale.forLanguageTag("no-NO");

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
