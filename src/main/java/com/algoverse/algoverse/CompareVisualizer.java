package com.algoverse.algoverse;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.function.Consumer;

public class CompareVisualizer extends VBox {

    private static final String[] SORTS = {
            "Bubble Sort", "Selection Sort", "Insertion Sort", "Merge Sort", "Quick Sort",
            "Shell Sort", "Heap Sort", "Cocktail Shaker Sort", "Counting Sort", "Radix Sort",
            "Gnome Sort", "Odd-Even Sort"
    };

    private final Consumer<String> onAlgorithmSelected;
    private final Canvas canvasA = new Canvas();
    private final Canvas canvasB = new Canvas();
    private final GraphicsContext gcA = canvasA.getGraphicsContext2D();
    private final GraphicsContext gcB = canvasB.getGraphicsContext2D();
    private final PseudocodePane codeA = new PseudocodePane();
    private final PseudocodePane codeB = new PseudocodePane();
    private final Label titleA = laneTitle();
    private final Label titleB = laneTitle();

    private int[] array;
    private SortingVisualizer.SortPlayback playA;
    private SortingVisualizer.SortPlayback playB;
    private int iA;
    private int iB;
    private boolean isPlaying;
    private long lastUpdate;
    private long speedDelay = 100_000_000L;
    private AnimationTimer timer;
    private ComboBox<String> algoA;
    private ComboBox<String> algoB;
    private TextField sizeInput;
    private Label statsLabel;
    private VBox stepLogBox;
    private ScrollPane logScroll;
    private boolean loggedDone;

    public CompareVisualizer(Consumer<String> onAlgorithmSelected) {
        this.onAlgorithmSelected = onAlgorithmSelected;
        codeA.compact();
        codeB.compact();

        array = randomArray(24);
        rebuild();

        VBox laneA = lane(titleA, canvasA, codeA, this::drawA);
        VBox laneB = lane(titleB, canvasB, codeB, this::drawB);
        HBox stage = new HBox(laneA, laneB);
        stage.setStyle("-fx-background-color: #0A0E1A;");
        HBox.setHgrow(laneA, Priority.ALWAYS);
        HBox.setHgrow(laneB, Priority.ALWAYS);
        VBox.setVgrow(stage, Priority.ALWAYS);

        ScrollPane controlScroll = new ScrollPane(createControlPanel());
        controlScroll.setFitToHeight(true);
        controlScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        controlScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        controlScroll.setStyle(UiKit.scrollBarStyle());

        getChildren().addAll(stage, controlScroll, createTerminalPanel());
        setStyle("-fx-background-color: #0A0E1A;");
        setupTimer();
        if (onAlgorithmSelected != null) onAlgorithmSelected.accept("Compare Sorts");
        appendLog("Same array on both lanes. Pick two sorts, then Play both.", "#00E5FF");
    }

    public void stop() {
        isPlaying = false;
        if (timer != null) timer.stop();
    }

    private VBox lane(Label title, Canvas canvas, PseudocodePane code, Runnable draw) {
        Pane holder = UiKit.bindCanvas(canvas, draw);
        VBox box = new VBox(title, holder, code);
        box.setStyle("-fx-background-color: #0A0E1A; -fx-border-color: #1E293B; -fx-border-width: 0 1 0 0;");
        VBox.setVgrow(holder, Priority.ALWAYS);
        return box;
    }

    private static Label laneTitle() {
        Label title = new Label();
        title.setStyle("""
                -fx-text-fill: #00E5FF;
                -fx-font-family: Monospace;
                -fx-font-size: 13;
                -fx-padding: 8 12;
                -fx-border-color: #1E293B;
                -fx-border-width: 0 0 1 0;
                """);
        title.setMaxWidth(Double.MAX_VALUE);
        return title;
    }

