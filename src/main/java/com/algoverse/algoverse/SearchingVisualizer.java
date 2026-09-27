package com.algoverse.algoverse;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SearchingVisualizer extends VBox {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Consumer<String> onAlgorithmSelected;

    private int[] array;
    private int[] comparing = {};
    private int rangeLow = -1, rangeHigh = -1;
    private int foundIndex = -1;
    private int target = 0;

    private final List<int[][]> steps = new ArrayList<>();
    private int currentStep = 0;
    private boolean isPlaying = false;
    private long lastUpdate = 0;
    private long speedDelay = 180_000_000L;

    private int comparisons = 0;
    private final List<Integer> comparisonsAtStep = new ArrayList<>();
    private final List<String> stepMessages = new ArrayList<>();
    private final List<String> stepColors = new ArrayList<>();
    private final List<Integer> codeLineAtStep = new ArrayList<>();
    private PseudocodePane codePane;

    private Label comparisonsLabel;
    private Label resultLabel;
    private VBox stepLogBox;
    private ScrollPane logScroll;
    private int stepNumber = 0;
    private AnimationTimer timer;
    private ComboBox<String> algoSelector;
    private TextField sizeInput;
    private TextField targetInput;

    public SearchingVisualizer(Consumer<String> onAlgorithmSelected) {
        this.onAlgorithmSelected = onAlgorithmSelected;

        canvas = new Canvas();
        gc = canvas.getGraphicsContext2D();

        array = generateArray(20, false);
        pickDefaultTarget();
        generateSteps("Linear Search");

        codePane = new PseudocodePane();
        codePane.load("Linear Search");
        Pane canvasHolder = UiKit.bindCanvas(canvas, this::drawArray);
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
        if (onAlgorithmSelected != null) onAlgorithmSelected.accept("Linear Search");
    }

    public void stop() {
        isPlaying = false;
        if (timer != null) timer.stop();
    }

    private int[] generateArray(int size, boolean sorted) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = (int) (Math.random() * 90) + 10;
        if (sorted) Arrays.sort(arr);
        return arr;
    }

    private boolean needsSorted(String algo) {
        return "Binary Search".equals(algo) || "Jump Search".equals(algo);
    }

    private void pickDefaultTarget() {
        target = array[(int) (Math.random() * array.length)];
        if (targetInput != null) targetInput.setText(String.valueOf(target));
    }

    private void record(int[] arr, int[] cmp, int low, int high, int found, String msg, String color) {
        steps.add(new int[][]{
                arr.clone(),
                cmp,
                {low, high},
                {found},
                new int[]{comparisons}
        });
        comparisonsAtStep.add(comparisons);
        stepMessages.add(msg);
        stepColors.add(color);
        String event = found >= 0 ? "found" : found == -2 ? "miss" : msg.contains("Jump") ? "jump" : "compare";
        String algo = algoSelector != null ? algoSelector.getValue() : "Linear Search";
        codeLineAtStep.add(Pseudocode.line(algo, event));
    }

    private void generateSteps(String algorithm) {
        steps.clear();
        stepMessages.clear();
        stepColors.clear();
        comparisonsAtStep.clear();
        codeLineAtStep.clear();
        currentStep = 0;
        comparisons = 0;
        foundIndex = -1;
        rangeLow = 0;
        rangeHigh = array.length - 1;

        if (needsSorted(algorithm)) Arrays.sort(array);
        if (codePane != null) codePane.load(algorithm);

        switch (algorithm) {
            case "Binary Search" -> generateBinary();
            case "Jump Search" -> generateJump();
            default -> generateLinear();
        }
    }

    private void generateLinear() {
        int[] arr = array.clone();
        for (int i = 0; i < arr.length; i++) {
            comparisons++;
            record(arr, new int[]{i}, 0, arr.length - 1, -1,
                    "Comparing idx " + i + " (val:" + arr[i] + ")  with target " + target, "#FFD700");
            if (arr[i] == target) {
                record(arr, new int[]{}, 0, arr.length - 1, i,
                        "Found target " + target + " at index " + i + ".", "#00FF88");
                return;
            }
        }
        record(arr, new int[]{}, 0, arr.length - 1, -2,
                "Target " + target + " is not in the array.", "#FF4444");
    }

    private void generateBinary() {
        int[] arr = array.clone();
        int low = 0, high = arr.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            comparisons++;
            record(arr, new int[]{mid}, low, high, -1,
                    "Low=" + low + "  High=" + high + "  Mid=" + mid + " (val:" + arr[mid] + ")", "#FFD700");
            if (arr[mid] == target) {
                record(arr, new int[]{}, low, high, mid,
                        "Found target " + target + " at index " + mid + ".", "#00FF88");
                return;
            }
            if (arr[mid] < target) low = mid + 1;
            else high = mid - 1;
        }
        record(arr, new int[]{}, 0, arr.length - 1, -2,
                "Target " + target + " is not in the array.", "#FF4444");
    }

    private void generateJump() {
        int[] arr = array.clone();
        int n = arr.length;
        int step = (int) Math.floor(Math.sqrt(n));
        int prev = 0;
        while (prev < n && arr[Math.min(step, n) - 1] < target) {
            comparisons++;
            int idx = Math.min(step, n) - 1;
            record(arr, new int[]{idx}, prev, Math.min(step, n) - 1, -1,
                    "Jump to idx " + idx + " (val:" + arr[idx] + ")", "#FFD700");
            prev = step;
            step += (int) Math.floor(Math.sqrt(n));
            if (prev >= n) break;
        }
        for (int i = prev; i < Math.min(step, n); i++) {
            comparisons++;
            record(arr, new int[]{i}, prev, Math.min(step, n) - 1, -1,
                    "Linear scan idx " + i + " (val:" + arr[i] + ")", "#FFD700");
            if (arr[i] == target) {
                record(arr, new int[]{}, prev, Math.min(step, n) - 1, i,
                        "Found target " + target + " at index " + i + ".", "#00FF88");
                return;
            }
        }
        record(arr, new int[]{}, 0, n - 1, -2,
                "Target " + target + " is not in the array.", "#FF4444");
    }

    private void drawArray() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w < 8 || h < 8 || array == null) return;
        gc.setFill(Color.web("#0A0E1A"));
        gc.fillRect(0, 0, w, h);

        double box = Math.min(72, (w / array.length) - 8);
        double total = array.length * (box + 8) - 8;
        double originX = (w - total) / 2;
        double y = h / 2 - box / 2;

        gc.setFill(Color.web("#94A3B8"));
        gc.setFont(Font.font("Monospace", 14));
        gc.fillText("Target: " + target, 24, 36);

        for (int i = 0; i < array.length; i++) {
            double x = originX + i * (box + 8);
            gc.setFill(getBoxColor(i));
            gc.fillRoundRect(x, y, box, box, 8, 8);

            gc.setFill(Color.web("#0A0E1A"));
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, Math.max(11, box / 3)));
            String label = String.valueOf(array[i]);
            double tw = label.length() * (box / 5.0);
            gc.fillText(label, x + box / 2 - tw / 2, y + box / 2 + 5);

            gc.setFill(Color.web("#64748B"));
            gc.setFont(Font.font("Monospace", 11));
            gc.fillText(String.valueOf(i), x + box / 2 - 4, y + box + 16);
        }
    }

    private Color getBoxColor(int index) {
        if (foundIndex >= 0 && index == foundIndex) return Color.web("#00FF88");
        for (int c : comparing) if (c == index) return Color.web("#FFD700");
        if (rangeLow >= 0 && rangeHigh >= 0 && (index < rangeLow || index > rangeHigh)) {
            return Color.web("#1E293B");
        }
        double ratio = (double) index / Math.max(1, array.length - 1);
        return Color.web("#00E5FF").interpolate(Color.web("#A78BFA"), ratio);
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
        if (currentStep >= steps.size()) {
            isPlaying = false;
            return;
        }
        int[][] step = steps.get(currentStep);
        array = step[0];
        comparing = step[1];
        rangeLow = step[2][0];
        rangeHigh = step[2][1];
        foundIndex = step[3][0];
        drawArray();
        stepNumber++;
        comparisonsLabel.setText("Comparisons: " + comparisonsAtStep.get(currentStep));
        if (foundIndex >= 0) resultLabel.setText("Result: index " + foundIndex);
        else if (foundIndex == -2) resultLabel.setText("Result: not found");
        else resultLabel.setText("Result: searching…");
        appendLog("Step " + String.format("%4d", stepNumber) + "  →  " + stepMessages.get(currentStep),
                stepColors.get(currentStep));
        if (currentStep < codeLineAtStep.size() && codePane != null) {
            codePane.highlight(codeLineAtStep.get(currentStep));
        }
        if (foundIndex >= 0 || foundIndex == -2) isPlaying = false;
        currentStep++;
    }

    private void stepBackward() {
        if (currentStep <= 1) return;
        isPlaying = false;
        int targetStep = currentStep - 1;
        currentStep = 0;
        stepNumber = 0;
        comparing = new int[]{};
        foundIndex = -1;
        if (!steps.isEmpty()) array = steps.get(0)[0].clone();
        stepLogBox.getChildren().clear();
        if (codePane != null) codePane.highlight(-1);
        for (int i = 0; i < targetStep; i++) stepForward();
        isPlaying = false;
    }

    private void rebuild(boolean reshuffle) {
        isPlaying = false;
        String algo = algoSelector.getValue();
        if (reshuffle) array = generateArray(parseSize(), needsSorted(algo));
        else if (needsSorted(algo)) Arrays.sort(array);
        parseTarget();
        comparing = new int[]{};
        foundIndex = -1;
        generateSteps(algo);
        drawArray();
        clearLog();
        comparisonsLabel.setText("Comparisons: 0");
        resultLabel.setText("Result: —");
        if (onAlgorithmSelected != null) onAlgorithmSelected.accept(algo);
    }

    private int parseSize() {
        try {
            int size = Integer.parseInt(sizeInput.getText().trim());
            if (size < 5 || size > 40) {
                appendLog("⚠ Size must be between 5 and 40.", "#FF4444");
                return array.length;
            }
            return size;
        } catch (NumberFormatException e) {
            return array.length;
        }
    }

    private void parseTarget() {
        try {
            target = Integer.parseInt(targetInput.getText().trim());
        } catch (NumberFormatException e) {
            appendLog("⚠ Invalid target — using " + target + ".", "#FF4444");
        }
    }

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
        appendLog("▶  Ready — set a target, then press Play.", "#A78BFA");
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
        Label legend = new Label("   🟡 current    dim = eliminated    🟢 found");
        legend.setStyle("-fx-text-fill: #4B5563; -fx-font-family: Monospace; -fx-font-size: 12;");
        header.getChildren().addAll(title, legend);

        stepLogBox = new VBox(3);
        stepLogBox.setStyle("-fx-background-color: #060912; -fx-padding: 8 14;");
        appendLog("▶  Ready — set a target, then press Play.", "#A78BFA");

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
        algoSelector.getItems().addAll("Linear Search", "Binary Search", "Jump Search");
        algoSelector.setValue("Linear Search");
        UiKit.styleCombo(algoSelector);
        algoSelector.setOnAction(e -> {
            rebuild(false);
            appendLog("◉ Algorithm → " + algoSelector.getValue() + ". Press Play.", "#00E5FF");
        });

        Button shuffleBtn = UiKit.controlButton("🔀  Shuffle", "#00E5FF");
        Button playBtn = UiKit.controlButton("▶️  Play", "#00E5FF");
        Button pauseBtn = UiKit.controlButton("⏸️  Pause", "#A78BFA");
        Button stepFwdBtn = UiKit.controlButton("⏩  Forward", "#A78BFA");
        Button stepBwdBtn = UiKit.controlButton("⏪  Backward", "#A78BFA");
        Button resetBtn = UiKit.controlButton("🔄  Reset", "#FF4444");
        Button applyBtn = UiKit.controlButton("✅  Apply", "#00FF88");

        sizeInput = UiKit.darkField("20", 60);
        targetInput = UiKit.darkField(String.valueOf(target), 70);

        shuffleBtn.setOnAction(e -> rebuild(true));
        applyBtn.setOnAction(e -> {
            int size = parseSize();
            String algo = algoSelector.getValue();
            if (size != array.length) {
                array = generateArray(size, needsSorted(algo));
            }
            rebuild(false);
        });
        playBtn.setOnAction(e -> isPlaying = true);
        pauseBtn.setOnAction(e -> isPlaying = false);
        stepFwdBtn.setOnAction(e -> {
            isPlaying = false;
            stepForward();
        });
        stepBwdBtn.setOnAction(e -> stepBackward());
        resetBtn.setOnAction(e -> {
            isPlaying = false;
            currentStep = 0;
            comparing = new int[]{};
            foundIndex = -1;
            rangeLow = 0;
            rangeHigh = array.length - 1;
            if (!steps.isEmpty()) array = steps.get(0)[0].clone();
            drawArray();
            clearLog();
            comparisonsLabel.setText("Comparisons: 0");
            resultLabel.setText("Result: —");
        });

        Label speedLabel = UiKit.label("Speed:");
        Slider speedSlider = new Slider(1, 10, 5);
        speedSlider.setPrefWidth(120);
        speedSlider.valueProperty().addListener((obs, o, n) ->
                speedDelay = (long) ((11 - n.doubleValue()) * 40_000_000L));

        comparisonsLabel = UiKit.label("Comparisons: 0");
        resultLabel = UiKit.label("Result: —");
        VBox stats = new VBox(4, comparisonsLabel, resultLabel);

        VBox sizeBox = new VBox(4, UiKit.label("Size:"), sizeInput);
        sizeBox.setStyle("-fx-alignment: center;");
        VBox targetBox = new VBox(4, UiKit.label("Target:"), targetInput);
        targetBox.setStyle("-fx-alignment: center;");
        VBox speedBox = new VBox(4, speedLabel, speedSlider);
        speedBox.setStyle("-fx-alignment: center;");

        panel.getChildren().addAll(
                algoSelector, UiKit.separator(),
                shuffleBtn, playBtn, pauseBtn, stepBwdBtn, stepFwdBtn, resetBtn,
                UiKit.separator(), sizeBox, targetBox, applyBtn,
                UiKit.separator(), speedBox, UiKit.separator(), stats
        );
        return panel;
    }
}
