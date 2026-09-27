package com.algoverse.algoverse;

import java.util.Map;

final class Pseudocode {
    private Pseudocode() {}

    private static final Map<String, String[]> LINES = Map.ofEntries(
            Map.entry("Bubble Sort", new String[]{
                    "for i from n-1 down to 1",
                    "  for j from 0 to i-1",
                    "    if a[j] > a[j+1]",
                    "      swap a[j] and a[j+1]",
                    "  mark a[i] sorted",
                    "done"
            }),
            Map.entry("Selection Sort", new String[]{
                    "for i from 0 to n-2",
                    "  min ← i",
                    "  for j from i+1 to n-1",
                    "    if a[j] < a[min]: min ← j",
                    "  swap a[i] and a[min]",
                    "  mark a[i] sorted",
                    "done"
            }),
            Map.entry("Insertion Sort", new String[]{
                    "for i from 1 to n-1",
                    "  j ← i",
                    "  while j > 0 and a[j] < a[j-1]",
                    "    swap a[j] and a[j-1]",
                    "    j ← j-1",
                    "  mark a[0..i] sorted",
                    "done"
            }),
            Map.entry("Shell Sort", new String[]{
                    "for gap = n/2, gap/2, ... 1",
                    "  for i from gap to n-1",
                    "    j ← i",
                    "    while j ≥ gap and a[j] < a[j-gap]",
                    "      swap a[j] and a[j-gap]",
                    "      j ← j-gap",
                    "done"
            }),
            Map.entry("Merge Sort", new String[]{
                    "split array into halves",
                    "recursively sort left",
                    "recursively sort right",
                    "compare next left vs right",
                    "write the smaller value",
                    "copy leftovers",
                    "done"
            }),
            Map.entry("Quick Sort", new String[]{
                    "pick pivot = a[high]",
                    "for j from low to high-1",
                    "  if a[j] ≤ pivot",
                    "    swap into left partition",
                    "swap pivot into place",
                    "recurse on left and right",
                    "done"
            }),
            Map.entry("Heap Sort", new String[]{
                    "build max-heap",
                    "compare node with children",
                    "swap with larger child if needed",
                    "swap heap root with end",
                    "heapify the reduced heap",
                    "done"
            }),
            Map.entry("Cocktail Shaker Sort", new String[]{
                    "while swapped",
                    "  scan left → right, swap if needed",
                    "  scan right → left, swap if needed",
                    "  shrink start and end",
                    "done"
            }),
            Map.entry("Counting Sort", new String[]{
                    "find max value",
                    "count occurrences of each value",
                    "write values back in order",
                    "done"
            }),
            Map.entry("Radix Sort", new String[]{
                    "find the maximum value",
                    "for each digit place (1s, 10s, ...)",
                    "  counting-sort by that digit",
                    "  write the digit-stable order",
                    "done"
            }),
            Map.entry("Gnome Sort", new String[]{
                    "i ← 0",
                    "while i < n",
                    "  if a[i] ≥ a[i-1]: i ← i+1",
                    "  else swap a[i] and a[i-1]; i ← i-1",
                    "done"
            }),
            Map.entry("Odd-Even Sort", new String[]{
                    "while not sorted",
                    "  compare/swap odd pairs",
                    "  compare/swap even pairs",
                    "done"
            }),
            Map.entry("Linear Search", new String[]{
                    "for i from 0 to n-1",
                    "  if a[i] = target: return i",
                    "return not found"
            }),
            Map.entry("Binary Search", new String[]{
                    "low ← 0, high ← n-1",
                    "while low ≤ high",
                    "  mid ← (low + high) / 2",
                    "  if a[mid] = target: return mid",
                    "  if a[mid] < target: low ← mid+1",
                    "  else high ← mid-1",
                    "return not found"
            }),
            Map.entry("Jump Search", new String[]{
                    "step ← √n, prev ← 0",
                    "jump ahead while a[block] < target",
                    "linear scan inside the block",
                    "if a[i] = target: return i",
                    "return not found"
            }),
            Map.entry("BFS", new String[]{
                    "queue ← {start}",
                    "while queue is not empty",
                    "  u ← dequeue",
                    "  if u is the goal: rebuild path",
                    "  for each neighbor v of u",
                    "    if v unseen: enqueue v",
                    "return no path"
            }),
            Map.entry("DFS", new String[]{
                    "stack ← {start}",
                    "while stack is not empty",
                    "  u ← pop",
                    "  if u is the goal: rebuild path",
                    "  for each neighbor v of u",
                    "    if v unseen: push v",
                    "return no path"
            }),
            Map.entry("Dijkstra", new String[]{
                    "dist[start] ← 0, pq ← {start}",
                    "while pq is not empty",
                    "  u ← pop closest by dist",
                    "  if u is the goal: rebuild path",
                    "  for each neighbor v",
                    "    relax dist[v] via cost(u,v)",
                    "return no path"
            }),
            Map.entry("A*", new String[]{
                    "g[start] ← 0, f ← g + h",
                    "while open set is not empty",
                    "  u ← pop lowest f = g + h",
                    "  if u is the goal: rebuild path",
                    "  for each neighbor v",
                    "    relax g[v] and update f",
                    "return no path"
            }),
            Map.entry("Greedy Best-First", new String[]{
                    "open ← {start}",
                    "while open is not empty",
                    "  u ← pop closest to goal (h only)",
                    "  if u is the goal: rebuild path",
                    "  expand unseen neighbors",
                    "return no path"
            })
    );

