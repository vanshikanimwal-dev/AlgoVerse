# AlgoVerse

Learn algorithms through motion. Watch **sorting**, **searching**, and **pathfinding** animate — comparisons, swaps, visits, and the shortest path — in the browser or as a JavaFX desktop app.

## Use it now (no install)

**[Open the live visualizer →](https://vanshikanimwal-dev.github.io/AlgoVerse/)**

Works in any modern browser. Home → Sorting, Searching, or Pathfinding. Press Play.

Direct links:

- [Sorting](https://vanshikanimwal-dev.github.io/AlgoVerse/#sort)
- [Searching](https://vanshikanimwal-dev.github.io/AlgoVerse/#search)
- [Pathfinding](https://vanshikanimwal-dev.github.io/AlgoVerse/#path)

## Features

- Play, pause, step forward/back, shuffle, and speed control
- Live **pseudocode** with the current line highlighted
- Slide-out **INFO** panel: complexity, what it does, and when to use it
- Step log plus running stats
- Pathfinding: draw walls, drag start/end, generate a maze, optional terrain weights

### Sorting

Bubble, Selection, Insertion, Shell, Merge, Quick, Heap, Cocktail Shaker, Counting, Radix, Gnome, Odd-Even

### Searching

Linear, Binary, Jump

### Pathfinding

BFS, DFS, Dijkstra, A\*, Greedy Best-First

- Left-drag paints walls; right-drag erases
- Drag the cyan **start** and red **end**
- **Weights** assigns move costs 1–5. Dijkstra and A\* use cost; BFS still treats every step as 1

## Run the desktop app (JavaFX)

Needs **JDK 21**. `JAVA_HOME` must be the JDK **folder**, not `java.exe`.

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"
.\mvnw.cmd javafx:run
```

On Windows you can also double-click `run.cmd` (edit the JDK path inside it if needed).

macOS / Linux:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./mvnw javafx:run
```

## Project layout

```
docs/                         # GitHub Pages web app
src/main/java/.../            # JavaFX desktop app
  AlgoVerseApp.java
  SortingVisualizer.java
  SearchingVisualizer.java
  PathfindingVisualizer.java
```

## Tech

Web: HTML, CSS, Canvas  
Desktop: Java 21, JavaFX 21, Maven
