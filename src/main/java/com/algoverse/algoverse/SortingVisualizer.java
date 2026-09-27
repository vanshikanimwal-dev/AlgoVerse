package com.algoverse.algoverse;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SortingVisualizer extends VBox {

    // Canvas
    private final Canvas canvas;
    private final GraphicsContext gc;

    // Array state
    private int[] array;
    private int[] comparing = {};
    private int[] swapping  = {};
    private int[] sorted    = {};

    // Algorithm steps
    private List<int[][]> steps     = new ArrayList<>();
    private final List<int[]> statsAtStep = new ArrayList<>();
    private int currentStep         = 0;
    private boolean isPlaying       = false;
    private final Consumer<String> onAlgorithmSelected;
    private ComboBox<String> algoSelector;
    private String currentAlgoName = "Bubble Sort";
    private final List<Integer> codeLineAtStep = new ArrayList<>();
    private PseudocodePane codePane;

    // Speed control
    private long lastUpdate         = 0;
    private long speedDelay         = 100_000_000L;

    // Stats
    private int comparisons = 0;
    private int swaps       = 0;

    // UI Labels
    private Label comparisonsLabel;
    private Label swapsLabel;
    private VBox  stepLogBox;
    private ScrollPane logScroll;
    // ← CHANGED from Label stepLogLabel
    private int   stepNumber = 0; // ← NEW

    // Animation
    private AnimationTimer timer;

    public SortingVisualizer() {
        this(null);
    }

    public SortingVisualizer(Consumer<String> onAlgorithmSelected) {
        this.onAlgorithmSelected = onAlgorithmSelected;
        canvas = new Canvas();
        gc     = canvas.getGraphicsContext2D();

        array  = generateArray(20);
        generateBubbleSortSteps();

        codePane = new PseudocodePane();
        codePane.load("Bubble Sort");
        Pane canvasHolder = UiKit.bindCanvas(canvas, this::drawArray);
        HBox stage = new HBox(canvasHolder, codePane);
        stage.setStyle("-fx-background-color: #0A0E1A; -fx-padding: 16 0 0 16;");
        HBox.setHgrow(canvasHolder, Priority.ALWAYS);
        VBox.setVgrow(stage, Priority.ALWAYS);

        VBox terminalPanel = createTerminalPanel();

        ScrollPane controlScroll = new ScrollPane();
        HBox controls = createControlPanel();
        controlScroll.setContent(controls);
        controlScroll.setFitToHeight(true);
        controlScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        controlScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        controlScroll.setStyle("""
    -fx-background: #1A1F2E;
    -fx-background-color: #1A1F2E;
    -fx-border-color: #00E5FF;
    -fx-border-width: 1 0 0 0;
""");

        // ← CHANGED: terminalPanel added at bottom
        this.getChildren().addAll(stage, controlScroll, terminalPanel);
        this.setStyle("-fx-background-color: #0A0E1A;");
        VBox.setVgrow(stage, Priority.ALWAYS);

        setupAnimationTimer();
        if (onAlgorithmSelected != null) onAlgorithmSelected.accept("Bubble Sort");
    }

    public void stop() {
        isPlaying = false;
        if (timer != null) timer.stop();
    }

    private void beginGeneration() {
        steps.clear();
        statsAtStep.clear();
        codeLineAtStep.clear();
        currentStep = 0;
        comparisons = 0;
        swaps = 0;
    }

    private void recordStep(int[][] frame) {
        String event = "mark";
        if (frame[1].length >= 1) event = "compare";
        else if (frame[2].length >= 1) event = "swap";
        else if (frame[3].length > 0 && array != null && frame[3].length == array.length) event = "done";
        steps.add(frame);
        statsAtStep.add(new int[]{comparisons, swaps});
        codeLineAtStep.add(Pseudocode.line(currentAlgoName, event));
    }

    // ─── Array Generation ────────────────────────────────────────────────────

    private int[] generateArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = (int) (Math.random() * 400) + 20;
        }
        return arr;
    }

    // ─── Bubble Sort Step Generator ──────────────────────────────────────────

    private void generateBubbleSortSteps() {
        beginGeneration();

        int[] arr = array.clone();
        int n     = arr.length;

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                comparisons++;
                recordStep(new int[][]{
                        arr.clone(),
                        {j, j + 1},
                        {},
                        {}
                });

                if (arr[j] > arr[j + 1]) {
                    swaps++;
                    int temp   = arr[j];
                    arr[j]     = arr[j + 1];
                    arr[j + 1] = temp;
                    recordStep(new int[][]{
                            arr.clone(),
                            {},
                            {j, j + 1},
                            {}
                    });
                }
            }

            int[] sortedSoFar = new int[i + 1];
            for (int k = 0; k <= i; k++) {
                sortedSoFar[k] = n - 1 - k;
            }
            recordStep(new int[][]{
                    arr.clone(),
                    {},
                    {},
                    sortedSoFar
            });
        }

        int[] allSorted = new int[n];
        for (int i = 0; i < n; i++) allSorted[i] = i;
        recordStep(new int[][]{arr.clone(), {}, {}, allSorted});
    }
    // ─── Selection Sort ──────────────────────────────────────────────────────────
    private void generateSelectionSortSteps() {
        beginGeneration();
        int[] arr = array.clone();
        int n = arr.length;

        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                comparisons++;
                recordStep(new int[][]{ arr.clone(), {minIdx, j}, {}, {} });
                if (arr[j] < arr[minIdx]) minIdx = j;
            }
            if (minIdx != i) {
                swaps++;
                int tmp = arr[i]; arr[i] = arr[minIdx]; arr[minIdx] = tmp;
                recordStep(new int[][]{ arr.clone(), {}, {i, minIdx}, {} });
            }
            int[] sf = new int[i + 1];
            for (int k = 0; k <= i; k++) sf[k] = k;
            recordStep(new int[][]{ arr.clone(), {}, {}, sf });
        }
        int[] all = new int[n]; for (int i = 0; i < n; i++) all[i] = i;
        recordStep(new int[][]{ arr.clone(), {}, {}, all });
    }

    // ─── Insertion Sort ──────────────────────────────────────────────────────────
    private void generateInsertionSortSteps() {
        beginGeneration();
        int[] arr = array.clone();
        int n = arr.length;

        for (int i = 1; i < n; i++) {
            int j = i;
            while (j > 0) {
                comparisons++;
                recordStep(new int[][]{ arr.clone(), {j - 1, j}, {}, {} });
                if (arr[j] < arr[j - 1]) {
                    swaps++;
                    int tmp = arr[j]; arr[j] = arr[j - 1]; arr[j - 1] = tmp;
                    recordStep(new int[][]{ arr.clone(), {}, {j, j - 1}, {} });
                    j--;
                } else break;
            }
            int[] sf = new int[i + 1];
            for (int k = 0; k <= i; k++) sf[k] = k;
            recordStep(new int[][]{ arr.clone(), {}, {}, sf });
        }
        int[] all = new int[n]; for (int i = 0; i < n; i++) all[i] = i;
        recordStep(new int[][]{ arr.clone(), {}, {}, all });
    }

    // ─── Shell Sort ──────────────────────────────────────────────────────────────
    private void generateShellSortSteps() {
        beginGeneration();
        int[] arr = array.clone();
        int n = arr.length;

        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                int j = i;
                while (j >= gap) {
                    comparisons++;
                    recordStep(new int[][]{ arr.clone(), {j - gap, j}, {}, {} });
                    if (arr[j] < arr[j - gap]) {
                        swaps++;
                        int tmp = arr[j]; arr[j] = arr[j - gap]; arr[j - gap] = tmp;
                        recordStep(new int[][]{ arr.clone(), {}, {j, j - gap}, {} });
                        j -= gap;
                    } else break;
                }
            }
        }
        int[] all = new int[n]; for (int i = 0; i < n; i++) all[i] = i;
        recordStep(new int[][]{ arr.clone(), {}, {}, all });
    }

    // ─── Merge Sort ──────────────────────────────────────────────────────────────
    private void generateMergeSortSteps() {
        beginGeneration();
        int[] arr = array.clone();
        mergeSort(arr, 0, arr.length - 1);
        int[] all = new int[arr.length];
        for (int i = 0; i < arr.length; i++) all[i] = i;
        recordStep(new int[][]{ arr.clone(), {}, {}, all });
    }

    private void mergeSort(int[] arr, int l, int r) {
        if (l >= r) return;
        int mid = (l + r) / 2;
        mergeSort(arr, l, mid);
        mergeSort(arr, mid + 1, r);
        merge(arr, l, mid, r);
    }

    private void merge(int[] arr, int l, int mid, int r) {
        int[] left  = java.util.Arrays.copyOfRange(arr, l, mid + 1);
        int[] right = java.util.Arrays.copyOfRange(arr, mid + 1, r + 1);
        int i = 0, j = 0, k = l;
        while (i < left.length && j < right.length) {
            comparisons++;
            recordStep(new int[][]{ arr.clone(), {l + i, mid + 1 + j}, {}, {} });
            if (left[i] <= right[j]) {
                arr[k++] = left[i++];
            } else {
                arr[k++] = right[j++];
                swaps++;
            }
            recordStep(new int[][]{ arr.clone(), {}, {k - 1}, {} });
        }
        while (i < left.length) { arr[k++] = left[i++]; }
        while (j < right.length) { arr[k++] = right[j++]; }
    }

    // ─── Quick Sort ──────────────────────────────────────────────────────────────
    private void generateQuickSortSteps() {
        beginGeneration();
        int[] arr = array.clone();
        quickSort(arr, 0, arr.length - 1);
        int[] all = new int[arr.length];
        for (int i = 0; i < arr.length; i++) all[i] = i;
        recordStep(new int[][]{ arr.clone(), {}, {}, all });
    }

    private void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            comparisons++;
            recordStep(new int[][]{ arr.clone(), {j, high}, {}, {} });
            if (arr[j] <= pivot) {
                i++;
                swaps++;
                int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
                recordStep(new int[][]{ arr.clone(), {}, {i, j}, {} });
            }
        }
        int tmp = arr[i + 1]; arr[i + 1] = arr[high]; arr[high] = tmp;
        recordStep(new int[][]{ arr.clone(), {}, {i + 1, high}, {} });
        return i + 1;
    }
    // ─── Heap Sort ────────────────────────────────────────────────────────────────
    private void generateHeapSortSteps() {
        beginGeneration();
        int[] arr = array.clone();
        int n = arr.length;

        // Build max heap
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }

        // Extract elements one by one
        for (int i = n - 1; i > 0; i--) {
            swaps++;
            int tmp = arr[0]; arr[0] = arr[i]; arr[i] = tmp;
            recordStep(new int[][]{ arr.clone(), {}, {0, i}, {} });

            int[] sf = new int[n - i];
            for (int k = 0; k < n - i; k++) sf[k] = i + k;
            recordStep(new int[][]{ arr.clone(), {}, {}, sf });

            heapify(arr, i, 0);
        }

        int[] all = new int[n];
        for (int i = 0; i < n; i++) all[i] = i;
        recordStep(new int[][]{ arr.clone(), {}, {}, all });
    }

    private void heapify(int[] arr, int n, int i) {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;

        if (l < n) {
            comparisons++;
            recordStep(new int[][]{ arr.clone(), {largest, l}, {}, {} });
            if (arr[l] > arr[largest]) largest = l;
        }
        if (r < n) {
            comparisons++;
            recordStep(new int[][]{ arr.clone(), {largest, r}, {}, {} });
            if (arr[r] > arr[largest]) largest = r;
        }
        if (largest != i) {
            swaps++;
            int tmp = arr[i]; arr[i] = arr[largest]; arr[largest] = tmp;
            recordStep(new int[][]{ arr.clone(), {}, {i, largest}, {} });
            heapify(arr, n, largest);
        }
    }

    // ─── Cocktail Shaker Sort ─────────────────────────────────────────────────────
    private void generateCocktailSortSteps() {
        beginGeneration();
        int[] arr = array.clone();
        int n = arr.length;
        boolean swapped = true;
        int start = 0;
        int end = n - 1;

        while (swapped) {
            swapped = false;

            // Left to right
            for (int i = start; i < end; i++) {
                comparisons++;
                recordStep(new int[][]{ arr.clone(), {i, i + 1}, {}, {} });
                if (arr[i] > arr[i + 1]) {
                    swaps++;
                    int tmp = arr[i]; arr[i] = arr[i + 1]; arr[i + 1] = tmp;
                    recordStep(new int[][]{ arr.clone(), {}, {i, i + 1}, {} });
                    swapped = true;
                }
            }

            if (!swapped) break;
            swapped = false;
            end--;

            // Right to left
            for (int i = end - 1; i >= start; i--) {
                comparisons++;
                recordStep(new int[][]{ arr.clone(), {i, i + 1}, {}, {} });
                if (arr[i] > arr[i + 1]) {
                    swaps++;
                    int tmp = arr[i]; arr[i] = arr[i + 1]; arr[i + 1] = tmp;
                    recordStep(new int[][]{ arr.clone(), {}, {i, i + 1}, {} });
                    swapped = true;
                }
            }
            start++;
        }

        int[] all = new int[n];
        for (int i = 0; i < n; i++) all[i] = i;
        recordStep(new int[][]{ arr.clone(), {}, {}, all });
    }

    // ─── Counting Sort ────────────────────────────────────────────────────────────
    private void generateCountingSortSteps() {
        beginGeneration();
        int[] arr = array.clone();
        int n = arr.length;

        int max = arr[0];
        for (int i = 1; i < n; i++) {
            comparisons++;
            recordStep(new int[][]{ arr.clone(), {i, 0}, {}, {} });
            if (arr[i] > max) max = arr[i];
        }

        int[] count = new int[max + 1];
        for (int i = 0; i < n; i++) count[arr[i]]++;

        int[] output = new int[n];
        int idx = 0;
        for (int i = 0; i <= max; i++) {
            while (count[i] > 0) {
                output[idx++] = i;
                count[i]--;
            }
        }

        // Animate placing sorted values back
        for (int i = 0; i < n; i++) {
            arr[i] = output[i];
            swaps++;
            int[] sf = new int[i + 1];
            for (int k = 0; k <= i; k++) sf[k] = k;
            recordStep(new int[][]{ arr.clone(), {}, {i}, sf });
        }

        int[] all = new int[n];
        for (int i = 0; i < n; i++) all[i] = i;
        recordStep(new int[][]{ arr.clone(), {}, {}, all });
    }

    // ─── Radix Sort ───────────────────────────────────────────────────────────────
    private void generateRadixSortSteps() {
        beginGeneration();
        int[] arr = array.clone();
        int n = arr.length;

        int max = arr[0];
        for (int x : arr) if (x > max) max = x;

        for (int exp = 1; max / exp > 0; exp *= 10) {
            countingSortByDigit(arr, n, exp);
        }

        int[] all = new int[n];
        for (int i = 0; i < n; i++) all[i] = i;
        recordStep(new int[][]{ arr.clone(), {}, {}, all });
    }

    private void countingSortByDigit(int[] arr, int n, int exp) {
        int[] output = new int[n];
        int[] count  = new int[10];

        for (int i = 0; i < n; i++) count[(arr[i] / exp) % 10]++;
        for (int i = 1; i < 10; i++) count[i] += count[i - 1];

        for (int i = n - 1; i >= 0; i--) {
            output[count[(arr[i] / exp) % 10] - 1] = arr[i];
            count[(arr[i] / exp) % 10]--;
        }

        for (int i = 0; i < n; i++) {
            arr[i] = output[i];
            swaps++;
            recordStep(new int[][]{ arr.clone(), {}, {i}, {} });
        }
    }

    // ─── Gnome Sort ───────────────────────────────────────────────────────────────
    private void generateGnomeSortSteps() {
        beginGeneration();
        int[] arr = array.clone();
        int n = arr.length;
        int i = 0;

        while (i < n) {
            if (i == 0) {
                i++;
            } else {
                comparisons++;
                recordStep(new int[][]{ arr.clone(), {i - 1, i}, {}, {} });
                if (arr[i] >= arr[i - 1]) {
                    i++;
                } else {
                    swaps++;
                    int tmp = arr[i]; arr[i] = arr[i - 1]; arr[i - 1] = tmp;
                    recordStep(new int[][]{ arr.clone(), {}, {i, i - 1}, {} });
                    i--;
                }
            }
        }

        int[] all = new int[n];
        for (int k = 0; k < n; k++) all[k] = k;
        recordStep(new int[][]{ arr.clone(), {}, {}, all });
    }

    // ─── Odd-Even Sort ────────────────────────────────────────────────────────────
    private void generateOddEvenSortSteps() {
        beginGeneration();
        int[] arr = array.clone();
        int n = arr.length;
        boolean sorted = false;

        while (!sorted) {
            sorted = true;

            // Odd phase
            for (int i = 1; i < n - 1; i += 2) {
                comparisons++;
                recordStep(new int[][]{ arr.clone(), {i, i + 1}, {}, {} });
                if (arr[i] > arr[i + 1]) {
                    swaps++;
                    int tmp = arr[i]; arr[i] = arr[i + 1]; arr[i + 1] = tmp;
                    recordStep(new int[][]{ arr.clone(), {}, {i, i + 1}, {} });
                    sorted = false;
                }
            }

            // Even phase
            for (int i = 0; i < n - 1; i += 2) {
                comparisons++;
                recordStep(new int[][]{ arr.clone(), {i, i + 1}, {}, {} });
                if (arr[i] > arr[i + 1]) {
                    swaps++;
                    int tmp = arr[i]; arr[i] = arr[i + 1]; arr[i + 1] = tmp;
                    recordStep(new int[][]{ arr.clone(), {}, {i, i + 1}, {} });
                    sorted = false;
                }
            }
        }

        int[] all = new int[n];
        for (int i = 0; i < n; i++) all[i] = i;
        recordStep(new int[][]{ arr.clone(), {}, {}, all });
    }

    private void generateSteps(String algorithm) {
        currentAlgoName = algorithm;
        if (codePane != null) codePane.load(algorithm);
        switch (algorithm) {
            case "Bubble Sort"         -> generateBubbleSortSteps();
            case "Selection Sort"      -> generateSelectionSortSteps();
            case "Insertion Sort"      -> generateInsertionSortSteps();
            case "Merge Sort"          -> generateMergeSortSteps();
            case "Quick Sort"          -> generateQuickSortSteps();
            case "Shell Sort"          -> generateShellSortSteps();
            case "Heap Sort"           -> generateHeapSortSteps();
            case "Cocktail Shaker Sort"-> generateCocktailSortSteps();
            case "Counting Sort"       -> generateCountingSortSteps();
            case "Radix Sort"          -> generateRadixSortSteps();
            case "Gnome Sort"          -> generateGnomeSortSteps();
            case "Odd-Even Sort"       -> generateOddEvenSortSteps();
            default                    -> generateBubbleSortSteps();
        }
    }
    // ─── Drawing ─────────────────────────────────────────────────────────────

    private void drawArray() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w < 8 || h < 8 || array == null) return;
        gc.setFill(Color.web("#0A0E1A"));
        gc.fillRect(0, 0, w, h);

        double barWidth = (w / array.length) - 2;
        int max = 1;
        for (int value : array) if (value > max) max = value;

        for (int i = 0; i < array.length; i++) {
            double barHeight = (array[i] / (double) max) * (h - 8);
            double x = i * (barWidth + 2);
            double y = h - barHeight;
            gc.setFill(getBarColor(i));
            gc.fillRect(x, y, barWidth, barHeight);
        }
    }

    private Color getBarColor(int index) {
        for (int s : sorted)    if (s == index) return Color.web("#00FF88");
        for (int s : swapping)  if (s == index) return Color.web("#FF4444");
        for (int c : comparing) if (c == index) return Color.web("#FFD700");
        double ratio = (double) index / array.length;
        return Color.web("#00E5FF").interpolate(Color.web("#A78BFA"), ratio);
    }

    // ─── Animation Timer ─────────────────────────────────────────────────────

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

    // ─── Step Logic ──────────────────────────────────────────────────────────

    private void stepForward() {
        if (currentStep >= steps.size()) {
            if (isPlaying) appendLog("✅  Sorting complete!", "#00FF88");
            isPlaying = false;
            return;
        }

        int[][] step = steps.get(currentStep);
        array     = step[0];
        comparing = step[1];
        swapping  = step[2];
        sorted    = step[3];

        drawArray();
        updateStepLog();
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
        comparing = new int[]{};
        swapping = new int[]{};
        sorted = new int[]{};
        if (!steps.isEmpty()) array = steps.get(0)[0].clone();
        stepLogBox.getChildren().clear();
        if (codePane != null) codePane.highlight(-1);
        for (int i = 0; i < target; i++) stepForward();
        isPlaying = false;
    }

    // ─── CHANGED: updateStepLog now appends numbered steps ───────────────────

    private void updateStepLog() {
        stepNumber++;
        if (currentStep < statsAtStep.size()) {
            int[] stats = statsAtStep.get(currentStep);
            comparisonsLabel.setText("Comparisons: " + stats[0]);
            swapsLabel.setText("Moves: " + stats[1]);
        }

        String msg;
        String color;

        if (comparing.length == 2) {
            msg   = "Comparing  idx " + comparing[0]
                    + " (val:" + array[comparing[0]] + ")"
                    + "  ↔  idx " + comparing[1]
                    + " (val:" + array[comparing[1]] + ")";
            color = "#FFD700";
        } else if (swapping.length == 2) {
            msg   = "Swapping   idx " + swapping[0]
                    + " (val:" + array[swapping[0]] + ")"
                    + "  ↔  idx " + swapping[1]
                    + " (val:" + array[swapping[1]] + ")";
            color = "#FF6666";
        } else {
            msg   = "Sorted ✓   " + sorted.length + " element(s) in place";
            color = "#00FF88";
        }

        appendLog("Step " + String.format("%4d", stepNumber) + "  →  " + msg, color);
    }

    // ─── NEW: append a line to the terminal log ───────────────────────────────

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

        // only auto-scroll if already attached to scene
        if (logScroll != null) {
            logScroll.setVvalue(1.0);
        }
    }

    // ─── NEW: clear the terminal log ─────────────────────────────────────────

    private void clearLog() {
        stepLogBox.getChildren().clear();
        stepNumber = 0;
        appendLog("▶  Ready — press Play or Step Forward to begin.", "#A78BFA");
    }

    // ─── NEW: terminal panel UI ───────────────────────────────────────────────

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

        Label legend = new Label("   🟡 comparing    🔴 swapping    🟢 sorted");
        legend.setStyle("""
            -fx-text-fill: #4B5563;
            -fx-font-family: Monospace;
            -fx-font-size: 12;
        """);

        header.getChildren().addAll(title, legend);

        stepLogBox = new VBox(3);
        stepLogBox.setStyle("-fx-background-color: #060912; -fx-padding: 8 14;");
        stepLogBox.setFillWidth(true);

        // initial message — stepLogBox is now ready
        appendLog("▶  Ready — press Play or Step Forward to begin.", "#A78BFA");

        logScroll = new ScrollPane(stepLogBox);
        logScroll.setFitToWidth(true);
        logScroll.setPrefHeight(160);
        logScroll.setStyle("""
    -fx-background: #060912;
    -fx-background-color: #060912;
""");
        logScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        logScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox terminal = new VBox(header, logScroll);
        terminal.setStyle("-fx-background-color: #060912;");
        return terminal;
    }

    // ─── Control Panel ───────────────────────────────────────────────────────

    private HBox createControlPanel() {
        HBox panel = new HBox(16);
        panel.setStyle("""
            -fx-background-color: #1A1F2E;
            -fx-padding: 14 20;
            -fx-border-color: #00E5FF;
            -fx-border-width: 1 0 0 0;
            -fx-alignment: center-left;
        """);
        // Algorithm selector
        algoSelector = new ComboBox<>();
        algoSelector.getItems().addAll(
                "Bubble Sort",
                "Selection Sort",
                "Insertion Sort",
                "Merge Sort",
                "Quick Sort",
                "Shell Sort",
                "Heap Sort",
                "Cocktail Shaker Sort",
                "Counting Sort",
                "Radix Sort",
                "Gnome Sort",
                "Odd-Even Sort"
        );
        algoSelector.setValue("Bubble Sort");
        algoSelector.setStyle("""
    -fx-background-color: #0A0E1A;
    -fx-border-color: #00E5FF;
    -fx-border-width: 2;
    -fx-font-family: Monospace;
    -fx-font-size: 13;
    -fx-min-width: 160;
    -fx-mark-color: #00E5FF;
""");

// style the text and arrow inside
        // Style the selected item shown in the box
        algoSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("""
            -fx-text-fill: #00E5FF;
            -fx-background-color: #0A0E1A;
            -fx-font-family: Monospace;
            -fx-font-size: 13;
        """);
            }
        });
        algoSelector.setOnAction(e -> {
            isPlaying   = false;
            currentStep = 0;
            comparing   = new int[]{};
            swapping    = new int[]{};
            sorted      = new int[]{};
            generateSteps(algoSelector.getValue());
            drawArray();
            clearLog();
            appendLog("◉ Algorithm → " + algoSelector.getValue() + ". Press Play.", "#00E5FF");
            comparisonsLabel.setText("Comparisons: 0");
            swapsLabel.setText("Moves: 0");
            if (onAlgorithmSelected != null) onAlgorithmSelected.accept(algoSelector.getValue());
        });
        algoSelector.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("""
            -fx-text-fill: #00E5FF;
            -fx-background-color: #0A0E1A;
            -fx-font-family: Monospace;
            -fx-font-size: 13;
            -fx-padding: 6 10;
        """);
            }
        });
        Button generateBtn = createButton("🔀  Shuffle",   "#00E5FF");
        Button playBtn     = createButton("▶️  Play",       "#00E5FF");
        Button pauseBtn    = createButton("⏸️  Pause",      "#A78BFA");
        Button stepFwdBtn  = createButton("⏩  Forward",    "#A78BFA");
        Button stepBwdBtn  = createButton("⏪  Backward",   "#A78BFA");
        Button resetBtn    = createButton("🔄  Reset",      "#FF4444");
        Button applyBtn    = createButton("✅  Apply",      "#00FF88");

        Label sizeLabel = createLabel("Array Size:");
        TextField sizeInput = new TextField("30");
        sizeInput.setPrefWidth(60);
        sizeInput.setStyle("""
    -fx-background-color: #0A0E1A;
    -fx-border-color: #00E5FF;
    -fx-border-width: 2;
    -fx-text-fill: #00E5FF;
    -fx-font-family: Monospace;
    -fx-font-size: 14;
    -fx-padding: 6 10;
""");

        // ← CHANGED: stepLogLabel → appendLog
        applyBtn.setOnAction(e -> {
            try {
                int size = Integer.parseInt(sizeInput.getText().trim());
                if (size < 5 || size > 200) {
                    appendLog("⚠ Enter a value between 5 and 200.", "#FF4444");
                    return;
                }
                isPlaying = false;
                array     = generateArray(size);
                comparing = new int[]{};
                swapping  = new int[]{};
                sorted    = new int[]{};
                generateSteps(algoSelector.getValue());
                drawArray();
                clearLog();
                appendLog("◉ Array size → " + size + ". Press Play.", "#00E5FF");
                comparisonsLabel.setText("Comparisons: 0");
                swapsLabel.setText("Moves: 0");
            } catch (NumberFormatException ex) {
                appendLog("⚠ Invalid input — enter a number.", "#FF4444");
            }
        });

        Label speedLabel = createLabel("Speed:");
        Slider speedSlider = new Slider(1, 10, 5);
        speedSlider.setStyle("-fx-control-inner-background: #0A0E1A;");
        speedSlider.setPrefWidth(120);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            speedDelay = (long) ((11 - newVal.doubleValue()) * 50_000_000L);
        });

        comparisonsLabel = createLabel("Comparisons: 0");
        swapsLabel       = createLabel("Moves: 0");

        // ← CHANGED: stepLogLabel → clearLog()
        generateBtn.setOnAction(e -> {
            isPlaying = false;
            array     = generateArray(Integer.parseInt(
                    sizeInput.getText().trim().matches("\\d+")
                            ? sizeInput.getText().trim() : "20"));
            comparing = new int[]{};
            swapping  = new int[]{};
            sorted    = new int[]{};
            generateSteps(algoSelector.getValue());
            drawArray();
            clearLog();
            comparisonsLabel.setText("Comparisons: 0");
            swapsLabel.setText("Moves: 0");
        });

        playBtn.setOnAction(e    -> isPlaying = true);
        pauseBtn.setOnAction(e   -> isPlaying = false);
        stepFwdBtn.setOnAction(e -> { isPlaying = false; stepForward(); });
        stepBwdBtn.setOnAction(e -> { isPlaying = false; stepBackward(); });

        // ← CHANGED: stepLogLabel → clearLog()
        resetBtn.setOnAction(e -> {
            isPlaying   = false;
            currentStep = 0;
            comparing   = new int[]{};
            swapping    = new int[]{};
            sorted      = new int[]{};
            array       = steps.get(0)[0].clone();
            drawArray();
            clearLog();
            comparisonsLabel.setText("Comparisons: 0");
            swapsLabel.setText("Moves: 0");
            if (codePane != null) codePane.highlight(-1);
        });

        VBox sizeBox = new VBox(4);
        sizeBox.setStyle("-fx-alignment: center;");
        sizeBox.getChildren().addAll(sizeLabel, sizeInput);

        VBox speedBox = new VBox(4);
        speedBox.setStyle("-fx-alignment: center;");
        speedBox.getChildren().addAll(speedLabel, speedSlider);

        VBox statsBox = new VBox(4);
        statsBox.setStyle("-fx-alignment: center-left;");
        statsBox.getChildren().addAll(comparisonsLabel, swapsLabel);

        // ← CHANGED: removed stepLogLabel from here
        panel.getChildren().addAll(
                algoSelector,         // ← ADD THIS
                createSeparator(),
                generateBtn, playBtn, pauseBtn,
                stepBwdBtn, stepFwdBtn, resetBtn,
                createSeparator(),
                sizeBox, applyBtn,
                createSeparator(),
                speedBox,
                createSeparator(),
                statsBox
        );

        return panel;
    }

    // ─── UI Helpers ──────────────────────────────────────────────────────────

    private Button createButton(String text, String color) {
        String[] parts = text.trim().split("\\s+", 2);
        String emoji = parts[0];
        String label = parts.length > 1 ? parts[1] : "";

        Button btn = new Button(emoji + "\n" + label);
        btn.setTextAlignment(TextAlignment.CENTER);

        String normal = String.format("""
        -fx-background-color: #1A1F2E;
        -fx-border-color: %s;
        -fx-border-width: 1.5;
        -fx-text-fill: %s;
        -fx-font-family: Monospace;
        -fx-font-size: 13;
        -fx-padding: 8 12;
        -fx-cursor: hand;
        -fx-min-width: 72;
        -fx-min-height: 52;
        -fx-alignment: center;
    """, color, color);

        String hovered = String.format("""
        -fx-background-color: %s22;
        -fx-border-color: %s;
        -fx-border-width: 1.5;
        -fx-text-fill: %s;
        -fx-font-family: Monospace;
        -fx-font-size: 13;
        -fx-padding: 8 12;
        -fx-cursor: hand;
        -fx-min-width: 72;
        -fx-min-height: 52;
        -fx-alignment: center;
    """, color, color, color);

        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hovered));
        btn.setOnMouseExited(e  -> btn.setStyle(normal));
        return btn;
    }

    private Label createLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("""
        -fx-text-fill: #F0F4FF;
        -fx-font-family: Monospace;
        -fx-font-size: 14;
    """);
        return lbl;
    }

    private Region createSeparator() {
        Region sep = new Region();
        sep.setStyle("-fx-background-color: #2A2F3E;");
        sep.setPrefWidth(1);
        sep.setPrefHeight(52);
        return sep;
    }
}
