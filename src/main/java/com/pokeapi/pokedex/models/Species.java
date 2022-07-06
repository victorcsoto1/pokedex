package com.pokeapi.pokedex.models;

import java.util.List;

public class Species {
    private EvolutionChain evolution_chain;
    private List<FlavorTextEntries> flavor_text_entries;

    public EvolutionChain getEvolution_chain() {
        return evolution_chain;
    }
    public void setEvolution_chain(EvolutionChain evolution_chain) {
        this.evolution_chain = evolution_chain;
    }
    public List<FlavorTextEntries> getFlavor_text_entries() {
        return flavor_text_entries;
    }
    public void setFlavor_text_entries(List<FlavorTextEntries> flavor_text_entries) {
        this.flavor_text_entries = flavor_text_entries;
    }
    
}
