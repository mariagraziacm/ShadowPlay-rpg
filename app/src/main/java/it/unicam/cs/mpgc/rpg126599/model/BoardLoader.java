package it.unicam.cs.mpgc.rpg126599.model;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.List;

/**
 * Componente infrastrutturale dedicato al caricamento dei dati della mappa da file JSON.
 * Isola completamente l'utilizzo della libreria Gson.
 */
public class BoardLoader {

    /** Carica il file JSON e restituisce una istanza di Board pronta all'uso. */
    public static Board loadFromResource(String resourcePath) {
        Gson gson = new Gson();

        try (Reader reader = new InputStreamReader(
                BoardLoader.class.getResourceAsStream(resourcePath), StandardCharsets.UTF_8)) {
            
            MapFile mapFile = gson.fromJson(reader, MapFile.class);
            if (mapFile == null || mapFile.locations == null) {
                throw new IllegalStateException("Il file mappa risulta vuoto o corrotto: " + resourcePath);
            }
            return new Board(mapFile.locations);

        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("Impossibile caricare il file di mappa dal path: " + resourcePath, e);
        }
    }

    /** DTO adibito unicamente alla deserializzazione di map.json. */
    private static class MapFile {
        List<Location> locations;
    }
}