package com.algoverse.algoverse;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

class PseudocodePane extends VBox {
    private final VBox linesBox = new VBox(2);
    private String[] current = {};

    PseudocodePane() {
        setPrefWidth(320);
        setMinWidth(280);
        setMaxWidth(380);
        setStyle("""
                -fx-background-color: #060912;
                -fx-border-color: #00E5FF;
                -fx-border-width: 0 0 0 1;
                """);

        Label title = new Label("[ PSEUDOCODE ]");
        title.setStyle("""
                -fx-text-fill: #00E5FF;
                -fx-font-family: Monospace;
                -fx-font-size: 13;
                -fx-font-weight: bold;
                -fx-padding: 10 14 8 14;
                """);

        linesBox.setStyle("-fx-background-color: #060912; -fx-padding: 4 10 12 10;");

        ScrollPane scroll = new ScrollPane(linesBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #060912; -fx-background-color: #060912;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(title, scroll);
        load("Bubble Sort");
    }

    void load(String algorithm) {
        current = Pseudocode.lines(algorithm);
        highlight(-1);
    }

    void highlight(int line) {
        linesBox.getChildren().clear();
        for (int i = 0; i < current.length; i++) {
            Label row = new Label(String.format("%2d  %s", i + 1, current[i]));
            row.setWrapText(true);
            row.setMaxWidth(Double.MAX_VALUE);
            if (i == line) {
                row.setStyle("""
                        -fx-text-fill: #0A0E1A;
                        -fx-background-color: #FFD700;
                        -fx-font-family: Monospace;
                        -fx-font-size: 12;
                        -fx-padding: 4 8;
                        """);
            } else {
                row.setStyle("""
                        -fx-text-fill: #94A3B8;
                        -fx-font-family: Monospace;
                        -fx-font-size: 12;
                        -fx-padding: 4 8;
                        """);
            }
            linesBox.getChildren().add(row);
        }
    }
}
