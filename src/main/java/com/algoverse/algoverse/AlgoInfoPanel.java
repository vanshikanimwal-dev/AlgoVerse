package com.algoverse.algoverse;

import javafx.animation.TranslateTransition;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;
import java.util.Map;

public class AlgoInfoPanel extends VBox {

    private boolean isOpen = false;
    private final TranslateTransition slideIn;
    private final TranslateTransition slideOut;
    private final VBox contentBox;
    private String currentAlgo = "Bubble Sort";

    // ── Complexity data ───────────────────────────────────────────────────────
    private static final Map<String, String[]> COMPLEXITY = Map.ofEntries(
            Map.entry("Bubble Sort",          new String[]{"O(n)",    "O(n²)",    "O(n²)",    "O(1)"}),
            Map.entry("Selection Sort",       new String[]{"O(n²)",   "O(n²)",    "O(n²)",    "O(1)"}),
            Map.entry("Insertion Sort",       new String[]{"O(n)",    "O(n²)",    "O(n²)",    "O(1)"}),
            Map.entry("Merge Sort",           new String[]{"O(n log n)","O(n log n)","O(n log n)","O(n)"}),
            Map.entry("Quick Sort",           new String[]{"O(n log n)","O(n log n)","O(n²)",  "O(log n)"}),
            Map.entry("Shell Sort",           new String[]{"O(n log n)","O(n log²n)","O(n²)", "O(1)"}),
            Map.entry("Heap Sort",            new String[]{"O(n log n)","O(n log n)","O(n log n)","O(1)"}),
            Map.entry("Cocktail Shaker Sort", new String[]{"O(n)",    "O(n²)",    "O(n²)",    "O(1)"}),
            Map.entry("Counting Sort",        new String[]{"O(n+k)",  "O(n+k)",   "O(n+k)",   "O(k)"}),
            Map.entry("Radix Sort",           new String[]{"O(nk)",   "O(nk)",    "O(nk)",    "O(n+k)"}),
            Map.entry("Gnome Sort",           new String[]{"O(n)",    "O(n²)",    "O(n²)",    "O(1)"}),
            Map.entry("Odd-Even Sort",        new String[]{"O(n)",    "O(n²)",    "O(n²)",    "O(1)"}),
            Map.entry("Linear Search",        new String[]{"O(1)",    "O(n)",     "O(n)",     "O(1)"}),
            Map.entry("Binary Search",        new String[]{"O(1)",    "O(log n)", "O(log n)", "O(1)"}),
            Map.entry("Jump Search",          new String[]{"O(1)",    "O(√n)",    "O(√n)",    "O(1)"}),
            Map.entry("BFS",                  new String[]{"O(V+E)",  "O(V+E)",   "O(V+E)",   "O(V)"}),
            Map.entry("DFS",                  new String[]{"O(V+E)",  "O(V+E)",   "O(V+E)",   "O(V)"}),
            Map.entry("Dijkstra",             new String[]{"O(E log V)","O(E log V)","O(E log V)","O(V)"}),
            Map.entry("A*",                   new String[]{"O(E)",    "O(E)",     "O(E)",     "O(V)"}),
            Map.entry("Greedy Best-First",    new String[]{"O(E)",    "O(E)",     "O(E)",     "O(V)"})
    );

