package com.saga.arrowescape.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

public final class GameView extends View {

    public interface Listener {
        void onLevelFinished(int completedLevel);
        void onRewardedContinueRequested();
        void onPrivacyRequested();
    }

    private static final String PREFS = "arrow_escape_progress";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_COINS = "coins";
    private static final String KEY_STREAK = "streak";

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path arrowPath = new Path();
    private final RectF boardRect = new RectF();
    private final RectF retryButton = new RectF();
    private final RectF continueButton = new RectF();
    private final RectF privacyButton = new RectF();
    private final SharedPreferences prefs;
    private final float density;

    private Listener listener;
    private PuzzleGenerator.Puzzle puzzle;
    private int level;
    private int coins;
    private int streak;
    private int hearts = 3;
    private boolean inputLocked;
    private boolean gameOver;
    private long wrongFlashUntil;
    private float lastTapX;
    private float lastTapY;

    private final int background = Color.rgb(11, 16, 32);
    private final int surface = Color.rgb(20, 27, 51);
    private final int surface2 = Color.rgb(28, 38, 70);
    private final int text = Color.rgb(247, 249, 255);
    private final int muted = Color.rgb(158, 169, 199);
    private final int mint = Color.rgb(124, 247, 200);
    private final int coral = Color.rgb(255, 118, 122);
    private final int blue = Color.rgb(112, 174, 255);
    private final int yellow = Color.rgb(255, 213, 105);

