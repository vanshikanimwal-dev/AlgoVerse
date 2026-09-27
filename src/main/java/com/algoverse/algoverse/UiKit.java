package com.algoverse.algoverse;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

final class UiKit {
    private UiKit() {}

    static Button controlButton(String text, String color) {
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
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    static Button navButton(String text, String color) {
        Button btn = new Button(text);
        String normal = String.format("""
                -fx-background-color: transparent;
                -fx-border-color: %s;
                -fx-border-width: 1.5;
                -fx-text-fill: %s;
                -fx-font-family: Monospace;
                -fx-font-size: 13;
                -fx-padding: 6 18;
                -fx-cursor: hand;
                """, color, color);
        String hovered = String.format("""
                -fx-background-color: %s22;
                -fx-border-color: %s;
                -fx-border-width: 1.5;
                -fx-text-fill: %s;
                -fx-font-family: Monospace;
                -fx-font-size: 13;
                -fx-padding: 6 18;
                -fx-cursor: hand;
                """, color, color, color);
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hovered));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    static Label label(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("""
                -fx-text-fill: #F0F4FF;
                -fx-font-family: Monospace;
                -fx-font-size: 14;
                """);
        return lbl;
    }

    static Region separator() {
        Region sep = new Region();
        sep.setStyle("-fx-background-color: #2A2F3E;");
        sep.setPrefWidth(1);
        sep.setPrefHeight(52);
        return sep;
    }

    static TextField darkField(String value, double width) {
        TextField field = new TextField(value);
        field.setPrefWidth(width);
        field.setStyle("""
                -fx-background-color: #0A0E1A;
                -fx-border-color: #00E5FF;
                -fx-border-width: 2;
                -fx-text-fill: #00E5FF;
                -fx-font-family: Monospace;
                -fx-font-size: 14;
                -fx-padding: 6 10;
                """);
        return field;
    }

    static void styleCombo(ComboBox<String> box) {
        box.setStyle("""
                -fx-background-color: #0A0E1A;
                -fx-border-color: #00E5FF;
                -fx-border-width: 2;
                -fx-font-family: Monospace;
                -fx-font-size: 13;
                -fx-min-width: 180;
                -fx-mark-color: #00E5FF;
                """);
        box.setButtonCell(comboCell());
        box.setCellFactory(lv -> comboCell());
    }

    private static ListCell<String> comboCell() {
        return new ListCell<>() {
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
        };
    }

    static String controlBarStyle() {
        return """
                -fx-background-color: #1A1F2E;
                -fx-padding: 14 20;
                -fx-border-color: #00E5FF;
                -fx-border-width: 1 0 0 0;
                -fx-alignment: center-left;
                """;
    }

    static String scrollBarStyle() {
        return """
                -fx-background: #1A1F2E;
                -fx-background-color: #1A1F2E;
                -fx-border-color: #00E5FF;
                -fx-border-width: 1 0 0 0;
                """;
    }

    static Pane bindCanvas(Canvas canvas, Runnable draw) {
        Pane holder = new Pane(canvas);
        canvas.widthProperty().bind(holder.widthProperty());
        canvas.heightProperty().bind(holder.heightProperty());
        holder.widthProperty().addListener((o, a, b) -> draw.run());
        holder.heightProperty().addListener((o, a, b) -> draw.run());
        holder.setMinHeight(280);
        holder.setPrefHeight(440);
        HBox.setHgrow(holder, Priority.ALWAYS);
        VBox.setVgrow(holder, Priority.ALWAYS);
        return holder;
    }
}