    private int[] randomArray(int n) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = 20 + (int) (Math.random() * 400);
        return arr;
    }

    private void rebuild() {
        int n = parseSize();
        if (array == null || array.length != n) array = randomArray(n);
        String a = algoA != null ? algoA.getValue() : "Bubble Sort";
        String b = algoB != null ? algoB.getValue() : "Quick Sort";
        playA = SortingVisualizer.playback(a, array);
        playB = SortingVisualizer.playback(b, array);
        iA = 0;
        iB = 0;
        isPlaying = false;
        loggedDone = false;
        if (codeA != null) codeA.load(a);
        if (codeB != null) codeB.load(b);
        paint();
    }

    private int parseSize() {
        try {
            int n = Integer.parseInt(sizeInput.getText().trim());
            return Math.min(50, Math.max(8, n));
        } catch (Exception e) {
            return 24;
        }
    }

    private int[][] idle() {
        return new int[][]{array.clone(), new int[]{}, new int[]{}, new int[]{}};
    }

    private int[][] frame(SortingVisualizer.SortPlayback play, int i) {
        if (i <= 0 || play.frames().isEmpty()) return idle();
        return play.frames().get(Math.min(i, play.frames().size()) - 1);
    }

    private int stat(SortingVisualizer.SortPlayback play, int i, int idx) {
        if (i <= 0 || play.stats().isEmpty()) return 0;
        int[] s = play.stats().get(Math.min(i, play.stats().size()) - 1);
        return s[idx];
    }

    private void paint() {
        drawA();
        drawB();
        String a = algoA != null ? algoA.getValue() : "Bubble Sort";
        String b = algoB != null ? algoB.getValue() : "Quick Sort";
        titleA.setText("A  ·  " + a + "  ·  " + iA + "/" + playA.frames().size() + " steps");
        titleB.setText("B  ·  " + b + "  ·  " + iB + "/" + playB.frames().size() + " steps");
        if (iA > 0 && iA - 1 < playA.lines().size()) codeA.highlight(playA.lines().get(iA - 1));
        else codeA.highlight(-1);
        if (iB > 0 && iB - 1 < playB.lines().size()) codeB.highlight(playB.lines().get(iB - 1));
        else codeB.highlight(-1);
        String done = (iA >= playA.frames().size() ? "A done" : "A running")
                + " · " + (iB >= playB.frames().size() ? "B done" : "B running");
        if (statsLabel != null) {
            statsLabel.setText("A cmp " + stat(playA, iA, 0) + "  ·  B cmp " + stat(playB, iB, 0) + "\n" + done);
        }
    }

    private void drawA() { drawBars(gcA, canvasA, frame(playA, iA)); }
    private void drawB() { drawBars(gcB, canvasB, frame(playB, iB)); }

    private void drawBars(GraphicsContext gc, Canvas canvas, int[][] step) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        int[] values = step[0];
        if (w < 8 || h < 8 || values == null || values.length == 0) return;
        gc.setFill(Color.web("#0A0E1A"));
        gc.fillRect(0, 0, w, h);
        double barWidth = (w / values.length) - 2;
        int max = 1;
        for (int v : values) if (v > max) max = v;
        for (int i = 0; i < values.length; i++) {
            double barHeight = (values[i] / (double) max) * (h - 8);
            gc.setFill(barColor(i, step, values.length));
            gc.fillRect(i * (barWidth + 2), h - barHeight, barWidth, barHeight);
        }
    }

    private Color barColor(int index, int[][] step, int n) {
        for (int s : step[3]) if (s == index) return Color.web("#00FF88");
        for (int s : step[2]) if (s == index) return Color.web("#FF4444");
        for (int c : step[1]) if (c == index) return Color.web("#FFD700");
        return Color.web("#00E5FF").interpolate(Color.web("#A78BFA"), (double) index / n);
    }

    private boolean stepBoth() {
        boolean moved = false;
        if (iA < playA.frames().size()) { iA++; moved = true; }
        if (iB < playB.frames().size()) { iB++; moved = true; }
        paint();
        if (!loggedDone && iA >= playA.frames().size() && iB >= playB.frames().size()) {
            loggedDone = true;
            isPlaying = false;
            String winner = playA.frames().size() == playB.frames().size()
                    ? "Tie in step count."
                    : (playA.frames().size() < playB.frames().size()
                    ? "A finished in fewer steps."
                    : "B finished in fewer steps.");
            appendLog("Done. A " + playA.frames().size() + " steps / " + stat(playA, iA, 0)
                    + " cmp. B " + playB.frames().size() + " steps / " + stat(playB, iB, 0)
                    + " cmp. " + winner, "#00FF88");
        }
        return moved;
    }

    private void setupTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isPlaying) return;
                if (now - lastUpdate < speedDelay) return;
                lastUpdate = now;
                if (!stepBoth()) isPlaying = false;
            }
        };
        timer.start();
    }

    private void appendLog(String message, String color) {
        Label entry = new Label(message);
        entry.setWrapText(true);
        entry.setStyle(String.format("""
                -fx-text-fill: %s;
                -fx-font-family: Monospace;
                -fx-font-size: 13;
                -fx-padding: 1 8;
                """, color));
        stepLogBox.getChildren().add(entry);
        if (logScroll != null) logScroll.setVvalue(1.0);
    }

    private VBox createTerminalPanel() {
        Label title = new Label("[ STEP LOG ]");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        title.setStyle("-fx-text-fill: #00E5FF; -fx-padding: 6 14;");
        HBox header = new HBox(title);
        header.setStyle("-fx-background-color: #111827; -fx-border-color: #00E5FF; -fx-border-width: 1 0 0 0;");
        stepLogBox = new VBox(3);
        stepLogBox.setStyle("-fx-background-color: #060912; -fx-padding: 8 14;");
        logScroll = new ScrollPane(stepLogBox);
        logScroll.setFitToWidth(true);
        logScroll.setPrefHeight(140);
        logScroll.setStyle("-fx-background: #060912; -fx-background-color: #060912;");
        return new VBox(header, logScroll);
    }

    private HBox createControlPanel() {
        HBox panel = new HBox(12);
        panel.setStyle(UiKit.controlBarStyle());

        algoA = new ComboBox<>();
        algoA.getItems().addAll(SORTS);
        algoA.setValue("Bubble Sort");
        UiKit.styleCombo(algoA);
        algoA.setOnAction(e -> rebuild());

        algoB = new ComboBox<>();
        algoB.getItems().addAll(SORTS);
        algoB.setValue("Quick Sort");
        UiKit.styleCombo(algoB);
        algoB.setOnAction(e -> rebuild());

        sizeInput = UiKit.darkField("24", 50);

        Button shuffle = UiKit.controlButton("🔀  Shuffle", "#00E5FF");
        Button play = UiKit.controlButton("▶️  Play both", "#00E5FF");
        Button pause = UiKit.controlButton("⏸️  Pause", "#A78BFA");
        Button step = UiKit.controlButton("⏩  Step both", "#A78BFA");
        Button reset = UiKit.controlButton("🔄  Reset", "#FF4444");

        shuffle.setOnAction(e -> {
            array = randomArray(parseSize());
            rebuild();
            appendLog("Shuffled a shared array of " + array.length + ".", "#00E5FF");
        });
        play.setOnAction(e -> isPlaying = true);
        pause.setOnAction(e -> isPlaying = false);
        step.setOnAction(e -> {
            isPlaying = false;
            stepBoth();
        });
        reset.setOnAction(e -> {
            rebuild();
            stepLogBox.getChildren().clear();
            appendLog("Reset both lanes to the same array.", "#A78BFA");
        });

        Slider speed = new Slider(1, 10, 6);
        speed.setPrefWidth(110);
        speed.valueProperty().addListener((o, a, n) ->
                speedDelay = (long) ((11 - n.doubleValue()) * 45_000_000L));

        statsLabel = UiKit.label("A cmp 0  ·  B cmp 0");
        VBox stats = new VBox(4, statsLabel);
        VBox sizeBox = new VBox(4, UiKit.label("Size:"), sizeInput);
        VBox speedBox = new VBox(4, UiKit.label("Speed:"), speed);

        panel.getChildren().addAll(
                new VBox(4, UiKit.label("A"), algoA),
                new VBox(4, UiKit.label("B"), algoB),
                UiKit.separator(),
                shuffle, play, pause, step, reset,
                UiKit.separator(), sizeBox, speedBox, UiKit.separator(), stats
        );
        return panel;
    }
}
