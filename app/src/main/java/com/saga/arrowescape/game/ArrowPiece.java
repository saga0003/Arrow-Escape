package com.saga.arrowescape.game;

public final class ArrowPiece {
    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;

    public final int row;
    public final int col;
    public final int direction;
    public boolean removed;

    public ArrowPiece(int row, int col, int direction) {
        this.row = row;
        this.col = col;
        this.direction = direction;
        this.removed = false;
    }
}
