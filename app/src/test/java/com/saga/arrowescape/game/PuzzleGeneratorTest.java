package com.saga.arrowescape.game;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public final class PuzzleGeneratorTest {

    @Test
    public void generatedLevelsAreSolvable() {
        for (int level = 1; level <= 500; level++) {
            PuzzleGenerator.Puzzle puzzle = PuzzleGenerator.generate(level);
            List<ArrowPiece> remaining = new ArrayList<>(puzzle.pieces);

            int safety = remaining.size() + 5;
            while (!remaining.isEmpty() && safety-- > 0) {
                ArrowPiece removable = null;
                for (ArrowPiece candidate : remaining) {
                    boolean[][] occupied = new boolean[puzzle.gridSize][puzzle.gridSize];
                    for (ArrowPiece other : remaining) {
                        if (other != candidate) occupied[other.row][other.col] = true;
                    }
                    if (PuzzleGenerator.rayIsClear(candidate.row, candidate.col, candidate.direction, occupied, puzzle.gridSize)) {
                        removable = candidate;
                        break;
                    }
                }
                assertTrue("Level " + level + " has no legal move", removable != null);
                remaining.remove(removable);
            }
            assertTrue("Level " + level + " did not fully solve", remaining.isEmpty());
        }
    }
}
