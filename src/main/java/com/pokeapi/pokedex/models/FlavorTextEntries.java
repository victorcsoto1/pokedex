package com.pokeapi.pokedex.models;

public class FlavorTextEntries {
    private String flavor_text;
    private DescriptionLanguage language;

    public String getFlavor_text() {
        return flavor_text;
    }

    public void setFlavor_text(String flavor_text) {
        this.flavor_text = flavor_text;
    }

    public DescriptionLanguage getLanguage() {
        return language;
    }

    public void setLanguage(DescriptionLanguage language) {
        this.language = language;
    }
}
