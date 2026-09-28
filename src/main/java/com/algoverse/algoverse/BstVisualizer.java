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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BstVisualizer extends VBox {

    private final Consumer<String> onAlgorithmSelected;
    private final Canvas canvas = new Canvas();
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final PseudocodePane codePane = new PseudocodePane();

    private Node root;
    private Integer hi;
    private final List<Frame> frames = new ArrayList<>();
    private int current;
    private boolean isPlaying;
    private long lastUpdate;
    private long speedDelay = 160_000_000L;
    private AnimationTimer timer;
    private TextField valueInput;
    private Label statsLabel;
    private VBox stepLogBox;
    private ScrollPane logScroll;

    static final class Node {
        int v;
        Node l, r;
        Node(int v) { this.v = v; }
        Node copy() {
            Node n = new Node(v);
            n.l = l == null ? null : l.copy();
            n.r = r == null ? null : r.copy();
            return n;
        }
    }

    private record Frame(Node root, Integer hi, String event, String msg, String color) {}

    public BstVisualizer(Consumer<String> onAlgorithmSelected) {
        this.onAlgorithmSelected = onAlgorithmSelected;
        codePane.load("Binary Search Tree");
        for (int v : new int[]{7, 3, 11, 1, 5, 9, 13}) root = insert(root, v, new ArrayList<>());
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
        if (onAlgorithmSelected != null) onAlgorithmSelected.accept("Binary Search Tree");
        appendLog("Insert and search walk left/right. Traversals highlight visit order.", "#00E5FF");
    }

    public void stop() {
        isPlaying = false;
        if (timer != null) timer.stop();
    }

    private Node insert(Node node, int v, List<Frame> out) {
        if (node == null) {
            Node n = new Node(v);
            out.add(new Frame(n.copy(), v, "insert", "Insert " + v + " as root", "#00FF88"));
            return n;
        }
        Node cur = node;
        while (true) {
            out.add(new Frame(node.copy(), cur.v, v < cur.v ? "left" : "right",
                    v + " vs " + cur.v + " → " + (v < cur.v ? "left" : "right"), "#FFD700"));
            if (v == cur.v) {
                out.add(new Frame(node.copy(), v, "insert", v + " already present", "#A78BFA"));
                return node;
            }
            if (v < cur.v) {
                if (cur.l == null) {
                    cur.l = new Node(v);
                    out.add(new Frame(node.copy(), v, "insert", "Insert " + v + " left of " + cur.v, "#00FF88"));
                    return node;
                }
                cur = cur.l;
            } else {
                if (cur.r == null) {
                    cur.r = new Node(v);
                    out.add(new Frame(node.copy(), v, "insert", "Insert " + v + " right of " + cur.v, "#00FF88"));
                    return node;
                }
                cur = cur.r;
            }
        }
    }

    private void play(List<Frame> built) {
        isPlaying = false;
        frames.clear();
        frames.addAll(built);
        current = 0;
        isPlaying = true;
        lastUpdate = 0;
    }

    private void apply(Frame st) {
        root = st.root().copy();
        hi = st.hi();
        codePane.highlight(Pseudocode.line("Binary Search Tree", st.event()));
        statsLabel.setText("Highlight: " + (st.hi() == null ? "—" : st.hi()));
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
                if (current < frames.size()) apply(frames.get(current++));
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

    private void finishPlay() {
        isPlaying = false;
        if (!frames.isEmpty()) {
            Frame last = frames.get(frames.size() - 1);
            root = last.root() == null ? null : last.root().copy();
            hi = last.hi();
        }
    }

    private void doInsert() {
        finishPlay();
        List<Frame> f = new ArrayList<>();
        root = insert(root, value(), f);
        play(f);
    }

    private void doFind() {
        finishPlay();        int v = value();
        List<Frame> f = new ArrayList<>();
        Node cur = root;
        while (cur != null) {
            f.add(new Frame(root.copy(), cur.v, v < cur.v ? "left" : "right", v + " vs " + cur.v, "#FFD700"));
            if (cur.v == v) {
                f.add(new Frame(root.copy(), v, "insert", "Found " + v, "#00FF88"));
                play(f);
                return;
            }
            cur = v < cur.v ? cur.l : cur.r;
        }
        f.add(new Frame(root == null ? null : root.copy(), null, "insert", v + " not in tree", "#A78BFA"));
        play(f);
    }

    private void traverse(String order) {
        finishPlay();
        List<Frame> f = new ArrayList<>();
        walk(root, order, f);
        f.add(new Frame(root == null ? null : root.copy(), null, order, order + " complete", "#00FF88"));
        play(f);
    }

    private void walk(Node n, String order, List<Frame> f) {
        if (n == null) return;
        if ("preorder".equals(order)) f.add(new Frame(root.copy(), n.v, "preorder", "Visit " + n.v, "#FFD700"));
        walk(n.l, order, f);
        if ("inorder".equals(order)) f.add(new Frame(root.copy(), n.v, "inorder", "Visit " + n.v, "#FFD700"));
        walk(n.r, order, f);
        if ("postorder".equals(order)) f.add(new Frame(root.copy(), n.v, "postorder", "Visit " + n.v, "#FFD700"));
    }

    private void seed() {
        finishPlay();
        isPlaying = false;
        root = null;
        hi = null;
        List<Integer> vals = new ArrayList<>();
        for (int i = 0; i < 7; i++) vals.add(1 + (int) (Math.random() * 20));
        for (int v : vals) root = insert(root, v, new ArrayList<>());
        codePane.highlight(-1);
        draw();
        stepLogBox.getChildren().clear();
        appendLog("Random tree: " + vals.toString().replaceAll("[\\[\\]]", ""), "#00E5FF");
    }

    private void clearTree() {
        finishPlay();
        isPlaying = false;
        root = null;
        hi = null;
        codePane.highlight(-1);
        draw();
        stepLogBox.getChildren().clear();
        appendLog("Tree cleared.", "#A78BFA");
    }

    private Map<Node, double[]> layout(Node n) {
        Map<Node, double[]> pos = new IdentityHashMap<>();
        int[] i = {0};
        inorder(n, 0, i, pos);
        return pos;
    }

    private void inorder(Node n, int depth, int[] i, Map<Node, double[]> pos) {
        if (n == null) return;
        inorder(n.l, depth + 1, i, pos);
        pos.put(n, new double[]{i[0]++, depth, n.v});
        inorder(n.r, depth + 1, i, pos);
    }

    private void draw() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w < 8 || h < 8) return;
        gc.setFill(Color.web("#0A0E1A"));
        gc.fillRect(0, 0, w, h);
        if (root == null) {
            gc.setFill(Color.web("#64748B"));
            gc.setFont(Font.font("Monospace", 16));
            gc.fillText("empty tree", 24, h / 2);
            return;
        }
        Map<Node, double[]> pos = layout(root);
        double maxX = 1, maxY = 1;
        for (double[] p : pos.values()) {
            if (p[0] > maxX) maxX = p[0];
            if (p[1] > maxY) maxY = p[1];
        }
        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(2);
        edges(root, pos, maxX, maxY, w, h);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        for (Map.Entry<Node, double[]> e : pos.entrySet()) {
            double[] p = e.getValue();
            double x = 40 + (p[0] / Math.max(1, maxX)) * (w - 80);
            double y = 36 + (p[1] / Math.max(1, maxY)) * (h - 80);
            gc.setFill(hi != null && e.getKey().v == hi ? Color.web("#FFD700") : Color.web("#00E5FF"));
            gc.fillOval(x - 16, y - 16, 32, 32);
            gc.setFill(Color.web("#0A0E1A"));
            gc.fillText(String.valueOf(e.getKey().v), x, y + 4);
        }
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void edges(Node n, Map<Node, double[]> pos, double maxX, double maxY, double w, double h) {
        if (n == null) return;
        double[] a = xy(pos.get(n), maxX, maxY, w, h);
        if (n.l != null) {
            double[] b = xy(pos.get(n.l), maxX, maxY, w, h);
            gc.strokeLine(a[0], a[1], b[0], b[1]);
        }
        if (n.r != null) {
            double[] b = xy(pos.get(n.r), maxX, maxY, w, h);
            gc.strokeLine(a[0], a[1], b[0], b[1]);
        }
        edges(n.l, pos, maxX, maxY, w, h);
        edges(n.r, pos, maxX, maxY, w, h);
    }

    private double[] xy(double[] p, double maxX, double maxY, double w, double h) {
        return new double[]{
                40 + (p[0] / Math.max(1, maxX)) * (w - 80),
                36 + (p[1] / Math.max(1, maxY)) * (h - 80)
        };
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
        valueInput = UiKit.darkField("8", 60);
        Button ins = UiKit.controlButton("➕  Insert", "#00E5FF");
        Button find = UiKit.controlButton("🔍  Search", "#00E5FF");
        Button inorder = UiKit.controlButton("↦  Inorder", "#A78BFA");
        Button preorder = UiKit.controlButton("↥  Preorder", "#A78BFA");
        Button postorder = UiKit.controlButton("↧  Postorder", "#A78BFA");
        Button seedBtn = UiKit.controlButton("🎲  Random", "#00E5FF");
        Button reset = UiKit.controlButton("🔄  Clear", "#FF4444");
        ins.setOnAction(e -> doInsert());
        find.setOnAction(e -> doFind());
        inorder.setOnAction(e -> traverse("inorder"));
        preorder.setOnAction(e -> traverse("preorder"));
        postorder.setOnAction(e -> traverse("postorder"));
        seedBtn.setOnAction(e -> seed());
        reset.setOnAction(e -> clearTree());
        Slider speed = new Slider(1, 10, 6);
        speed.setPrefWidth(110);
        speed.valueProperty().addListener((o, a, n) ->
                speedDelay = (long) ((11 - n.doubleValue()) * 80_000_000L));
        statsLabel = UiKit.label("Highlight: —");
        panel.getChildren().addAll(
                ins, find, inorder, preorder, postorder,
                UiKit.separator(),
                new VBox(4, UiKit.label("Value:"), valueInput),
                seedBtn, reset,
                UiKit.separator(),
                new VBox(4, UiKit.label("Speed:"), speed),
                UiKit.separator(), statsLabel
        );
        return panel;
    }
}
