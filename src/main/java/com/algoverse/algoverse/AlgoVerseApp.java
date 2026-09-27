package com.algoverse.algoverse;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class AlgoVerseApp extends Application {
    private BorderPane root;
    private AlgoInfoPanel infoPanel;
    private VBox homeScreen;

    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0A0E1A;");

        root.setTop(createNavBar());
        homeScreen = createHomeScreen();
        root.setCenter(homeScreen);

        infoPanel = new AlgoInfoPanel();
        StackPane stack = new StackPane();
        StackPane.setAlignment(infoPanel, Pos.CENTER_RIGHT);
        stack.getChildren().addAll(root, infoPanel);

        Scene scene = new Scene(stack, 1400, 800);
        stage.setTitle("AlgoVerse");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    private void showHome() {
        stopCurrentView();
        root.setCenter(homeScreen);
        infoPanel.loadAlgorithm("Bubble Sort");
    }

    private void showSorting() {
        stopCurrentView();
        SortingVisualizer visualizer = new SortingVisualizer(infoPanel::loadAlgorithm);
        root.setCenter(visualizer);
        infoPanel.loadAlgorithm("Bubble Sort");
    }

    private void showSearching() {
        stopCurrentView();
        SearchingVisualizer visualizer = new SearchingVisualizer(infoPanel::loadAlgorithm);
        root.setCenter(visualizer);
        infoPanel.loadAlgorithm("Linear Search");
    }

    private void showPathfinding() {
        stopCurrentView();
        PathfindingVisualizer visualizer = new PathfindingVisualizer(infoPanel::loadAlgorithm);
        root.setCenter(visualizer);
        infoPanel.loadAlgorithm("BFS");
    }

    private void stopCurrentView() {
        Node center = root.getCenter();
        if (center instanceof SortingVisualizer v) v.stop();
        if (center instanceof SearchingVisualizer v) v.stop();
        if (center instanceof PathfindingVisualizer v) v.stop();
    }

    private HBox createNavBar() {
        HBox nav = new HBox(16);
        nav.setStyle("""
                -fx-background-color: #1A1F2E;
                -fx-padding: 12 24;
                -fx-border-color: #00E5FF;
                -fx-border-width: 0 0 1 0;
                -fx-alignment: center-left;
                """);

        Text logo = new Text("ALGO VERSE");
        logo.setFill(Color.web("#00E5FF"));
        logo.setFont(Font.font("Monospace", FontWeight.BOLD, 20));
        logo.setOnMouseClicked(e -> showHome());
        logo.setStyle("-fx-cursor: hand;");

        Button homeBtn = UiKit.navButton("[ HOME ]", "#00E5FF");
        homeBtn.setOnAction(e -> showHome());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button infoBtn = UiKit.navButton("[ INFO ]", "#A78BFA");
        infoBtn.setOnAction(e -> {
            if (infoPanel != null) infoPanel.toggle();
        });

        nav.getChildren().addAll(logo, homeBtn, spacer, infoBtn);
        return nav;
    }

    private VBox createHomeScreen() {
        VBox home = new VBox(20);
        home.setStyle("""
                -fx-alignment: center;
                -fx-padding: 48;
                -fx-background-color: #0A0E1A;
                """);

        Text title = new Text("AlgoVerse");
        title.setFill(Color.web("#00E5FF"));
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 64));

        Text tagline = new Text("Learn algorithms through motion.");
        tagline.setFill(Color.web("#F0F4FF"));
        tagline.setFont(Font.font("Monospace", 18));

        Text hint = new Text("Sorting  ·  Searching  ·  Pathfinding");
        hint.setFill(Color.web("#64748B"));
        hint.setFont(Font.font("Monospace", 14));

        Button sortingBtn = createCTAButton("[ Sorting Visualizer ]", "#00E5FF");
        Button searchBtn = createCTAButton("[ Searching Visualizer ]", "#00FF88");
        Button pathBtn = createCTAButton("[ Pathfinding Visualizer ]", "#A78BFA");

        sortingBtn.setOnAction(e -> showSorting());
        searchBtn.setOnAction(e -> showSearching());
        pathBtn.setOnAction(e -> showPathfinding());

        home.getChildren().addAll(title, tagline, hint, sortingBtn, searchBtn, pathBtn);
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
                -fx-min-width: 320;
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
                -fx-min-width: 320;
                """, color, color, color);

        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hovered));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    public static void main(String[] args) {
        launch();
    }
}
