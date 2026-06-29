package com.algoverse.algoverse;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    private BorderPane root;
    private AlgoInfoPanel infoPanel;
    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0A0E1A;");

        HBox navBar = createNavBar();
        root.setTop(navBar);

        VBox homeScreen = createHomeScreen();
        root.setCenter(homeScreen);

        // wrap root + info panel in a StackPane
        infoPanel = new AlgoInfoPanel();
        StackPane stack = new StackPane();
        StackPane.setAlignment(infoPanel, javafx.geometry.Pos.CENTER_RIGHT);
        stack.getChildren().addAll(root, infoPanel);

        Scene scene = new Scene(stack, 1400, 800);
        stage.setTitle("AlgoVerse");
        stage.setScene(scene);
        stage.show();
    }

    private HBox createNavBar() {
        HBox nav = new HBox(20);
        nav.setStyle("""
            -fx-background-color: #1A1F2E;
            -fx-padding: 12 24;
            -fx-border-color: #00E5FF;
            -fx-border-width: 0 0 1 0;
        """);

        Text logo = new Text("ALGO VERSE");
        logo.setFill(Color.web("#00E5FF"));
        logo.setFont(Font.font("Monospace", FontWeight.BOLD, 20));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button infoBtn = new Button("[ INFO ]");
        infoBtn.setStyle("""
    -fx-background-color: transparent;
    -fx-border-color: #A78BFA;
    -fx-border-width: 1.5;
    -fx-text-fill: #A78BFA;
    -fx-font-family: Monospace;
    -fx-font-size: 13;
    -fx-padding: 6 18;
    -fx-cursor: hand;
""");
        infoBtn.setOnAction(e -> {
            if (infoPanel != null) infoPanel.toggle();
        });

        nav.getChildren().addAll(logo, spacer, infoBtn);
        return nav;
    }

    private VBox createHomeScreen() {
        VBox home = new VBox(24);
        home.setStyle("""
        -fx-alignment: center;
        -fx-padding: 60;
        -fx-background-color: #0A0E1A;
    """);

        Text title = new Text("AlgoVerse");
        title.setFill(Color.web("#00E5FF"));
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 64));

        Text tagline = new Text("Learn algorithms through motion.");
        tagline.setFill(Color.web("#F0F4FF"));
        tagline.setFont(Font.font("Monospace", 18));

        Button sortingBtn = createCTAButton("[ Sorting Visualizer ]", "#00E5FF");
        Button pathBtn    = createCTAButton("[ Pathfinding Visualizer ]", "#A78BFA");

        // ── Actions ──
        sortingBtn.setOnAction(e -> {
            System.out.println("Button clicked!");
            try {
                SortingVisualizer visualizer = new SortingVisualizer();
                System.out.println("Visualizer created!");
                root.setCenter(visualizer);
                System.out.println("Center set!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        pathBtn.setOnAction(e -> {
            // Pathfinding coming soon
            Text soon = new Text("Pathfinding Visualizer — Coming Soon!");
            soon.setFill(Color.web("#A78BFA"));
            soon.setFont(Font.font("Monospace", FontWeight.BOLD, 32));
            VBox placeholder = new VBox(soon);
            placeholder.setStyle("-fx-alignment: center; -fx-background-color: #0A0E1A;");
            root.setCenter(placeholder);
        });

        home.getChildren().addAll(title, tagline, sortingBtn, pathBtn);
        return home;
    }

    private Button createCTAButton(String text, String color) {
        Button btn = new Button(text);

        String normal = String.format("""
        -fx-background-color: transparent;
        -fx-border-color: %s;
        -fx-border-width: 1.5;
        -fx-text-fill: %s;
        -fx-font-family: Monospace;
        -fx-font-size: 16;
        -fx-padding: 12 32;
        -fx-cursor: hand;
        -fx-min-width: 280;
    """, color, color);

        String hovered = String.format("""
        -fx-background-color: %s22;
        -fx-border-color: %s;
        -fx-border-width: 1.5;
        -fx-text-fill: %s;
        -fx-font-family: Monospace;
        -fx-font-size: 16;
        -fx-padding: 12 32;
        -fx-cursor: hand;
        -fx-min-width: 280;
    """, color, color, color);

        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hovered));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    private void addHoverEffect(Button btn, String color) {
        String normal = btn.getStyle();
        String hovered = normal + String.format("""
            -fx-background-color: %s22;
        """, color);

        btn.setOnMouseEntered(e -> btn.setStyle(hovered));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
    }

    public static void main(String[] args) {
        launch();
    }
}
