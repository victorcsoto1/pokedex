package com.pokeapi.pokedex.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.pokeapi.pokedex.models.Chain;
import com.pokeapi.pokedex.models.FlavorTextEntries;
import com.pokeapi.pokedex.models.Pokemon;
import com.pokeapi.pokedex.models.PokemonEvolutions;
import com.pokeapi.pokedex.models.PokemonList;
import com.pokeapi.pokedex.models.Species;

@Service
public class PokedexService {

    //Metodo para obtener el listado de 20 pokemones segun sea la pagina
    @Cacheable("lista_pokemon")
    public PokemonList getListaPokemonService(String url) throws RestClientException{
        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.getForObject(url, PokemonList.class);
    }

    //Metodo para obtener el pokemon a traves de la url
    @Cacheable("pokeurl")
    public Pokemon getPokemonFromUrl(String urlPokemon) {

        try {
            RestTemplate restTemplate = new RestTemplate();

            return restTemplate.getForObject(urlPokemon, Pokemon.class);

        } catch (RestClientException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Metodo para obtener el pokemon seleccionado desde la api externa
    @Cacheable("pokemon")
    public Pokemon getPokemonService(String id) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject("https://pokeapi.co/api/v2/pokemon/" + id, Pokemon.class);
        } catch (RestClientException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Metodo para obtener la descripcion del pokemon
    @Cacheable("description")
    public String getPokemonDescription(Pokemon pokemon) {

        try {
            String urlPokemonSpecie = pokemon.getSpecies().getUrl();
            RestTemplate restTemplate = new RestTemplate();

            Species pokemonSpecies = restTemplate.getForObject(urlPokemonSpecie, Species.class);

            String description = "";

            for (FlavorTextEntries flavorText : pokemonSpecies.getFlavor_text_entries()) {
                if (flavorText.getLanguage().getName().equals("en")){
                    description = flavorText.getFlavor_text();
                    break;
                }
            }

            return description.replaceAll("\\f", " ");

        } catch (RestClientException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Metodo para obtener las evoluciones del pokemon
    @Cacheable("evolutions")
    public String getPokemonEvolutions(Pokemon pokemon) {
        try {
            String urlPokemonSpecie = pokemon.getSpecies().getUrl();
            RestTemplate restTemplateSpecies = new RestTemplate();
            RestTemplate restTemplateEvolutions = new RestTemplate();

            Species pokemonSpecies = restTemplateSpecies.getForObject(urlPokemonSpecie, Species.class);

            String urlEvolutionChain = pokemonSpecies.getEvolution_chain().getUrl();

            PokemonEvolutions evolutions = restTemplateEvolutions.getForObject(urlEvolutionChain, PokemonEvolutions.class);

            List<Chain> evolutionChains = evolutions.getChain().getEvolves_to();

            if (evolutionChains.size() == 0)
                return "None";

            return evolutions.getChain().getSpecies().getName() + "\u2192" + this.getAllEvolutions(evolutionChains);

        } catch (RestClientException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Metodo recursivo para obtener la cadena evolutiva del pokemon
    public String getAllEvolutions(List<Chain> evolutionChains) {
        String nombreEvolucion = "";
        Integer count = 1;
        for(Chain c : evolutionChains) {
            if (c.getEvolves_to().size() == 0) {
                if (count == evolutionChains.size()) {
                    nombreEvolucion += c.getSpecies().getName();
                }
                else {
                    nombreEvolucion += c.getSpecies().getName() + "/";
                    count++;
                }
            }
            else {
                if (count == evolutionChains.size()) {
                    nombreEvolucion += c.getSpecies().getName() + "\u2192" + this.getAllEvolutions(c.getEvolves_to());
                }
                else {
                    nombreEvolucion += c.getSpecies().getName() + "\u2192" + this.getAllEvolutions(c.getEvolves_to()) + ", ";
                    count++;
                }
            }
        }
        return nombreEvolucion;
    }
}