    public GameView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        level = Math.max(1, prefs.getInt(KEY_LEVEL, 1));
        coins = Math.max(0, prefs.getInt(KEY_COINS, 0));
        streak = Math.max(0, prefs.getInt(KEY_STREAK, 0));
        setBackgroundColor(background);
        setFocusable(true);
        loadLevel(level);
    }

    public void setListener(Listener listener) { this.listener = listener; }

    public void grantRewardedContinue() {
        if (!gameOver) return;
        hearts = 2;
        gameOver = false;
        inputLocked = false;
        invalidate();
    }

    public void advanceToNextLevel() {
        level++;
        streak++;
        coins += 20 + Math.min(80, level / 3);
        prefs.edit().putInt(KEY_LEVEL, level).putInt(KEY_COINS, coins).putInt(KEY_STREAK, streak).apply();
        loadLevel(level);
    }

    private void restartLevel() {
        hearts = 3;
        gameOver = false;
        inputLocked = false;
        streak = 0;
        prefs.edit().putInt(KEY_STREAK, streak).apply();
        loadLevel(level);
    }

    private void loadLevel(int requestedLevel) {
        puzzle = PuzzleGenerator.generate(requestedLevel);
        hearts = 3;
        inputLocked = false;
        gameOver = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawHeader(canvas);
        calculateBoardRect();
        drawBoard(canvas);
        drawFooter(canvas);
        if (SystemClock.uptimeMillis() < wrongFlashUntil) {
            paint.setColor(Color.argb(90, 255, 82, 92));
            canvas.drawCircle(lastTapX, lastTapY, dp(28), paint);
            postInvalidateDelayed(16);
        }
        if (gameOver) drawGameOverOverlay(canvas);
    }

    private void drawHeader(Canvas canvas) {
        float pad = dp(18);
        textPaint.setColor(text);
        textPaint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD));
        textPaint.setTextSize(sp(24));
        canvas.drawText("ARROW ESCAPE", pad, dp(38), textPaint);

        textPaint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.NORMAL));
        textPaint.setTextSize(sp(12));
        textPaint.setColor(muted);
        canvas.drawText("Clear every arrow. Order matters.", pad, dp(58), textPaint);

        float cardTop = dp(74), cardHeight = dp(52), gap = dp(8);
        float cardWidth = (getWidth() - pad * 2 - gap * 2) / 3f;
        drawStatCard(canvas, pad, cardTop, cardWidth, cardHeight, "LEVEL", String.valueOf(level), mint);
        drawStatCard(canvas, pad + cardWidth + gap, cardTop, cardWidth, cardHeight, "COINS", String.valueOf(coins), yellow);
        drawStatCard(canvas, pad + (cardWidth + gap) * 2, cardTop, cardWidth, cardHeight, "STREAK", streak + "×", blue);

        float s = dp(32);
        privacyButton.set(getWidth() - pad - s, dp(12), getWidth() - pad, dp(12) + s);
        paint.setColor(surface2);
        canvas.drawRoundRect(privacyButton, dp(10), dp(10), paint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(sp(15));
        textPaint.setColor(text);
        canvas.drawText("i", privacyButton.centerX(), privacyButton.centerY() + dp(5), textPaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawStatCard(Canvas canvas, float left, float top, float width, float height, String label, String value, int accent) {
        RectF rect = new RectF(left, top, left + width, top + height);
        paint.setColor(surface);
        canvas.drawRoundRect(rect, dp(14), dp(14), paint);
        paint.setColor(accent);
        canvas.drawRoundRect(new RectF(left + dp(8), top + dp(9), left + dp(12), top + height - dp(9)), dp(2), dp(2), paint);
        textPaint.setColor(muted);
        textPaint.setTextSize(sp(9));
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        canvas.drawText(label, left + dp(20), top + dp(18), textPaint);
        textPaint.setColor(text);
        textPaint.setTextSize(sp(16));
        canvas.drawText(value, left + dp(20), top + dp(40), textPaint);
    }

    private void calculateBoardRect() {
        float horizontal = dp(18), top = dp(145), bottomReserve = dp(98);
        float maxWidth = getWidth() - horizontal * 2;
        float maxHeight = Math.max(dp(200), getHeight() - top - bottomReserve);
        float size = Math.min(maxWidth, maxHeight);
        float left = (getWidth() - size) / 2f;
        boardRect.set(left, top, left + size, top + size);
    }

    private void drawBoard(Canvas canvas) {
        if (puzzle == null) return;
        paint.setColor(surface);
        canvas.drawRoundRect(boardRect, dp(20), dp(20), paint);
        int n = puzzle.gridSize;
        float innerPad = dp(12), gridLeft = boardRect.left + innerPad, gridTop = boardRect.top + innerPad;
        float gridSizePx = boardRect.width() - innerPad * 2, cell = gridSizePx / n;

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.argb(70, 120, 138, 185));
        for (int i = 1; i < n; i++) {
            canvas.drawLine(gridLeft + cell * i, gridTop, gridLeft + cell * i, gridTop + gridSizePx, paint);
            canvas.drawLine(gridLeft, gridTop + cell * i, gridLeft + gridSizePx, gridTop + cell * i, paint);
        }
        paint.setStyle(Paint.Style.FILL);

        for (ArrowPiece piece : puzzle.pieces) {
            if (piece.removed) continue;
            float cx = gridLeft + (piece.col + 0.5f) * cell;
            float cy = gridTop + (piece.row + 0.5f) * cell;
            drawArrowTile(canvas, cx, cy, cell * 0.72f, piece.direction);
        }
    }

    private void drawArrowTile(Canvas canvas, float cx, float cy, float size, int direction) {
        float half = size / 2f;
        RectF tile = new RectF(cx - half, cy - half, cx + half, cy + half);
        paint.setColor(colorForDirection(direction));
        canvas.drawRoundRect(tile, size * 0.23f, size * 0.23f, paint);

        float shaft = size * 0.38f, head = size * 0.28f, thick = Math.max(dp(2), size * 0.08f);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(thick);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);

        float dx = 0, dy = 0;
        if (direction == ArrowPiece.UP) dy = -1;
        if (direction == ArrowPiece.RIGHT) dx = 1;
        if (direction == ArrowPiece.DOWN) dy = 1;
        if (direction == ArrowPiece.LEFT) dx = -1;
        float sx = cx - dx * shaft * 0.46f, sy = cy - dy * shaft * 0.46f;
        float ex = cx + dx * shaft * 0.46f, ey = cy + dy * shaft * 0.46f;
        canvas.drawLine(sx, sy, ex, ey, paint);
        float px = -dy, py = dx;
        arrowPath.reset();
        arrowPath.moveTo(ex, ey);
        arrowPath.lineTo(ex - dx * head + px * head * 0.72f, ey - dy * head + py * head * 0.72f);
        arrowPath.moveTo(ex, ey);
        arrowPath.lineTo(ex - dx * head - px * head * 0.72f, ey - dy * head - py * head * 0.72f);
        canvas.drawPath(arrowPath, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private int colorForDirection(int direction) {
        switch (direction) {
            case ArrowPiece.UP: return mint;
            case ArrowPiece.RIGHT: return blue;
            case ArrowPiece.DOWN: return coral;
            case ArrowPiece.LEFT: return yellow;
            default: return mint;
        }
    }

    private void drawFooter(Canvas canvas) {
        float y = Math.min(getHeight() - dp(24), boardRect.bottom + dp(34));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(sp(15));
        textPaint.setColor(text);
        canvas.drawText("Lives  " + heartsText(), getWidth() / 2f, y, textPaint);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT);
        textPaint.setTextSize(sp(11));
        textPaint.setColor(muted);
        canvas.drawText("Tap an arrow only when its path to the edge is clear", getWidth() / 2f, y + dp(24), textPaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private String heartsText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) sb.append(i < hearts ? "♥ " : "♡ ");
        return sb.toString().trim();
    }

    private void drawGameOverOverlay(Canvas canvas) {
        paint.setColor(Color.argb(210, 4, 8, 20));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        float panelWidth = Math.min(getWidth() - dp(36), dp(360));
        RectF panel = new RectF(cx - panelWidth / 2f, cy - dp(150), cx + panelWidth / 2f, cy + dp(150));
        paint.setColor(surface);
        canvas.drawRoundRect(panel, dp(24), dp(24), paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(text);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(sp(28));
        canvas.drawText("Almost!", cx, cy - dp(88), textPaint);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT);
        textPaint.setTextSize(sp(13));
        textPaint.setColor(muted);
        canvas.drawText("The board is still solvable. Try again or", cx, cy - dp(58), textPaint);
        canvas.drawText("use one rewarded continue.", cx, cy - dp(39), textPaint);

        float bw = panelWidth - dp(52);
        retryButton.set(cx - bw / 2f, cy - dp(8), cx + bw / 2f, cy + dp(42));
        continueButton.set(cx - bw / 2f, cy + dp(54), cx + bw / 2f, cy + dp(104));
        paint.setColor(surface2);
        canvas.drawRoundRect(retryButton, dp(14), dp(14), paint);
        paint.setColor(mint);
        canvas.drawRoundRect(continueButton, dp(14), dp(14), paint);

        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(sp(14));
        textPaint.setColor(text);
        canvas.drawText("RETRY LEVEL", cx, retryButton.centerY() + dp(5), textPaint);
        textPaint.setColor(background);
        canvas.drawText("WATCH AD • CONTINUE", cx, continueButton.centerY() + dp(5), textPaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        float x = event.getX(), y = event.getY();
        if (privacyButton.contains(x, y)) {
            if (listener != null) listener.onPrivacyRequested();
            return true;
        }
        if (gameOver) {
            if (retryButton.contains(x, y)) restartLevel();
            else if (continueButton.contains(x, y) && listener != null) {
                inputLocked = true;
                listener.onRewardedContinueRequested();
            }
            return true;
        }
        if (inputLocked || !boardRect.contains(x, y)) return true;
        ArrowPiece tapped = findPieceAt(x, y);
        if (tapped == null) return true;

        if (isClear(tapped)) {
            tapped.removed = true;
            coins += 2;
            prefs.edit().putInt(KEY_COINS, coins).apply();
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            invalidate();
            if (allRemoved()) {
                inputLocked = true;
                coins += 8;
                prefs.edit().putInt(KEY_COINS, coins).apply();
                postDelayed(() -> {
                    if (listener != null) listener.onLevelFinished(level);
                    else advanceToNextLevel();
                }, 280);
            }
        } else {
            hearts--;
            lastTapX = x;
            lastTapY = y;
            wrongFlashUntil = SystemClock.uptimeMillis() + 260;
            performHapticFeedback(HapticFeedbackConstants.REJECT);
            if (hearts <= 0) {
                hearts = 0;
                gameOver = true;
                inputLocked = true;
            }
            invalidate();
        }
        return true;
    }

    private ArrowPiece findPieceAt(float x, float y) {
        float innerPad = dp(12), gridLeft = boardRect.left + innerPad, gridTop = boardRect.top + innerPad;
        float gridSizePx = boardRect.width() - innerPad * 2, cell = gridSizePx / puzzle.gridSize;
        int col = (int) ((x - gridLeft) / cell), row = (int) ((y - gridTop) / cell);
        if (row < 0 || row >= puzzle.gridSize || col < 0 || col >= puzzle.gridSize) return null;
        for (ArrowPiece p : puzzle.pieces) if (!p.removed && p.row == row && p.col == col) return p;
        return null;
    }

    private boolean isClear(ArrowPiece target) {
        boolean[][] occupied = new boolean[puzzle.gridSize][puzzle.gridSize];
        for (ArrowPiece p : puzzle.pieces) if (!p.removed && p != target) occupied[p.row][p.col] = true;
        return PuzzleGenerator.rayIsClear(target.row, target.col, target.direction, occupied, puzzle.gridSize);
    }

    private boolean allRemoved() {
        for (ArrowPiece p : puzzle.pieces) if (!p.removed) return false;
        return true;
    }

    private float dp(float value) { return value * density; }
    private float sp(float value) { return value * getResources().getDisplayMetrics().scaledDensity; }
}
