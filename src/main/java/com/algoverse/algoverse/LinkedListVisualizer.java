package com.algoverse.algoverse;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LinkedListVisualizer extends VBox {

    private final Consumer<String> onAlgorithmSelected;
    private final Canvas canvas = new Canvas();
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final PseudocodePane codePane = new PseudocodePane();

    private final List<Integer> list = new ArrayList<>(List.of(3, 8, 1, 5));
    private final List<Frame> steps = new ArrayList<>();
    private int current;
    private int hi = -1;
    private String kind = "";
    private boolean isPlaying;
    private long lastUpdate;
    private long speedDelay = 140_000_000L;
    private AnimationTimer timer;
    private TextField valueInput;
    private Label statsLabel;
    private VBox stepLogBox;
    private ScrollPane logScroll;

    private record Frame(List<Integer> arr, int hi, String kind, int line, String msg, String color) {}

    public LinkedListVisualizer(Consumer<String> onAlgorithmSelected) {
        this.onAlgorithmSelected = onAlgorithmSelected;
        codePane.load("Linked List");
        Pane holder = UiKit.bindCanvas(canvas, this::draw);
        HBox stage = new HBox(holder, codePane);
        stage.setStyle("-fx-background-color: #0A0E1A; -fx-padding: 16 0 0 16;");
        HBox.setHgrow(holder, Priority.ALWAYS);
        VBox.setVgrow(stage, Priority.ALWAYS);

        ScrollPane controlScroll = new ScrollPane(createControlPanel());
        controlScroll.setFitToHeight(true);
        controlScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        controlScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        controlScroll.setStyle(UiKit.scrollBarStyle());

        getChildren().addAll(stage, controlScroll, createTerminalPanel());
        setStyle("-fx-background-color: #0A0E1A;");
        setupTimer();
        draw();
        if (onAlgorithmSelected != null) onAlgorithmSelected.accept("Linked List");
        appendLog("Insert, delete, reverse, or search. Each action animates the pointers.", "#00E5FF");
    }

    public void stop() {
        isPlaying = false;
        if (timer != null) timer.stop();
    }

    private void rec(List<Frame> out, List<Integer> arr, int highlight, String k, String event, String msg, String color) {
        out.add(new Frame(new ArrayList<>(arr), highlight, k, Pseudocode.line("Linked List", event), msg, color));
    }

    private void play(List<Frame> built) {
        flush();
        steps.clear();
        steps.addAll(built);
        current = 0;
        isPlaying = true;
        lastUpdate = 0;
    }

    private void flush() {
        if (!steps.isEmpty() && current >= steps.size()) {
            adopt(steps.get(steps.size() - 1).arr());
        } else if (isPlaying && current > 0) {
            adopt(steps.get(Math.min(current, steps.size()) - 1).arr());
        }
        isPlaying = false;
    }

    private void adopt(List<Integer> arr) {
        list.clear();
        list.addAll(arr);
    }

    private void apply(Frame st) {
        adopt(st.arr());
        hi = st.hi();
        kind = st.kind();
        codePane.highlight(st.line());
        statsLabel.setText("Length: " + st.arr().size() + "\nHead: " + (st.arr().isEmpty() ? "null" : st.arr().get(0)));
        appendLog(st.msg(), st.color());
        draw();
    }

    private void setupTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isPlaying) return;
                if (now - lastUpdate < speedDelay) return;
                lastUpdate = now;
                if (current < steps.size()) apply(steps.get(current++));
                else isPlaying = false;
            }
        };
        timer.start();
    }

    private int value() {
        try {
            return Integer.parseInt(valueInput.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void insertHead() {
        int v = value();
        List<Frame> s = new ArrayList<>();
        rec(s, list, 0, "", "insert", "Insert " + v + " at head", "#FFD700");
        List<Integer> next = new ArrayList<>();
        next.add(v);
        next.addAll(list);
        rec(s, next, 0, "new", "insert", "head → " + v, "#00FF88");
        play(s);
    }

    private void insertTail() {
        int v = value();
        List<Frame> s = new ArrayList<>();
        for (int k = 0; k < list.size(); k++) rec(s, list, k, "", "walk", "Walk to idx " + k, "#FFD700");
        List<Integer> next = new ArrayList<>(list);
        next.add(v);
        rec(s, next, next.size() - 1, "new", "insert", "Tail insert " + v, "#00FF88");
        play(s);
    }

    private void deleteHead() {
        if (list.isEmpty()) return;
        List<Frame> s = new ArrayList<>();
        rec(s, list, 0, "del", "remove", "Delete head " + list.get(0), "#FF4444");
        rec(s, list.subList(1, list.size()).isEmpty() ? List.of() : new ArrayList<>(list.subList(1, list.size())), 0, "", "done", "head advanced", "#A78BFA");
        play(s);
    }

    private void deleteValue() {
        int v = value();
        List<Frame> s = new ArrayList<>();
        for (int k = 0; k < list.size(); k++) {
            boolean hit = list.get(k) == v;
            rec(s, list, k, hit ? "del" : "", "walk", "Check " + list.get(k), hit ? "#FF4444" : "#FFD700");
            if (hit) {
                List<Integer> next = new ArrayList<>(list);
                next.remove(k);
                rec(s, next, Math.max(0, k - 1), "", "remove", "Unlink " + v, "#00FF88");
                play(s);
                return;
            }
        }
        rec(s, list, -1, "", "done", v + " not in list", "#A78BFA");
        play(s);
    }

    private void reverse() {
        List<Frame> s = new ArrayList<>();
        rec(s, list, 0, "", "reverse", "Reverse pointers", "#FFD700");
        List<Integer> next = new ArrayList<>(list);
        java.util.Collections.reverse(next);
        rec(s, next, 0, "new", "reverse", "head is former tail", "#00FF88");
        play(s);
    }

    private void search() {
        int v = value();
        List<Frame> s = new ArrayList<>();
        for (int k = 0; k < list.size(); k++) {
            rec(s, list, k, "", "walk", "Compare " + list.get(k) + " to " + v, "#FFD700");
            if (list.get(k) == v) {
                rec(s, list, k, "new", "done", "Found at index " + k, "#00FF88");
                play(s);
                return;
            }
        }
        rec(s, list, -1, "", "done", "Not found", "#A78BFA");
        play(s);
    }

    private void resetList() {
        isPlaying = false;
        steps.clear();
        list.clear();
        list.addAll(List.of(3, 8, 1, 5));
        hi = -1;
        kind = "";
        codePane.highlight(-1);
        draw();
        stepLogBox.getChildren().clear();
        appendLog("List reset.", "#A78BFA");
    }

    private void draw() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w < 8 || h < 8) return;
        gc.setFill(Color.web("#0A0E1A"));
        gc.fillRect(0, 0, w, h);
        gc.setFont(Font.font("Monospace", 13));
        if (list.isEmpty()) {
            gc.setFill(Color.web("#64748B"));
            gc.fillText("empty  ·  head = null", 24, h / 2);
            if (statsLabel != null) statsLabel.setText("Length: 0\nHead: null");
            return;
        }
        double box = Math.min(72, (w - 80) / list.size() - 16);
        double y = h / 2 - box / 2;
        gc.setFill(Color.web("#00E5FF"));
        gc.fillText("head", 16, y - 12);
        for (int i = 0; i < list.size(); i++) {
            double x = 24 + i * (box + 36);
            if (hi == i && "del".equals(kind)) gc.setFill(Color.web("#FF4444"));
            else if (hi == i && "new".equals(kind)) gc.setFill(Color.web("#00FF88"));
            else if (hi == i) gc.setFill(Color.web("#FFD700"));
            else gc.setFill(Color.web("#00E5FF"));
            gc.fillRect(x, y, box, box);
            gc.setFill(Color.web("#0A0E1A"));
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, Math.max(12, box / 3)));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText(String.valueOf(list.get(i)), x + 10, y + box / 2 + 4);
            gc.setStroke(Color.web("#A78BFA"));
            gc.setLineWidth(2);
            gc.strokeLine(x + box, y + box / 2, x + box + 28, y + box / 2);
            gc.setFill(Color.web("#A78BFA"));
            gc.fillPolygon(
                    new double[]{x + box + 28, x + box + 18, x + box + 18},
                    new double[]{y + box / 2, y + box / 2 - 5, y + box / 2 + 5},
                    3
            );
        }
        double endX = Math.min(24 + list.size() * (box + 36) - 8, w - 50);
        gc.setFill(Color.web("#64748B"));
        gc.setFont(Font.font("Monospace", 13));
        gc.fillText("null", endX, y + box / 2 + 4);
        if (statsLabel != null) statsLabel.setText("Length: " + list.size() + "\nHead: " + list.get(0));
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
        title.setStyle("-fx-text-fill: #00E5FF; -fx-font-family: Monospace; -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 6 14;");
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
        valueInput = UiKit.darkField("7", 60);
        Button insHead = UiKit.controlButton("⬆  Head+", "#00E5FF");
        Button insTail = UiKit.controlButton("➡  Tail+", "#00E5FF");
        Button delHead = UiKit.controlButton("⬇  Head-", "#A78BFA");
        Button delVal = UiKit.controlButton("✖  Delete", "#A78BFA");
        Button rev = UiKit.controlButton("↺  Reverse", "#A78BFA");
        Button find = UiKit.controlButton("🔍  Search", "#00E5FF");
        Button reset = UiKit.controlButton("🔄  Reset", "#FF4444");
        insHead.setOnAction(e -> insertHead());
        insTail.setOnAction(e -> insertTail());
        delHead.setOnAction(e -> deleteHead());
        delVal.setOnAction(e -> deleteValue());
        rev.setOnAction(e -> reverse());
        find.setOnAction(e -> search());
        reset.setOnAction(e -> resetList());
        Slider speed = new Slider(1, 10, 6);
        speed.setPrefWidth(110);
        speed.valueProperty().addListener((o, a, n) ->
                speedDelay = (long) ((11 - n.doubleValue()) * 70_000_000L));
        statsLabel = UiKit.label("Length: 4\nHead: 3");
        panel.getChildren().addAll(
                insHead, insTail, delHead, delVal, rev, find,
                UiKit.separator(),
                new VBox(4, UiKit.label("Value:"), valueInput),
                reset,
                UiKit.separator(),
                new VBox(4, UiKit.label("Speed:"), speed),
                UiKit.separator(), statsLabel
        );
        return panel;
    }
}