    private static final Map<String, Map<String, Integer>> EVENT_LINE = Map.ofEntries(
            Map.entry("Bubble Sort", Map.of("compare", 2, "swap", 3, "mark", 4, "done", 5)),
            Map.entry("Selection Sort", Map.of("compare", 3, "swap", 4, "mark", 5, "done", 6)),
            Map.entry("Insertion Sort", Map.of("compare", 2, "swap", 3, "mark", 5, "done", 6)),
            Map.entry("Shell Sort", Map.of("compare", 3, "swap", 4, "done", 6)),
            Map.entry("Merge Sort", Map.of("compare", 3, "swap", 4, "done", 6)),
            Map.entry("Quick Sort", Map.of("compare", 2, "swap", 3, "mark", 4, "done", 6)),
            Map.entry("Heap Sort", Map.of("compare", 1, "swap", 2, "mark", 3, "done", 5)),
            Map.entry("Cocktail Shaker Sort", Map.of("compare", 1, "swap", 1, "done", 4)),
            Map.entry("Counting Sort", Map.of("compare", 0, "swap", 2, "mark", 2, "done", 3)),
            Map.entry("Radix Sort", Map.of("compare", 1, "swap", 3, "mark", 3, "done", 4)),
            Map.entry("Gnome Sort", Map.of("compare", 2, "swap", 3, "done", 4)),
            Map.entry("Odd-Even Sort", Map.of("compare", 1, "swap", 1, "done", 3)),
            Map.entry("Linear Search", Map.of("compare", 1, "found", 1, "miss", 2)),
            Map.entry("Binary Search", Map.of("compare", 2, "found", 3, "miss", 6)),
            Map.entry("Jump Search", Map.of("compare", 1, "jump", 1, "found", 3, "miss", 4)),
            Map.entry("BFS", Map.of("visit", 4, "path", 3, "done", 3, "fail", 6, "start", 0)),
            Map.entry("DFS", Map.of("visit", 4, "path", 3, "done", 3, "fail", 6, "start", 0)),
            Map.entry("Dijkstra", Map.of("visit", 2, "path", 3, "done", 3, "fail", 6, "start", 0)),
            Map.entry("A*", Map.of("visit", 2, "path", 3, "done", 3, "fail", 6, "start", 0)),
            Map.entry("Greedy Best-First", Map.of("visit", 4, "path", 3, "done", 3, "fail", 5, "start", 0))
    );

    static String[] lines(String algorithm) {
        return LINES.getOrDefault(algorithm, new String[]{"(no listing for this algorithm)"});
    }

    static int line(String algorithm, String event) {
        Map<String, Integer> events = EVENT_LINE.get(algorithm);
        if (events == null) return -1;
        return events.getOrDefault(event, -1);
    }
}
