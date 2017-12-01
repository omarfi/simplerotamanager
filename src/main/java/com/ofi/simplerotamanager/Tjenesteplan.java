package com.ofi.simplerotamanager;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class Tjenesteplan {

    private List<Skift> skifter;

    Tjenesteplan(List<Skift> skifter) {
        Collections.sort(skifter);
        this.skifter = skifter;
    }

    Month getManed() {
        return skifter.get(0).getStartTid().getMonth();
    }

    int getAar() {
        return skifter.get(0).getStartTid().getYear();
    }

    List<String> getAnsatte() {
        return skifter.stream().map(Skift::getAnsattNavn).distinct().collect(Collectors.toList());
    }

    List<Skift> getSkifterForAnsatt(String ansattNavn) {
        return skifter.stream()
                .filter(skift -> skift.getAnsattNavn().equals(ansattNavn))
                .collect(Collectors.toList());
    }

    List<Skift> getSkifterForAnsattForDato(String ansattNavn, LocalDate dato) {
        return skifter.stream()
                .filter(skift -> skift.getAnsattNavn().equals(ansattNavn))
                .filter(skift -> skift.getStartTid().toLocalDate().isEqual(dato))
                .collect(Collectors.toList());
    }
}
