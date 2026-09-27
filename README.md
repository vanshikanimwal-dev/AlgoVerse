# AlgoVerse

Learn algorithms through motion. AlgoVerse is a JavaFX desktop app that animates **sorting**, **searching**, and **pathfinding** so you can see comparisons, swaps, visits, and the shortest path as they happen.

## Features

- Play, pause, step forward/back, shuffle, and speed control
- Live **pseudocode** with the current line highlighted
- Slide-out **INFO** panel: complexity, what it does, and when to use it
- Step log plus running stats (comparisons, moves, visited cells, path length)
- Dark UI, resizable window, Home to jump between visualizers

### Sorting

Bubble, Selection, Insertion, Shell, Merge, Quick, Heap, Cocktail Shaker, Counting, Radix, Gnome, Odd-Even

### Searching

Linear, Binary, Jump

### Pathfinding

BFS, DFS, Dijkstra, A\*, Greedy Best-First

- Left-drag paints walls; right-drag erases
- Drag the cyan **start** and red **end**
- **Maze** generates a recursive-backtracker maze
- **Weights** assigns move costs 1–5. Dijkstra and A\* use cost; BFS still treats every step as 1

## Requirements

- **JDK 21** (tested with Oracle JDK 21.0.12)
- Maven Wrapper is included (`mvnw` / `mvnw.cmd`) — you do not need Maven installed

`JAVA_HOME` must point at the **JDK folder**, not `java.exe`.

| Wrong | Right |
| --- | --- |
| `C:\Program Files\Common Files\Oracle\Java\javapath\java.exe` | `C:\Program Files\Java\jdk-21.0.12` |

## Run

### Windows

From the project folder:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"
.\mvnw.cmd javafx:run
```

Or double-click `run.cmd`. Edit the `JAVA_HOME` path in that file if your JDK is installed somewhere else.

### macOS / Linux

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS
./mvnw javafx:run
```

Compile only:

```powershell
.\mvnw.cmd compile
```

## Project layout

```
src/main/java/com/algoverse/algoverse/
  AlgoVerseApp.java           # window, home, navigation
  SortingVisualizer.java
  SearchingVisualizer.java
  PathfindingVisualizer.java
  AlgoInfoPanel.java          # complexity + explanations
  Pseudocode.java / Pane      # highlighted listings
  UiKit.java                  # shared controls
```

## Tech

Java 21 · JavaFX 21 · Maven
