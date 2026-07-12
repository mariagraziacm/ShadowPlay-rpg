package it.unicam.cs.mpgc.rpg126599.persistence;

import it.unicam.cs.mpgc.rpg126599.model.GameState;
import java.io.IOException;
import java.nio.file.Path;

public interface GameRepository {
    void save(GameState state, Path file) throws IOException;
    GameState load(Path file) throws IOException;
}