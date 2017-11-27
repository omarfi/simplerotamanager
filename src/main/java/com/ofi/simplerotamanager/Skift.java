package com.ofi.simplerotamanager;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

class Skift implements Comparable<Skift> {
    @JsonProperty("title")
    private String ansattNavn;
    @JsonProperty("startsAt")
    private ZonedDateTime startTid;
    @JsonProperty("endsAt")
    private ZonedDateTime sluttTid;

    public String getAnsattNavn() {
        return ansattNavn;
    }

    public ZonedDateTime getStartTid() {
        return startTid;
    }

    public ZonedDateTime getSluttTid() {
        return sluttTid;
    }

    @Override
    public int compareTo(Skift o) {
        return startTid.compareTo(o.startTid);
    }
}
