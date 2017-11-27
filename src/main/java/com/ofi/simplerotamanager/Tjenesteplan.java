package com.ofi.simplerotamanager;


import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Tjenesteplan {

    private List<Skift> skifter;

    Tjenesteplan(List<Skift> skifter) {
        Collections.sort(skifter);
        this.skifter = skifter;
    }

    public Month getMonth() {
        return skifter.get(0).getStartTid().getMonth();
    }

    public int getYear() {
        return skifter.get(0).getStartTid().getYear();
    }

    public List<Skift> getSkifter() {
        return skifter;
    }

    public List<String> getAnsatte() {
        return skifter.stream().map(Skift::getAnsattNavn).distinct().collect(Collectors.toList());
    }
}
