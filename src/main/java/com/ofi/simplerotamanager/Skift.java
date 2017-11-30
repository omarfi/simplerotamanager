package com.ofi.simplerotamanager;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Skift implements Comparable<Skift> {

    @JsonProperty("title")
    private String ansattNavn;

    @JsonProperty("startsAt")
    @JsonFormat(pattern = "dd.MM.yyyy, HH:mm:ss")
    private LocalDateTime startTid;

    @JsonProperty("endsAt")
    @JsonFormat(pattern = "dd.MM.yyyy, HH:mm:ss")
    private LocalDateTime sluttTid;

    public String getAnsattNavn() {
        return ansattNavn;
    }

    public LocalDateTime getStartTid() {
        return startTid;
    }

    public LocalDateTime getSluttTid() {
        return sluttTid;
    }

    public Duration getDuration() {
        return Duration.between(startTid, sluttTid);
    }

    @Override
    public int compareTo(Skift o) {
        return startTid.compareTo(o.startTid);
    }

    @Override
    public String toString() {
        return startTid.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + sluttTid.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