    // ── Plain English explanations ────────────────────────────────────────────
    private static final Map<String, String> EXPLANATION = Map.ofEntries(
            Map.entry("Bubble Sort",
                    "Repeatedly steps through the list, compares adjacent elements and swaps them if they are in the wrong order. The largest elements 'bubble up' to the end with each pass."),
            Map.entry("Selection Sort",
                    "Divides the list into sorted and unsorted parts. Each pass finds the minimum element from the unsorted part and places it at the beginning of the unsorted section."),
            Map.entry("Insertion Sort",
                    "Builds the sorted array one element at a time by picking each element and inserting it into its correct position among the already-sorted elements."),
            Map.entry("Merge Sort",
                    "Divides the array in half recursively until single elements remain, then merges them back in sorted order. A classic divide-and-conquer algorithm."),
            Map.entry("Quick Sort",
                    "Picks a pivot element and partitions the array so all smaller elements are left of the pivot and larger ones are right. Recursively sorts each partition."),
            Map.entry("Shell Sort",
                    "An improved insertion sort that compares elements far apart first, then reduces the gap. This moves out-of-place elements into position faster."),
            Map.entry("Heap Sort",
                    "Builds a max-heap from the data, then repeatedly extracts the maximum element and places it at the end. Uses the heap property to sort efficiently."),
            Map.entry("Cocktail Shaker Sort",
                    "A bidirectional bubble sort — it passes through the list both left-to-right and right-to-left each cycle, moving large elements right and small ones left."),
            Map.entry("Counting Sort",
                    "Counts how many times each value appears, then uses those counts to place each element directly into its correct position. Only works with integers in a known range."),
            Map.entry("Radix Sort",
                    "Sorts numbers digit by digit from least significant to most significant, using a stable sort at each digit level. Very efficient for fixed-length integers."),
            Map.entry("Gnome Sort",
                    "Similar to insertion sort but moves elements back one step at a time like a garden gnome moving flower pots — simple but slow for large arrays."),
            Map.entry("Odd-Even Sort",
                    "Alternates between comparing odd-indexed and even-indexed adjacent pairs. Particularly useful for parallel processing environments."),
            Map.entry("Linear Search",
                    "Checks every element from left to right until the target is found or the array ends. Works on unsorted data."),
            Map.entry("Binary Search",
                    "Repeatedly splits a sorted array in half, discarding the side that cannot contain the target. Fast, but the data must already be sorted."),
            Map.entry("Jump Search",
                    "Jumps ahead by √n steps on a sorted array until it overshoots the target, then scans linearly in that block."),
            Map.entry("BFS",
                    "Explores the grid layer by layer from the start. On an unweighted grid it finds a shortest path."),
            Map.entry("DFS",
                    "Explores as far as possible along one branch before backtracking. It can find a path, but not necessarily the shortest one."),
            Map.entry("Dijkstra",
                    "Always expands the closest unvisited cell by path cost. On this unweighted grid it behaves like a priority-queue BFS and still yields a shortest path."),
            Map.entry("A*",
                    "Like Dijkstra, but it also uses a heuristic (Manhattan distance to the goal) so it usually visits fewer cells."),
            Map.entry("Greedy Best-First",
                    "Always expands the cell that looks closest to the goal. Fast, but it can ignore cheaper routes and miss the shortest path.")
    );

    // ── When to use ───────────────────────────────────────────────────────────
    private static final Map<String, String> WHEN_TO_USE = Map.ofEntries(
            Map.entry("Bubble Sort",          "Good for learning and nearly-sorted small arrays. Avoid for large datasets."),
            Map.entry("Selection Sort",       "Useful when memory writes are costly — it makes at most O(n) swaps."),
            Map.entry("Insertion Sort",       "Best for small or nearly-sorted arrays. Often used as a subroutine in hybrid algorithms like Tim Sort."),
            Map.entry("Merge Sort",           "Best when stable sorting is needed or when sorting linked lists. Preferred for large datasets."),
            Map.entry("Quick Sort",           "Best general-purpose algorithm for large datasets in practice. Avoid when worst-case O(n²) is unacceptable."),
            Map.entry("Shell Sort",           "Good middle ground when Quick Sort is overkill. Works well for medium-sized arrays."),
            Map.entry("Heap Sort",            "Use when you need guaranteed O(n log n) with O(1) space. Good for real-time systems."),
            Map.entry("Cocktail Shaker Sort", "Slightly better than Bubble Sort for arrays where small elements are near the end."),
            Map.entry("Counting Sort",        "Perfect for sorting integers in a small known range, like exam scores or ages."),
            Map.entry("Radix Sort",           "Excellent for large sets of integers or fixed-length strings where comparison-based sorts are too slow."),
            Map.entry("Gnome Sort",           "Mainly educational. Rarely used in practice due to poor performance."),
            Map.entry("Odd-Even Sort",        "Designed for parallel processors. Each phase can be computed simultaneously across cores."),
            Map.entry("Linear Search",        "Use when the list is small or unsorted, or when you only search once."),
            Map.entry("Binary Search",        "Use on large sorted arrays. The classic choice for dictionary-style lookup."),
            Map.entry("Jump Search",          "A middle ground on sorted arrays when binary search is awkward to implement, such as in some linked structures."),
            Map.entry("BFS",                  "Best default for unweighted maps, mazes, and 'fewest steps' problems."),
            Map.entry("DFS",                  "Useful for exploring all paths, topological-style traversal, or maze generation — not shortest paths."),
            Map.entry("Dijkstra",             "Use when edges have different weights and you need a guaranteed shortest path."),
            Map.entry("A*",                   "The usual choice for games and maps when you have a good heuristic to the target."),
            Map.entry("Greedy Best-First",    "Good when a fast path matters more than a shortest path.")
    );

