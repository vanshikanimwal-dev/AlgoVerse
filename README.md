# AlgoVerse

**[▶ Open in your browser](https://vanshikanimwal-dev.github.io/AlgoVerse/)** — no Java, no install.

Learn algorithms through motion. AlgoVerse animates **sorting**, **searching**, and **pathfinding** so you can see comparisons, swaps, visits, and the shortest path as they happen.

| | |
| --- | --- |
| Live demo | https://vanshikanimwal-dev.github.io/AlgoVerse/ |
| Source | https://github.com/vanshikanimwal-dev/AlgoVerse |

## Try it

Works in Chrome, Edge, Firefox, and Safari.

- [Home](https://vanshikanimwal-dev.github.io/AlgoVerse/)
- [Sorting visualizer](https://vanshikanimwal-dev.github.io/AlgoVerse/#sort)
- [Searching visualizer](https://vanshikanimwal-dev.github.io/AlgoVerse/#search)
- [Pathfinding visualizer](https://vanshikanimwal-dev.github.io/AlgoVerse/#path)

Click a mode, pick an algorithm, press **Play**. Use **[ INFO ]** for complexity and a short explanation. The gold line in the pseudocode follows the current step.

## What is included

- Play / pause / step forward / step back / speed
- Shuffle, array size, pathfinding maze and walls
- Highlighted pseudocode
- Step log and stats (comparisons, moves, visited cells, path length)

**Sorting:** Bubble, Selection, Insertion, Shell, Merge, Quick, Heap, Cocktail Shaker, Counting, Radix, Gnome, Odd-Even

**Searching:** Linear, Binary, Jump

**Pathfinding:** BFS, DFS, Dijkstra, A\*, Greedy Best-First

- Left-drag paints walls; right-drag erases
- Drag cyan **start** and red **end**
- **Weights** (1–5): Dijkstra and A\* use cost; BFS treats every step as 1

## Run the desktop app (optional)

The same visualizers also exist as a JavaFX app if you want them on your machine.

You need **JDK 21**. `JAVA_HOME` must be the JDK **folder**, not `java.exe`.

**Windows**

```powershell
git clone https://github.com/vanshikanimwal-dev/AlgoVerse.git
cd AlgoVerse
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"
.\mvnw.cmd javafx:run
```

Or double-click `run.cmd` (edit the JDK path in that file if yours is different).

**macOS / Linux**

```bash
git clone https://github.com/vanshikanimwal-dev/AlgoVerse.git
cd AlgoVerse
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./mvnw javafx:run
```

## Repo layout

```
docs/           GitHub Pages site (the live demo)
src/main/java   JavaFX desktop app
```

## License

Use and share freely for learning.
