package com.algoverse.algoverse;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.function.Consumer;

public class PathfindingVisualizer extends VBox {

    private static final int ROWS = 15;
    private static final int COLS = 41;

    private static final int VISIT = 0;
    private static final int PATH = 1;
    private static final int DONE_OK = 2;
    private static final int DONE_FAIL = 3;

    private static final int[][] DIRS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Consumer<String> onAlgorithmSelected;

    private final int[][] walls = new int[ROWS][COLS];
    private final int[][] weight = new int[ROWS][COLS];
    private int startR = 7, startC = 3;
    private int endR = 7, endC = 37;

    private final boolean[][] visited = new boolean[ROWS][COLS];
    private final boolean[][] inPath = new boolean[ROWS][COLS];
    private int cursorR = -1, cursorC = -1;

    private final List<int[]> steps = new ArrayList<>();
    private final List<String> stepMessages = new ArrayList<>();
    private final List<String> stepColors = new ArrayList<>();
    private int currentStep = 0;
    private boolean isPlaying = false;
    private boolean dirty = true;

    private long lastUpdate = 0;
    private long speedDelay = 40_000_000L;

    private int visitedShown = 0;
    private int pathShown = 0;
    private Label visitedLabel;
    private Label pathLabel;
    private VBox stepLogBox;
    private ScrollPane logScroll;
    private int stepNumber = 0;
    private AnimationTimer timer;
    private final List<Integer> codeLineAtStep = new ArrayList<>();
    private PseudocodePane codePane;
    private ComboBox<String> algoSelector;

    private boolean draggingStart;
    private boolean draggingEnd;
    private boolean painting;
    private boolean paintWall;

    public PathfindingVisualizer(Consumer<String> onAlgorithmSelected) {
        this.onAlgorithmSelected = onAlgorithmSelected;

        canvas = new Canvas();
        gc = canvas.getGraphicsContext2D();
        fillWeights(1);
        installMouseHandlers();

        generateMaze();

        codePane = new PseudocodePane();
        codePane.load("BFS");
        Pane canvasHolder = UiKit.bindCanvas(canvas, this::drawGrid);
        HBox stage = new HBox(canvasHolder, codePane);
        stage.setStyle("-fx-background-color: #0A0E1A; -fx-padding: 16 0 0 16;");
        HBox.setHgrow(canvasHolder, Priority.ALWAYS);

        VBox terminalPanel = createTerminalPanel();
        ScrollPane controlScroll = new ScrollPane(createControlPanel());
        controlScroll.setFitToHeight(true);
        controlScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        controlScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        controlScroll.setStyle(UiKit.scrollBarStyle());

        getChildren().addAll(stage, controlScroll, terminalPanel);
        setStyle("-fx-background-color: #0A0E1A;");
        VBox.setVgrow(stage, Priority.ALWAYS);

        setupAnimationTimer();
        if (onAlgorithmSelected != null) onAlgorithmSelected.accept("BFS");
        appendLog("◉ Draw walls with click-drag. Drag cyan start / red end. Then Play.", "#00E5FF");
    }

    public void stop() {
        isPlaying = false;
        if (timer != null) timer.stop();
    }

    // ─── Grid helpers ────────────────────────────────────────────────────────

    private void fillWeights(int value) {
        for (int r = 0; r < ROWS; r++) Arrays.fill(weight[r], value);
    }

    private int stepCost(int r, int c) {
        return Math.max(1, weight[r][c]);
    }

    private double cellSize() {
        return Math.max(8, Math.min(canvas.getWidth() / COLS, canvas.getHeight() / ROWS));
    }

    private double offsetX() {
        return (canvas.getWidth() - cellSize() * COLS) / 2;
    }