    public AlgoInfoPanel() {
        this.setPrefWidth(420);
        this.setMinWidth(420);
        this.setMaxWidth(420);
        this.setStyle("""
            -fx-background-color: #111827;
            -fx-border-color: #00E5FF;
            -fx-border-width: 0 0 0 1;
        """);

        // Header
        HBox header = new HBox();
        header.setStyle("""
            -fx-background-color: #1A1F2E;
            -fx-padding: 14 20;
            -fx-border-color: #00E5FF;
            -fx-border-width: 0 0 1 0;
            -fx-alignment: center-left;
        """);

        Text headerTitle = new Text("[ ALGORITHM INFO ]");
        headerTitle.setFill(Color.web("#00E5FF"));
        headerTitle.setFont(Font.font("Monospace", FontWeight.BOLD, 14));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #FF4444;
            -fx-font-size: 16;
            -fx-cursor: hand;
            -fx-padding: 0 4;
        """);
        closeBtn.setOnAction(e -> hide());

        header.getChildren().addAll(headerTitle, spacer, closeBtn);

        // Scrollable content
        contentBox = new VBox(16);
        contentBox.setStyle("-fx-padding: 20; -fx-background-color: #111827;");
        contentBox.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("""
            -fx-background: #111827;
            -fx-background-color: #111827;
        """);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        this.getChildren().addAll(header, scroll);

        // Slide animations
        slideIn = new TranslateTransition(Duration.millis(250), this);
        slideIn.setFromX(420);
        slideIn.setToX(0);

        slideOut = new TranslateTransition(Duration.millis(250), this);
        slideOut.setFromX(0);
        slideOut.setToX(420);
        slideOut.setOnFinished(e -> this.setVisible(false));

        this.setVisible(false);
        this.setTranslateX(420);

        // Load default
        loadAlgorithm("Bubble Sort");
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void show() {
        this.setVisible(true);
        slideIn.play();
        isOpen = true;
    }

    public void hide() {
        slideOut.play();
        isOpen = false;
    }

    public void toggle() {
        if (isOpen) hide(); else show();
    }

    public boolean isOpen() { return isOpen; }

    public void loadAlgorithm(String algoName) {
        currentAlgo = algoName;
        contentBox.getChildren().clear();

        // Title
        Text title = new Text(algoName);
        title.setFill(Color.web("#00E5FF"));
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 20));

        // Explanation
        Label expLabel = buildSectionLabel("📖  What it does");
        Label expText  = buildBodyLabel(
                EXPLANATION.getOrDefault(algoName, "No explanation available.")
        );

        // Complexity table
        Label compLabel = buildSectionLabel("⏱  Complexity");
        VBox compTable  = buildComplexityTable(algoName);

        // When to use
        Label whenLabel = buildSectionLabel("💡  When to use");
        Label whenText  = buildBodyLabel(
                WHEN_TO_USE.getOrDefault(algoName, "No data available.")
        );

        contentBox.getChildren().addAll(
                title,
                buildDivider(),
                expLabel, expText,
                buildDivider(),
                compLabel, compTable,
                buildDivider(),
                whenLabel, whenText
        );
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────

    private Label buildSectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("""
            -fx-text-fill: #A78BFA;
            -fx-font-family: Monospace;
            -fx-font-size: 13;
            -fx-font-weight: bold;
            -fx-padding: 4 0 2 0;
        """);
        return lbl;
    }

    private Label buildBodyLabel(String text) {
        Label lbl = new Label(text);
        lbl.setWrapText(true);
        lbl.setStyle("""
            -fx-text-fill: #CBD5E1;
            -fx-font-family: Monospace;
            -fx-font-size: 12;
            -fx-line-spacing: 4;
        """);
        return lbl;
    }

    private Region buildDivider() {
        Region div = new Region();
        div.setPrefHeight(1);
        div.setMaxWidth(Double.MAX_VALUE);
        div.setStyle("-fx-background-color: #1E2A3A;");
        return div;
    }

    private VBox buildComplexityTable(String algoName) {
        String[] c = COMPLEXITY.getOrDefault(algoName,
                new String[]{"?", "?", "?", "?"});

        VBox table = new VBox(6);

        String[][] rows = {
                {"Best Case",    c[0]},
                {"Average Case", c[1]},
                {"Worst Case",   c[2]},
                {"Space",        c[3]}
        };

        for (String[] row : rows) {
            HBox line = new HBox();
            line.setStyle("""
                -fx-background-color: #1A1F2E;
                -fx-padding: 6 12;
                -fx-background-radius: 4;
            """);

            Label key = new Label(row[0]);
            key.setStyle("""
                -fx-text-fill: #94A3B8;
                -fx-font-family: Monospace;
                -fx-font-size: 12;
            """);
            key.setPrefWidth(120);

            Label val = new Label(row[1]);
            val.setStyle("""
                -fx-text-fill: #00E5FF;
                -fx-font-family: Monospace;
                -fx-font-size: 12;
                -fx-font-weight: bold;
            """);

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            line.getChildren().addAll(key, sp, val);
            table.getChildren().add(line);
        }

        return table;
    }
}