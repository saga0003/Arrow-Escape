package com.saga.arrowescape.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Generates deterministic, guaranteed-solvable arrow boards. */
public final class PuzzleGenerator {

    public static final class Puzzle {
        public final int gridSize;
        public final List<ArrowPiece> pieces;

        Puzzle(int gridSize, List<ArrowPiece> pieces) {
            this.gridSize = gridSize;
            this.pieces = pieces;
        }
    }

    private static final class Candidate {
        final int row;
        final int col;
        final int direction;

        Candidate(int row, int col, int direction) {
            this.row = row;
            this.col = col;
            this.direction = direction;
        }
    }

    private PuzzleGenerator() {}

    public static Puzzle generate(int level) {
        int grid = Math.min(8, 4 + Math.max(0, level - 1) / 18);
        int maxFill = Math.max(6, (int) Math.floor(grid * grid * Math.min(0.72, 0.38 + level * 0.0045)));
        int desired = Math.min(maxFill, 6 + Math.max(0, level - 1) / 2);

        for (int attempt = 0; attempt < 64; attempt++) {
            long seed = 0xA22E5CA9L + (long) level * 7919L + (long) attempt * 104729L;
            Random random = new Random(seed);
            boolean[][] occupied = new boolean[grid][grid];
            List<ArrowPiece> pieces = new ArrayList<>();

            while (pieces.size() < desired) {
                List<Candidate> candidates = findCandidates(occupied, grid);
                if (candidates.isEmpty()) break;
                Candidate chosen = candidates.get(random.nextInt(candidates.size()));
                occupied[chosen.row][chosen.col] = true;
                pieces.add(new ArrowPiece(chosen.row, chosen.col, chosen.direction));
            }

            if (pieces.size() == desired) {
                Collections.shuffle(pieces, new Random(seed ^ 0x5EEDL));
                return new Puzzle(grid, pieces);
            }
        }

        List<ArrowPiece> fallback = new ArrayList<>();
        int count = Math.min(desired, grid * 2);
        for (int i = 0; i < count; i++) {
            int col = i % grid;
            int row = (i / grid == 0) ? 0 : grid - 1;
            int dir = row == 0 ? ArrowPiece.UP : ArrowPiece.DOWN;
            fallback.add(new ArrowPiece(row, col, dir));
        }
        return new Puzzle(grid, fallback);
    }

    private static List<Candidate> findCandidates(boolean[][] occupied, int grid) {
        List<Candidate> result = new ArrayList<>();
        for (int row = 0; row < grid; row++) {
            for (int col = 0; col < grid; col++) {
                if (occupied[row][col]) continue;
                for (int dir = 0; dir < 4; dir++) {
                    if (rayIsClear(row, col, dir, occupied, grid)) {
                        result.add(new Candidate(row, col, dir));
                    }
                }
            }
        }
        return result;
    }

    public static boolean rayIsClear(int row, int col, int dir, boolean[][] occupied, int grid) {
        int dr = 0;
        int dc = 0;
        switch (dir) {
            case ArrowPiece.UP: dr = -1; break;
            case ArrowPiece.RIGHT: dc = 1; break;
            case ArrowPiece.DOWN: dr = 1; break;
            case ArrowPiece.LEFT: dc = -1; break;
            default: return false;
        }

        int r = row + dr;
        int c = col + dc;
        while (r >= 0 && r < grid && c >= 0 && c < grid) {
            if (occupied[r][c]) return false;
            r += dr;
            c += dc;
        }
        return true;
    }
}