    private double offsetY() {
        return (canvas.getHeight() - cellSize() * ROWS) / 2;
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    private boolean isStart(int r, int c) {
        return r == startR && c == startC;
    }

    private boolean isEnd(int r, int c) {
        return r == endR && c == endC;
    }

    private void clearOverlay() {
        for (int r = 0; r < ROWS; r++) {
            Arrays.fill(visited[r], false);
            Arrays.fill(inPath[r], false);
        }
        cursorR = -1;
        cursorC = -1;
        visitedShown = 0;
        pathShown = 0;
    }

    private void clearWalls() {
        for (int r = 0; r < ROWS; r++) Arrays.fill(walls[r], 0);
        startR = 7;
        startC = 3;
        endR = 7;
        endC = 37;
        walls[startR][startC] = 0;
        walls[endR][endC] = 0;
        fillWeights(1);
        markDirty();
    }

    private void scatterWalls() {
        clearWalls();
        Random rng = new Random();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (isStart(r, c) || isEnd(r, c)) continue;
                if (rng.nextDouble() < 0.28) walls[r][c] = 1;
            }
        }
        markDirty();
    }

    private void scatterWeights() {
        Random rng = new Random();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (walls[r][c] == 1 || isStart(r, c) || isEnd(r, c)) {
                    weight[r][c] = 1;
                } else {
                    weight[r][c] = rng.nextInt(5) + 1;
                }
            }
        }
        markDirty();
    }

    private void generateMaze() {
        for (int r = 0; r < ROWS; r++) Arrays.fill(walls[r], 1);
        boolean[][] carved = new boolean[ROWS][COLS];
        carve(1, 1, carved);
        startR = 1;
        startC = 1;
        endR = ROWS - 2;
        endC = COLS - 2;
        walls[startR][startC] = 0;
        walls[endR][endC] = 0;
        fillWeights(1);
        markDirty();
    }

    private void carve(int r, int c, boolean[][] carved) {
        carved[r][c] = true;
        walls[r][c] = 0;
        int[][] dirs = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};
        Collections.shuffle(Arrays.asList(dirs));
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (inBounds(nr, nc) && !carved[nr][nc]) {
                walls[r + d[0] / 2][c + d[1] / 2] = 0;
                carve(nr, nc, carved);
            }
        }
    }

    private void markDirty() {
        dirty = true;
        isPlaying = false;
        currentStep = 0;
        steps.clear();
        stepMessages.clear();
        stepColors.clear();
        codeLineAtStep.clear();
        clearOverlay();
        drawGrid();
        if (visitedLabel != null) {
            visitedLabel.setText("Visited: 0");
            pathLabel.setText("Path: 0");
        }
    }

    private int[] cellAt(MouseEvent e) {
        double size = cellSize();
        if (size <= 0) return null;
        int c = (int) Math.floor((e.getX() - offsetX()) / size);
        int r = (int) Math.floor((e.getY() - offsetY()) / size);
        if (!inBounds(r, c)) return null;
        return new int[]{r, c};
    }

    private void installMouseHandlers() {
        canvas.setOnMousePressed(e -> {
            int[] cell = cellAt(e);
            if (cell == null) return;
            int r = cell[0], c = cell[1];
            if (isStart(r, c) && e.getButton() == MouseButton.PRIMARY) {
                draggingStart = true;
                return;
            }
            if (isEnd(r, c) && e.getButton() == MouseButton.PRIMARY) {
                draggingEnd = true;
                return;
            }
            painting = true;
            paintWall = e.getButton() != MouseButton.SECONDARY && walls[r][c] == 0;
            toggleWall(r, c, paintWall);
        });
        canvas.setOnMouseDragged(e -> {
            int[] cell = cellAt(e);
            if (cell == null) return;
            int r = cell[0], c = cell[1];
            if (draggingStart) {
                if (!isEnd(r, c)) {
                    walls[r][c] = 0;
                    startR = r;
                    startC = c;
                    markDirty();
                }
                return;
            }
            if (draggingEnd) {
                if (!isStart(r, c)) {
                    walls[r][c] = 0;
                    endR = r;
                    endC = c;
                    markDirty();
                }
                return;
            }
            if (painting) toggleWall(r, c, paintWall);
        });
        canvas.setOnMouseReleased(e -> {
            draggingStart = false;
            draggingEnd = false;
            painting = false;
        });
    }

    private void toggleWall(int r, int c, boolean makeWall) {
        if (isStart(r, c) || isEnd(r, c)) return;
        int next = makeWall ? 1 : 0;
        if (walls[r][c] == next) return;
        walls[r][c] = next;
        markDirty();
    }

    // ─── Step generation ─────────────────────────────────────────────────────

    private void ensureSteps() {
        if (!dirty && !steps.isEmpty()) return;
        generateSteps(algoSelector.getValue());
        dirty = false;
        currentStep = 0;
        clearOverlay();
        drawGrid();
    }

    private void generateSteps(String algorithm) {
        steps.clear();
        stepMessages.clear();
        stepColors.clear();
        codeLineAtStep.clear();
        currentStep = 0;
        if (codePane != null) codePane.load(algorithm);
        switch (algorithm) {
            case "DFS" -> runStackSearch();
            case "Dijkstra" -> runDijkstra();
            case "A*" -> runAStar(true);
            case "Greedy Best-First" -> runAStar(false);
            default -> runBfs();
        }
    }

    private void addStep(int type, int r, int c, String msg, String color) {
        steps.add(new int[]{type, r, c});
        stepMessages.add(msg);
        stepColors.add(color);
        String event = switch (type) {
            case VISIT -> (r == startR && c == startC) ? "start" : "visit";
            case PATH -> "path";
            case DONE_OK -> "done";
            default -> "fail";
        };
        String algo = algoSelector != null ? algoSelector.getValue() : "BFS";
        codeLineAtStep.add(Pseudocode.line(algo, event));
    }

    private List<int[]> neighbors(int r, int c) {
        List<int[]> out = new ArrayList<>();
        for (int[] d : DIRS) {
            int nr = r + d[0], nc = c + d[1];
            if (inBounds(nr, nc) && walls[nr][nc] == 0) out.add(new int[]{nr, nc});
        }
        return out;
    }

    private int manhattan(int r, int c) {
        return Math.abs(r - endR) + Math.abs(c - endC);
    }

    private void reconstruct(int[][] parentR, int[][] parentC) {
        List<int[]> path = new ArrayList<>();
        int r = endR, c = endC;
        while (!(r == startR && c == startC)) {
            path.add(new int[]{r, c});
            int pr = parentR[r][c];
            int pc = parentC[r][c];
            if (pr < 0) return;
            r = pr;
            c = pc;
        }
        Collections.reverse(path);
        for (int[] p : path) {
            addStep(PATH, p[0], p[1],
                    "Path  →  (" + p[0] + "," + p[1] + ")", "#00FF88");
        }
        addStep(DONE_OK, endR, endC,
                "Path found — length " + path.size() + ".", "#00FF88");
    }

    private void runBfs() {
        boolean[][] seen = new boolean[ROWS][COLS];
        int[][] parentR = fillNeg();
        int[][] parentC = fillNeg();
        ArrayDeque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{startR, startC});
        seen[startR][startC] = true;
        addStep(VISIT, startR, startC, "Start at (" + startR + "," + startC + ")", "#A78BFA");

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int r = cur[0], c = cur[1];
            if (r == endR && c == endC) {
                reconstruct(parentR, parentC);
                return;
            }
            for (int[] n : neighbors(r, c)) {
                if (seen[n[0]][n[1]]) continue;
                seen[n[0]][n[1]] = true;
                parentR[n[0]][n[1]] = r;
                parentC[n[0]][n[1]] = c;
                q.add(n);
                addStep(VISIT, n[0], n[1],
                        "Visit  (" + n[0] + "," + n[1] + ")", "#A78BFA");
            }
        }
        addStep(DONE_FAIL, -1, -1, "No path — the grid is blocked.", "#FF4444");
    }

    private void runStackSearch() {
        boolean[][] seen = new boolean[ROWS][COLS];
        int[][] parentR = fillNeg();
        int[][] parentC = fillNeg();
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{startR, startC});
        seen[startR][startC] = true;
        addStep(VISIT, startR, startC, "Start at (" + startR + "," + startC + ")", "#A78BFA");

        while (!stack.isEmpty()) {
            int[] cur = stack.pop();
            int r = cur[0], c = cur[1];
            if (r == endR && c == endC) {
                reconstruct(parentR, parentC);
                return;
            }
            for (int[] n : neighbors(r, c)) {
                if (seen[n[0]][n[1]]) continue;
                seen[n[0]][n[1]] = true;
                parentR[n[0]][n[1]] = r;
                parentC[n[0]][n[1]] = c;
                stack.push(n);
                addStep(VISIT, n[0], n[1],
                        "Visit  (" + n[0] + "," + n[1] + ")", "#A78BFA");
            }
        }
        addStep(DONE_FAIL, -1, -1, "No path — the grid is blocked.", "#FF4444");
    }

    private void runDijkstra() {
        int[][] dist = new int[ROWS][COLS];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
        int[][] parentR = fillNeg();
        int[][] parentC = fillNeg();
        boolean[][] seen = new boolean[ROWS][COLS];
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        dist[startR][startC] = 0;
        pq.add(new int[]{0, startR, startC});
        addStep(VISIT, startR, startC, "Start at (" + startR + "," + startC + ")", "#A78BFA");

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int r = cur[1], c = cur[2];
            if (seen[r][c]) continue;
            seen[r][c] = true;
            if (!(r == startR && c == startC)) {
                addStep(VISIT, r, c, "Pop  (" + r + "," + c + ")  dist=" + dist[r][c], "#A78BFA");
            }
            if (r == endR && c == endC) {
                reconstruct(parentR, parentC);
                return;
            }
            for (int[] n : neighbors(r, c)) {
                int nd = dist[r][c] + stepCost(n[0], n[1]);
                if (nd < dist[n[0]][n[1]]) {
                    dist[n[0]][n[1]] = nd;
                    parentR[n[0]][n[1]] = r;
                    parentC[n[0]][n[1]] = c;
                    pq.add(new int[]{nd, n[0], n[1]});
                }
            }
        }
        addStep(DONE_FAIL, -1, -1, "No path — the grid is blocked.", "#FF4444");
    }

    private void runAStar(boolean withG) {
        int[][] gScore = new int[ROWS][COLS];
        for (int[] row : gScore) Arrays.fill(row, Integer.MAX_VALUE);
        int[][] parentR = fillNeg();
        int[][] parentC = fillNeg();
        boolean[][] closed = new boolean[ROWS][COLS];
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        gScore[startR][startC] = 0;
        pq.add(new int[]{manhattan(startR, startC), startR, startC});
        addStep(VISIT, startR, startC, "Start at (" + startR + "," + startC + ")", "#A78BFA");

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int r = cur[1], c = cur[2];
            if (closed[r][c]) continue;
            closed[r][c] = true;
            if (!(r == startR && c == startC)) {
                addStep(VISIT, r, c,
                        "Expand  (" + r + "," + c + ")  h=" + manhattan(r, c), "#A78BFA");
            }
            if (r == endR && c == endC) {
                reconstruct(parentR, parentC);
                return;
            }
            for (int[] n : neighbors(r, c)) {
                int tg = withG ? gScore[r][c] + stepCost(n[0], n[1]) : 0;
                if (withG && tg >= gScore[n[0]][n[1]]) continue;
                if (!withG && gScore[n[0]][n[1]] != Integer.MAX_VALUE) continue;
                gScore[n[0]][n[1]] = withG ? tg : 1;
                parentR[n[0]][n[1]] = r;
                parentC[n[0]][n[1]] = c;
                int f = (withG ? tg : 0) + manhattan(n[0], n[1]);
                pq.add(new int[]{f, n[0], n[1]});
            }
        }
        addStep(DONE_FAIL, -1, -1, "No path — the grid is blocked.", "#FF4444");
    }

    private int[][] fillNeg() {
        int[][] a = new int[ROWS][COLS];
        for (int[] row : a) Arrays.fill(row, -1);
        return a;
    }

    // ─── Draw / play ─────────────────────────────────────────────────────────

    private void drawGrid() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w < 8 || h < 8) return;
        gc.setFill(Color.web("#0A0E1A"));
        gc.fillRect(0, 0, w, h);

        double size = cellSize();
        double ox = offsetX();
        double oy = offsetY();

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                double x = ox + c * size;
                double y = oy + r * size;
                Color fill;
                if (isStart(r, c)) fill = Color.web("#00E5FF");
                else if (isEnd(r, c)) fill = Color.web("#FF4444");
                else if (inPath[r][c]) fill = Color.web("#00FF88");
                else if (walls[r][c] == 1) fill = Color.web("#1E293B");
                else if (visited[r][c]) fill = Color.web("#A78BFA").deriveColor(0, 1, 1, 0.55);
                else fill = Color.web("#0F172A").interpolate(Color.web("#475569"), (stepCost(r, c) - 1) / 4.0);

                gc.setFill(fill);
                gc.fillRect(x + 1, y + 1, size - 2, size - 2);

                if (walls[r][c] == 0 && !isStart(r, c) && !isEnd(r, c) && weight[r][c] > 1 && size >= 16) {
                    gc.setFill(Color.web("#E2E8F0"));
                    gc.fillText(String.valueOf(weight[r][c]), x + size / 2 - 4, y + size / 2 + 4);
                }

                if (r == cursorR && c == cursorC) {
                    gc.setStroke(Color.web("#FFD700"));
                    gc.setLineWidth(2);
                    gc.strokeRect(x + 2, y + 2, size - 4, size - 4);
                }
            }
        }
    }

    private void setupAnimationTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isPlaying) return;
                if (now - lastUpdate < speedDelay) return;
                lastUpdate = now;
                stepForward();
            }
        };
        timer.start();
    }

    private void stepForward() {
        ensureSteps();
        if (currentStep >= steps.size()) {
            isPlaying = false;
            return;
        }
        int[] step = steps.get(currentStep);
        int type = step[0], r = step[1], c = step[2];
        if (type == VISIT && r >= 0) {
            visited[r][c] = true;
            cursorR = r;
            cursorC = c;
            visitedShown++;
        } else if (type == PATH && r >= 0) {
            inPath[r][c] = true;
            cursorR = r;
            cursorC = c;
            pathShown++;
        } else {
            cursorR = -1;
            cursorC = -1;
            isPlaying = false;
        }
        drawGrid();
        visitedLabel.setText("Visited: " + visitedShown);
        pathLabel.setText("Path: " + pathShown);
        stepNumber++;
        appendLog("Step " + String.format("%4d", stepNumber) + "  →  " + stepMessages.get(currentStep),
                stepColors.get(currentStep));
        if (currentStep < codeLineAtStep.size() && codePane != null) {
            codePane.highlight(codeLineAtStep.get(currentStep));
        }
        currentStep++;
    }

    private void stepBackward() {
        if (currentStep <= 1) return;
        isPlaying = false;
        int target = currentStep - 1;
        currentStep = 0;
        stepNumber = 0;
        clearOverlay();
        stepLogBox.getChildren().clear();
        for (int i = 0; i < target; i++) stepForward();
        isPlaying = false;
    }

    private void resetPlayback() {
        isPlaying = false;
        currentStep = 0;
        stepNumber = 0;
        clearOverlay();
        drawGrid();
        visitedLabel.setText("Visited: 0");
        pathLabel.setText("Path: 0");
        if (codePane != null) codePane.highlight(-1);
        clearLog();
    }

    // ─── UI ──────────────────────────────────────────────────────────────────

    private void appendLog(String message, String color) {
        Label entry = new Label(message);
        entry.setStyle(String.format("""
                -fx-text-fill: %s;
                -fx-font-family: Monospace;
                -fx-font-size: 13;
                -fx-padding: 1 8;
                """, color));
        entry.setMaxWidth(Double.MAX_VALUE);
        stepLogBox.getChildren().add(entry);
        if (logScroll != null) logScroll.setVvalue(1.0);
    }

    private void clearLog() {
        stepLogBox.getChildren().clear();
        stepNumber = 0;
        appendLog("▶  Ready — paint walls, then press Play.", "#A78BFA");
    }

    private VBox createTerminalPanel() {
        HBox header = new HBox(10);
        header.setStyle("""
                -fx-background-color: #111827;
                -fx-padding: 6 14;
                -fx-border-color: #00E5FF;
                -fx-border-width: 1 0 0 0;
                """);
        Label title = new Label("[ STEP LOG ]");
        title.setStyle("""
                -fx-text-fill: #00E5FF;
                -fx-font-family: Monospace;
                -fx-font-size: 13;
                -fx-font-weight: bold;
                """);
        Label legend = new Label("   cyan start    red end    numbers = move cost    purple visited    green path");
        legend.setStyle("-fx-text-fill: #4B5563; -fx-font-family: Monospace; -fx-font-size: 12;");
        header.getChildren().addAll(title, legend);

        stepLogBox = new VBox(3);
        stepLogBox.setStyle("-fx-background-color: #060912; -fx-padding: 8 14;");
        appendLog("▶  Ready — paint walls, then press Play.", "#A78BFA");

        logScroll = new ScrollPane(stepLogBox);
        logScroll.setFitToWidth(true);
        logScroll.setPrefHeight(160);
        logScroll.setStyle("-fx-background: #060912; -fx-background-color: #060912;");
        logScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return new VBox(header, logScroll);
    }

    private HBox createControlPanel() {
        HBox panel = new HBox(16);
        panel.setStyle(UiKit.controlBarStyle());

        algoSelector = new ComboBox<>();
        algoSelector.getItems().addAll("BFS", "DFS", "Dijkstra", "A*", "Greedy Best-First");
        algoSelector.setValue("BFS");
        UiKit.styleCombo(algoSelector);
        algoSelector.setOnAction(e -> {
            markDirty();
            clearLog();
            appendLog("◉ Algorithm → " + algoSelector.getValue() + ". Press Play.", "#00E5FF");
            if (onAlgorithmSelected != null) onAlgorithmSelected.accept(algoSelector.getValue());
        });

        Button mazeBtn = UiKit.controlButton("🧱  Maze", "#00E5FF");
        Button scatterBtn = UiKit.controlButton("🎲  Walls", "#00E5FF");
        Button weightBtn = UiKit.controlButton("🔢  Weights", "#A78BFA");
        Button clearBtn = UiKit.controlButton("🧹  Clear", "#A78BFA");
        Button playBtn = UiKit.controlButton("▶️  Play", "#00E5FF");
        Button pauseBtn = UiKit.controlButton("⏸️  Pause", "#A78BFA");
        Button stepFwdBtn = UiKit.controlButton("⏩  Forward", "#A78BFA");
        Button stepBwdBtn = UiKit.controlButton("⏪  Backward", "#A78BFA");
        Button resetBtn = UiKit.controlButton("🔄  Reset", "#FF4444");

        mazeBtn.setOnAction(e -> {
            generateMaze();
            clearLog();
            appendLog("◉ Recursive-backtracker maze generated.", "#00E5FF");
        });
        scatterBtn.setOnAction(e -> {
            scatterWalls();
            clearLog();
            appendLog("◉ Random walls scattered.", "#00E5FF");
        });
        weightBtn.setOnAction(e -> {
            scatterWeights();
            clearLog();
            appendLog("◉ Terrain weights 1–5 added. Dijkstra/A* use cost; BFS ignores it.", "#00E5FF");
        });
        clearBtn.setOnAction(e -> {
            clearWalls();
            clearLog();
            appendLog("◉ Grid cleared.", "#00E5FF");
        });
        playBtn.setOnAction(e -> {
            ensureSteps();
            isPlaying = true;
        });
        pauseBtn.setOnAction(e -> isPlaying = false);
        stepFwdBtn.setOnAction(e -> {
            isPlaying = false;
            stepForward();
        });
        stepBwdBtn.setOnAction(e -> stepBackward());
        resetBtn.setOnAction(e -> resetPlayback());

        Label speedLabel = UiKit.label("Speed:");
        Slider speedSlider = new Slider(1, 10, 6);
        speedSlider.setPrefWidth(120);
        speedSlider.valueProperty().addListener((obs, o, n) ->
                speedDelay = (long) ((11 - n.doubleValue()) * 12_000_000L));

        visitedLabel = UiKit.label("Visited: 0");
        pathLabel = UiKit.label("Path: 0");
        VBox stats = new VBox(4, visitedLabel, pathLabel);
        stats.setStyle("-fx-alignment: center-left;");

        VBox speedBox = new VBox(4, speedLabel, speedSlider);
        speedBox.setStyle("-fx-alignment: center;");

        panel.getChildren().addAll(
                algoSelector, UiKit.separator(),
                mazeBtn, scatterBtn, weightBtn, clearBtn,
                playBtn, pauseBtn, stepBwdBtn, stepFwdBtn, resetBtn,
                UiKit.separator(), speedBox, UiKit.separator(), stats
        );
        return panel;
    }
}
