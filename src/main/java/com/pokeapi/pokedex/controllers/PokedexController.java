package com.pokeapi.pokedex.controllers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.pokeapi.pokedex.models.Pokemon;
import com.pokeapi.pokedex.models.PokemonAbility;
import com.pokeapi.pokedex.models.PokemonList;
import com.pokeapi.pokedex.models.PokemonResult;
import com.pokeapi.pokedex.models.PokemonType;
import com.pokeapi.pokedex.service.PokedexService;

@RestController
public class PokedexController {

    String url = "https://pokeapi.co/api/v2/pokemon/";

    @Autowired
    PokedexService service;

    //Metodo principal para mostrar en frontend la lista de pokemones (20)
    @GetMapping("/")
    public String getPokemonList() {

        try {

            //Llamada al servicio para obtener la lista de pokemon desde la api externa
            PokemonList pokemonList = service.getListaPokemonService(url);

            List<PokemonResult> results = pokemonList.getResults();

            //Armamos la pagina
            String tituloHtml = "<div align='center'>\n<h1>Pokédex</h1>\n";

            String body = "";

            for (PokemonResult pr : results) {
                body += "<h3>" + pr.getName() + "</h3> \n";
                Pokemon pokemon = service.getPokemonFromUrl(pr.getUrl());
                body += "<a href='/pokemoninfo?id=" + pokemon.getId() + "'><img src='" + pokemon.getSprites().getFront_default() + "'></a>\n";
            }

            //Url para ir a la pagina siguiente o anterior
            String siguiente = pokemonList.getNext();
            String anterior = pokemonList.getPrevious();
            
            body += "<br/>\n<br/>\n";

            //Incorporamos los urls para ir a la pagina siguiente/anterior
            if (anterior != null) {

                URL urlPrevious = new URL(anterior);
                String queryPrevious = urlPrevious.getQuery();
                String[] paramsPrevious = queryPrevious.split("&");
    
                String offsetPrevious = "";
                for (String param : paramsPrevious) {
                    offsetPrevious = param.split("=")[1];
                    break;
                }

                body += "<a href='/pageChange?offset=" + offsetPrevious + "'>Previous</a>&emsp;";
            }
            if (siguiente != null) {

                URL urlNext = new URL(siguiente);
                String queryNext = urlNext.getQuery();
                String[] paramsNext = queryNext.split("&");

                String offsetNext = "";
                for (String param : paramsNext) {
                    offsetNext = param.split("=")[1];
                    break;
                }

                body += "<a href='/pageChange?offset=" + offsetNext + "'>Next</a>";
            }

            //Se retorna todo el html para ser mostrado en el sitio web
            body += "\n</div>";

            return tituloHtml + body;

        } catch (RestClientException | MalformedURLException e) {
            return "Error: " + e.getMessage();
        }
    }

    //Metodo para poder obtener la pagina siguiente/anterior del pokedex
    @GetMapping("/pageChange")
    public String getNextPreviousPage(@RequestParam String offset, HttpServletRequest request) {

        url = "https://pokeapi.co/api/v2/pokemon/?offset=" + offset + "&limit=20";

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            String requestURL = request.getRequestURL().substring(0, request.getRequestURL().indexOf(request.getRequestURI()));
            String respuesta = restTemplate.getForObject(requestURL, String.class);

            return respuesta;

        } catch (RestClientException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    //Metodo para obtener la información del pokemon que se haya seleccionado
    @GetMapping("/pokemoninfo")
    public String getPokemonInfo(@RequestParam String id) {

        try{
            Pokemon pokemonSelected = service.getPokemonService(id);

            //Obtenemos la información básica del pokemon que se ha seleccionado
            Integer weight = pokemonSelected.getWeight();
            List<PokemonType> pokemonTypes = pokemonSelected.getTypes();
            List<String> pokemonTypeNames = new ArrayList<>();

            for(PokemonType type : pokemonTypes) {
                pokemonTypeNames.add(type.getType().getName());
            }

            List<PokemonAbility> abilities = pokemonSelected.getAbilities();
            List<String> abilityNames = new ArrayList<>();

            for(PokemonAbility pa : abilities) {
                abilityNames.add(pa.getAbility().getName());
            }

            //Obtenemos la descripción del pokemon y sus evoluciones
            String descripcion = service.getPokemonDescription(pokemonSelected);
            String evoluciones = service.getPokemonEvolutions(pokemonSelected);

            //Armamos el frontend a mostrar del pokemon seleccionado con toda la información adquirida anteriormente
            String body = "<div align='center'>";
            body += "<h1>Pokémon: " + pokemonSelected.getName() + "</h1>\n";
            body += "<img src='" + pokemonSelected.getSprites().getFront_default() + "'>\n\n";

            body += "<p>Weight: " + weight + "</p>\n";
            body += "<p>Type: ";
            if (pokemonTypeNames.size() == 2) {
                body += pokemonTypeNames.get(0) + "/" + pokemonTypeNames.get(1) + "</p>\n";
            }
            else if (pokemonTypeNames.size() == 1) {
                body += pokemonTypeNames.get(0) + "</p>\n";
            }

            body += "<p>Abilities: ";
            Integer count = 1;

            for(String habilidad : abilityNames) {
                if (count == abilityNames.size()) {
                    body += habilidad;
                }
                else {
                    body += habilidad + ", ";
                    count++;
                }
            }
            body += "</p>\n";

            body += "<p>Description: " + descripcion + "</p>\n";
            body += "<p>Evolutions: " + evoluciones + "</p>\n";

            return body + "\n<br/><br/><a href='/'>Volver</a></div>";

        } catch (RestClientException e) {
            e.printStackTrace();
            return null;
        }
    }

}
