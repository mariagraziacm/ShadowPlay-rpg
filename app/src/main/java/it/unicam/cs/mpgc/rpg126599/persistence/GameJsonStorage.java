package it.unicam.cs.mpgc.rpg126599.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


public class GameJsonStorage {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void save(GameState state, Path file) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(state, writer);
        }
    }
    
    public GameState load(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            GameState state = gson.fromJson(reader, GameState.class);
            if (state == null) {
                throw new IOException("File di salvataggio vuoto o non valido: " + file);
            }
            return state;
        }
    }
}
