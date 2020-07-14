package ru.bloodsoft.gibddchecker.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AutoDetailsResponse {
    private HashMap<Integer, Engine> ENGINES_MAP = new HashMap<>(2);
    private String response;
    private Boolean isDetailsExists;
    private Integer choosedVariant = 0;

    public void addEngine(Integer id, Engine engine) {
        ENGINES_MAP.put(id, engine);
    }

    public void setRawResponse(String response) {
        this.response = response;
    }

    public void setDetailsExists() {
        isDetailsExists = true;
    }

    public void deleteDetails() {
        isDetailsExists = false;
    }

    public void setChoosedVariant(Integer variant) {
        choosedVariant = variant;
    }

    public Integer getChoosedVariant() {
        return choosedVariant;
    }

    public Engine getChoosedEngine() {
        Integer i = getChoosedVariant();
        Engine eng = ENGINES_MAP.get(i);

        return eng;
    }

    public String getRawResponse() {
        return response;
    }

    public Boolean isDetailsExists() {
        return ENGINES_MAP.size() > 0;
    }

    public List getEngines() {
        List<String> variants = new ArrayList<String>();

        for (int i = 0; i < ENGINES_MAP.size(); i++) {
            Engine eng = ENGINES_MAP.get(i);
            variants.add(eng.getName());
        }

        return variants;
    }
}