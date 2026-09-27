(() => {
  const view = document.getElementById("view");
  const infoPanel = document.getElementById("infoPanel");
  const infoBody = document.getElementById("infoBody");
  let stopFn = null;
  let currentAlgo = "Bubble Sort";

  document.getElementById("homeBtn").onclick = () => { location.hash = ""; showHome(); };
  document.getElementById("homeNav").onclick = () => { location.hash = ""; showHome(); };
  document.getElementById("infoBtn").onclick = () => {
    infoPanel.hidden = !infoPanel.hidden;
    renderInfo(currentAlgo);
  };
  document.getElementById("infoClose").onclick = () => { infoPanel.hidden = true; };

  function setAlgo(name) {
    currentAlgo = name;
    if (!infoPanel.hidden) renderInfo(name);
  }

  function renderInfo(name) {
    const d = INFO[name];
    if (!d) return;
    const [best, avg, worst, space] = d.c;
    infoBody.innerHTML = `
      <h2>${name}</h2>
      <h3>What it does</h3><p>${d.e}</p>
      <h3>Complexity</h3>
      <div class="info-row"><span>Best Case</span><span>${best}</span></div>
      <div class="info-row"><span>Average Case</span><span>${avg}</span></div>
      <div class="info-row"><span>Worst Case</span><span>${worst}</span></div>
      <div class="info-row"><span>Space</span><span>${space}</span></div>
      <h3>When to use</h3><p>${d.w}</p>`;
  }

  function renderCode(el, algo, line) {
    const lines = CODE[algo] || ["(no listing)"];
    el.innerHTML = `<h3>[ PSEUDOCODE ]</h3><pre>${lines.map((l, i) =>
      `<span class="${i === line ? "hl" : ""}">${String(i + 1).padStart(2, " ")}  ${l}</span>`
    ).join("\n")}</pre>`;
  }

  function shell(extraControls) {
    view.innerHTML = `
      <section class="viz">
        <div class="stage">
          <canvas id="c"></canvas>
          <div class="code" id="code"></div>
        </div>
        <div class="controls" id="controls">${extraControls}<div class="stats" id="stats"></div></div>
        <div class="log" id="log"></div>
      </section>`;
    return {
      canvas: document.getElementById("c"),
      code: document.getElementById("code"),
      log: document.getElementById("log"),
      stats: document.getElementById("stats")
    };
  }

  function appendLog(log, text, cls) {
    const row = document.createElement("div");
    row.className = cls || "purple";
    row.textContent = text;
    log.appendChild(row);
    log.scrollTop = log.scrollHeight;
  }

  function showHome() {
    if (stopFn) stopFn();
    if (location.hash) history.replaceState(null, "", location.pathname + location.search);
    setAlgo("Bubble Sort");
    view.innerHTML = `
      <section class="home">
        <h1>AlgoVerse</h1>
        <div class="tag">Learn algorithms through motion.</div>
        <div class="sub">Sorting  ·  Searching  ·  Pathfinding — runs in your browser</div>
        <button class="cta cyan" data-go="sort">[ Sorting Visualizer ]</button>
        <button class="cta green" data-go="search">[ Searching Visualizer ]</button>
        <button class="cta purple" data-go="path">[ Pathfinding Visualizer ]</button>
      </section>`;
    view.querySelectorAll("[data-go]").forEach((b) => {
      b.onclick = () => { location.hash = b.dataset.go; };
    });
  }

  const SORTS = ["Bubble Sort","Selection Sort","Insertion Sort","Merge Sort","Quick Sort","Shell Sort","Heap Sort","Cocktail Shaker Sort","Counting Sort","Radix Sort","Gnome Sort","Odd-Even Sort"];

  function randArray(n, max = 400) {
    return Array.from({ length: n }, () => 20 + Math.floor(Math.random() * max));
  }

  function sortSteps(algo, arr) {
    const a = arr.slice();
    const steps = [];
    let cmp = 0, mov = 0;
    const rec = (event, comparing = [], swapping = [], sorted = [], msg, cls) => {
      const line = (CODE_LINE[algo] || {})[event] ?? -1;
      steps.push({ array: a.slice(), comparing, swapping, sorted: sorted.slice(), cmp, mov, line, msg, cls });
    };
    const n = a.length;
    if (algo === "Bubble Sort") {
      for (let i = 0; i < n - 1; i++) {
        for (let j = 0; j < n - i - 1; j++) {
          cmp++; rec("compare", [j, j + 1], [], [], `Comparing idx ${j} ↔ ${j + 1}`, "gold");
          if (a[j] > a[j + 1]) {
            mov++; [a[j], a[j + 1]] = [a[j + 1], a[j]];
            rec("swap", [], [j, j + 1], [], `Swapping idx ${j} ↔ ${j + 1}`, "red");
          }
        }
        rec("mark", [], [], [...Array(i + 1)].map((_, k) => n - 1 - k), `Sorted ${i + 1} in place`, "green");
      }
      rec("done", [], [], [...Array(n).keys()], "Sorting complete", "green");
    } else if (algo === "Selection Sort") {
      for (let i = 0; i < n - 1; i++) {
        let min = i;
        for (let j = i + 1; j < n; j++) {
          cmp++; rec("compare", [min, j], [], [...Array(i).keys()], `Comparing ${min} ↔ ${j}`, "gold");
          if (a[j] < a[min]) min = j;
        }
        if (min !== i) {
          mov++; [a[i], a[min]] = [a[min], a[i]];
          rec("swap", [], [i, min], [...Array(i).keys()], `Swapping ${i} ↔ ${min}`, "red");
        }
        rec("mark", [], [], [...Array(i + 1).keys()], `Sorted ${i + 1} in place`, "green");
      }
      rec("done", [], [], [...Array(n).keys()], "Sorting complete", "green");
    } else if (algo === "Insertion Sort") {
      for (let i = 1; i < n; i++) {
        let j = i;
        while (j > 0) {
          cmp++; rec("compare", [j - 1, j], [], [...Array(j).keys()], `Comparing ${j - 1} ↔ ${j}`, "gold");
          if (a[j] < a[j - 1]) {
            mov++; [a[j], a[j - 1]] = [a[j - 1], a[j]];
            rec("swap", [], [j, j - 1], [], `Swapping ${j} ↔ ${j - 1}`, "red");
            j--;
          } else break;
        }
        rec("mark", [], [], [...Array(i + 1).keys()], `Sorted ${i + 1} in place`, "green");
      }
      rec("done", [], [], [...Array(n).keys()], "Sorting complete", "green");
    } else if (algo === "Shell Sort") {
      for (let gap = Math.floor(n / 2); gap > 0; gap = Math.floor(gap / 2)) {
        for (let i = gap; i < n; i++) {
          let j = i;
          while (j >= gap) {
            cmp++; rec("compare", [j - gap, j], [], [], `Gap ${gap}: ${j - gap} ↔ ${j}`, "gold");
            if (a[j] < a[j - gap]) {
              mov++; [a[j], a[j - gap]] = [a[j - gap], a[j]];
              rec("swap", [], [j, j - gap], [], `Swapping ${j} ↔ ${j - gap}`, "red");
              j -= gap;
            } else break;
          }
        }
      }
      rec("done", [], [], [...Array(n).keys()], "Sorting complete", "green");
    } else if (algo === "Merge Sort") {
      const mergeSort = (l, r) => {
        if (l >= r) return;
        const m = Math.floor((l + r) / 2);
        mergeSort(l, m); mergeSort(m + 1, r);
        const left = a.slice(l, m + 1), right = a.slice(m + 1, r + 1);
        let i = 0, j = 0, k = l;
        while (i < left.length && j < right.length) {
          cmp++; rec("compare", [l + i, m + 1 + j], [], [], `Merging ${left[i]} vs ${right[j]}`, "gold");
          if (left[i] <= right[j]) a[k++] = left[i++];
          else { a[k++] = right[j++]; mov++; }
          rec("swap", [], [k - 1], [], `Write idx ${k - 1}`, "red");
        }
        while (i < left.length) a[k++] = left[i++];
        while (j < right.length) a[k++] = right[j++];
      };
      mergeSort(0, n - 1);
      rec("done", [], [], [...Array(n).keys()], "Sorting complete", "green");
    } else if (algo === "Quick Sort") {
      const partition = (lo, hi) => {
        const pivot = a[hi];
        let i = lo - 1;
        for (let j = lo; j < hi; j++) {
          cmp++; rec("compare", [j, hi], [], [], `Compare ${a[j]} to pivot ${pivot}`, "gold");
          if (a[j] <= pivot) {
            i++; mov++; [a[i], a[j]] = [a[j], a[i]];
            rec("swap", [], [i, j], [], `Swap into partition ${i} ↔ ${j}`, "red");
          }
        }
        [a[i + 1], a[hi]] = [a[hi], a[i + 1]];
        rec("mark", [], [i + 1, hi], [], `Pivot at ${i + 1}`, "red");
        return i + 1;
      };
      const qs = (lo, hi) => { if (lo < hi) { const p = partition(lo, hi); qs(lo, p - 1); qs(p + 1, hi); } };
      qs(0, n - 1);
      rec("done", [], [], [...Array(n).keys()], "Sorting complete", "green");
    } else if (algo === "Heap Sort") {
      const heapify = (len, i) => {
        let largest = i, l = 2 * i + 1, r = 2 * i + 2;
        if (l < len) { cmp++; rec("compare", [largest, l], [], [], `Heap compare ${largest} ↔ ${l}`, "gold"); if (a[l] > a[largest]) largest = l; }
        if (r < len) { cmp++; rec("compare", [largest, r], [], [], `Heap compare ${largest} ↔ ${r}`, "gold"); if (a[r] > a[largest]) largest = r; }
        if (largest !== i) {
          mov++; [a[i], a[largest]] = [a[largest], a[i]];
          rec("swap", [], [i, largest], [], `Heap swap ${i} ↔ ${largest}`, "red");
          heapify(len, largest);
        }
      };
      for (let i = Math.floor(n / 2) - 1; i >= 0; i--) heapify(n, i);
      for (let i = n - 1; i > 0; i--) {
        mov++; [a[0], a[i]] = [a[i], a[0]];
        rec("swap", [], [0, i], [], `Extract max to ${i}`, "red");
        rec("mark", [], [], [...Array(n - i)].map((_, k) => i + k), `Sorted suffix`, "green");
        heapify(i, 0);
      }
      rec("done", [], [], [...Array(n).keys()], "Sorting complete", "green");
    } else if (algo === "Cocktail Shaker Sort") {
      let swapped = true, start = 0, end = n - 1;
      while (swapped) {
        swapped = false;
        for (let i = start; i < end; i++) {
          cmp++; rec("compare", [i, i + 1], [], [], `→ ${i} ↔ ${i + 1}`, "gold");
          if (a[i] > a[i + 1]) { mov++; [a[i], a[i + 1]] = [a[i + 1], a[i]]; swapped = true; rec("swap", [], [i, i + 1], [], `Swap`, "red"); }
        }
        if (!swapped) break;
        swapped = false; end--;
        for (let i = end - 1; i >= start; i--) {
          cmp++; rec("compare", [i, i + 1], [], [], `← ${i} ↔ ${i + 1}`, "gold");
          if (a[i] > a[i + 1]) { mov++; [a[i], a[i + 1]] = [a[i + 1], a[i]]; swapped = true; rec("swap", [], [i, i + 1], [], `Swap`, "red"); }
        }
        start++;
      }
      rec("done", [], [], [...Array(n).keys()], "Sorting complete", "green");
    } else if (algo === "Counting Sort") {
      let max = a[0];
      for (let i = 1; i < n; i++) { cmp++; rec("compare", [i, 0], [], [], `Scan max`, "gold"); if (a[i] > max) max = a[i]; }
      const count = Array(max + 1).fill(0);
      a.forEach((v) => count[v]++);
      const out = [];
      for (let i = 0; i <= max; i++) while (count[i]--) out.push(i);
      for (let i = 0; i < n; i++) {
        a[i] = out[i]; mov++;
        rec("swap", [], [i], [...Array(i + 1).keys()], `Place ${a[i]} at ${i}`, "red");
      }
      rec("done", [], [], [...Array(n).keys()], "Sorting complete", "green");
    } else if (algo === "Radix Sort") {
      const max = Math.max(...a);
      for (let exp = 1; Math.floor(max / exp) > 0; exp *= 10) {
        const output = Array(n), count = Array(10).fill(0);
        for (let i = 0; i < n; i++) count[Math.floor(a[i] / exp) % 10]++;
        for (let i = 1; i < 10; i++) count[i] += count[i - 1];
        for (let i = n - 1; i >= 0; i--) {
          const d = Math.floor(a[i] / exp) % 10;
          output[--count[d]] = a[i];
        }
        for (let i = 0; i < n; i++) {
          a[i] = output[i]; mov++;
          rec("swap", [], [i], [], `Digit ${exp}: write idx ${i}`, "red");
        }
      }
      rec("done", [], [], [...Array(n).keys()], "Sorting complete", "green");
    } else if (algo === "Gnome Sort") {
      let i = 0;
      while (i < n) {
        if (i === 0) i++;
        else {
          cmp++; rec("compare", [i - 1, i], [], [], `Compare ${i - 1} ↔ ${i}`, "gold");
          if (a[i] >= a[i - 1]) i++;
          else { mov++; [a[i], a[i - 1]] = [a[i - 1], a[i]]; rec("swap", [], [i, i - 1], [], `Swap back`, "red"); i--; }
        }
      }
      rec("done", [], [], [...Array(n).keys()], "Sorting complete", "green");
    } else {
      let sorted = false;
      while (!sorted) {
        sorted = true;
        for (let i = 1; i < n - 1; i += 2) {
          cmp++; rec("compare", [i, i + 1], [], [], `Odd ${i}`, "gold");
          if (a[i] > a[i + 1]) { mov++; [a[i], a[i + 1]] = [a[i + 1], a[i]]; sorted = false; rec("swap", [], [i, i + 1], [], `Swap`, "red"); }
        }
        for (let i = 0; i < n - 1; i += 2) {
          cmp++; rec("compare", [i, i + 1], [], [], `Even ${i}`, "gold");
          if (a[i] > a[i + 1]) { mov++; [a[i], a[i + 1]] = [a[i + 1], a[i]]; sorted = false; rec("swap", [], [i, i + 1], [], `Swap`, "red"); }
        }
      }
      rec("done", [], [], [...Array(n).keys()], "Sorting complete", "green");
    }
    return steps;
  }

  function drawBars(canvas, step) {
    const ctx = canvas.getContext("2d");
    const w = canvas.width = canvas.clientWidth;
    const h = canvas.height = canvas.clientHeight;
    ctx.fillStyle = "#0a0e1a"; ctx.fillRect(0, 0, w, h);
    const arr = step.array;
    const max = Math.max(...arr, 1);
    const bw = w / arr.length - 2;
    arr.forEach((v, i) => {
      const bh = (v / max) * (h - 8);
      let color = lerpColor("#00e5ff", "#a78bfa", i / Math.max(1, arr.length - 1));
      if (step.sorted.includes(i)) color = "#00ff88";
      if (step.swapping.includes(i)) color = "#ff4444";
      if (step.comparing.includes(i)) color = "#ffd700";
      ctx.fillStyle = color;
      ctx.fillRect(i * (bw + 2), h - bh, bw, bh);
    });
  }

  function lerpColor(a, b, t) {
    const p = (hex) => hex.slice(1).match(/.{2}/g).map((x) => parseInt(x, 16));
    const [ar, ag, ab] = p(a), [br, bg, bb] = p(b);
    const r = Math.round(ar + (br - ar) * t), g = Math.round(ag + (bg - ag) * t), bl = Math.round(ab + (bb - ab) * t);
    return `rgb(${r},${g},${bl})`;
  }

  function player(getDelay) {
    let i = 0, playing = false, timer = 0, steps = [], onStep = () => {};
    const tick = (now) => {
      timer = requestAnimationFrame(tick);
      if (!playing) return;
      if (!tick.last || now - tick.last >= getDelay()) {
        tick.last = now;
        if (i < steps.length) onStep(steps[i], i++);
        else playing = false;
      }
    };
    timer = requestAnimationFrame(tick);
    return {
      set(s, cb) { steps = s; onStep = cb; i = 0; playing = false; },
      play() { playing = true; },
      pause() { playing = false; },
      fwd() { playing = false; if (i < steps.length) onStep(steps[i], i++); },
      back() {
        playing = false;
        const t = Math.max(0, i - 1);
        i = 0;
        return t;
      },
      reset() { playing = false; i = 0; },
      index() { return i; },
      stop() { cancelAnimationFrame(timer); playing = false; }
    };
  }

  function showSort() {
    if (stopFn) stopFn();
    const ui = shell(`
      <select id="algo">${SORTS.map((s) => `<option>${s}</option>`).join("")}</select>
      <button id="shuffle">Shuffle</button>
      <button id="play">Play</button>
      <button class="muted" id="pause">Pause</button>
      <button class="muted" id="back">Back</button>
      <button class="muted" id="fwd">Forward</button>
      <button class="muted" id="reset">Reset</button>
      <label>Size <input id="size" value="30" size="4"></label>
      <button id="apply">Apply</button>
      <label>Speed <input id="speed" type="range" min="1" max="10" value="6"></label>
    `);
    let array = randArray(30);
    let steps = [];
    let speed = 6;
    const p = player(() => (11 - speed) * 50);
    const applyStep = (st, rebuildLog) => {
      drawBars(ui.canvas, st);
      renderCode(ui.code, currentAlgo, st.line);
      ui.stats.innerHTML = `Comparisons: ${st.cmp}<br>Moves: ${st.mov}`;
      if (!rebuildLog) appendLog(ui.log, st.msg, st.cls);
    };
    const rebuild = (algo, arr) => {
      setAlgo(algo);
      steps = sortSteps(algo, arr);
      ui.log.innerHTML = "";
      appendLog(ui.log, "Ready — press Play.", "purple");
      renderCode(ui.code, algo, -1);
      const zero = { array: arr.slice(), comparing: [], swapping: [], sorted: [], cmp: 0, mov: 0, line: -1 };
      drawBars(ui.canvas, zero);
      ui.stats.innerHTML = "Comparisons: 0<br>Moves: 0";
      p.set(steps, (st) => applyStep(st, false));
    };
    rebuild("Bubble Sort", array);
    const replayTo = (n) => {
      ui.log.innerHTML = "";
      for (let k = 0; k < n; k++) applyStep(steps[k], false);
      p.set(steps, (st) => applyStep(st, false));
      for (let k = 0; k < n; k++) p.fwd();
    };
    document.getElementById("algo").onchange = (e) => rebuild(e.target.value, array);
    document.getElementById("shuffle").onclick = () => { array = randArray(+document.getElementById("size").value || 30); rebuild(currentAlgo, array); };
    document.getElementById("apply").onclick = () => {
      const n = Math.min(80, Math.max(5, +document.getElementById("size").value || 30));
      array = randArray(n); rebuild(currentAlgo, array);
    };
    document.getElementById("play").onclick = () => p.play();
    document.getElementById("pause").onclick = () => p.pause();
    document.getElementById("fwd").onclick = () => p.fwd();
    document.getElementById("back").onclick = () => replayTo(Math.max(0, p.index() - 1));
    document.getElementById("reset").onclick = () => rebuild(currentAlgo, array);
    document.getElementById("speed").oninput = (e) => { speed = +e.target.value; };
    window.onresize = () => { const st = steps[Math.max(0, p.index() - 1)] || { array, comparing: [], swapping: [], sorted: [] }; drawBars(ui.canvas, st); };
    stopFn = () => { p.stop(); window.onresize = null; };
  }

  function searchSteps(algo, arr, target) {
    const a = arr.slice();
    if (algo !== "Linear Search") a.sort((x, y) => x - y);
    const steps = [];
    let cmp = 0;
    const rec = (event, comparing, lo, hi, found, msg, cls) => {
      steps.push({ array: a.slice(), comparing, lo, hi, found, cmp, line: (CODE_LINE[algo] || {})[event] ?? -1, msg, cls });
    };
    if (algo === "Linear Search") {
      for (let i = 0; i < a.length; i++) {
        cmp++; rec("compare", [i], 0, a.length - 1, -1, `Compare idx ${i} (${a[i]}) to ${target}`, "gold");
        if (a[i] === target) { rec("found", [], 0, a.length - 1, i, `Found at ${i}`, "green"); return steps; }
      }
      rec("miss", [], 0, a.length - 1, -2, "Not found", "red");
    } else if (algo === "Binary Search") {
      let lo = 0, hi = a.length - 1;
      while (lo <= hi) {
        const mid = (lo + hi) >> 1;
        cmp++; rec("compare", [mid], lo, hi, -1, `Low=${lo} High=${hi} Mid=${mid}`, "gold");
        if (a[mid] === target) { rec("found", [], lo, hi, mid, `Found at ${mid}`, "green"); return steps; }
        if (a[mid] < target) lo = mid + 1; else hi = mid - 1;
      }
      rec("miss", [], 0, a.length - 1, -2, "Not found", "red");
    } else {
      const n = a.length; let step = Math.floor(Math.sqrt(n)) || 1, prev = 0;
      while (prev < n && a[Math.min(step, n) - 1] < target) {
        cmp++; rec("jump", [Math.min(step, n) - 1], prev, Math.min(step, n) - 1, -1, `Jump to ${Math.min(step, n) - 1}`, "gold");
        prev = step; step += Math.floor(Math.sqrt(n));
        if (prev >= n) break;
      }
      for (let i = prev; i < Math.min(step, n); i++) {
        cmp++; rec("compare", [i], prev, Math.min(step, n) - 1, -1, `Scan idx ${i}`, "gold");
        if (a[i] === target) { rec("found", [], prev, Math.min(step, n) - 1, i, `Found at ${i}`, "green"); return steps; }
      }
      rec("miss", [], 0, n - 1, -2, "Not found", "red");
    }
    return steps;
  }

  function drawSearch(canvas, st) {
    const ctx = canvas.getContext("2d");
    const w = canvas.width = canvas.clientWidth;
    const h = canvas.height = canvas.clientHeight;
    ctx.fillStyle = "#0a0e1a"; ctx.fillRect(0, 0, w, h);
    const box = Math.min(64, w / st.array.length - 8);
    const total = st.array.length * (box + 8) - 8;
    const ox = (w - total) / 2;
    const y = h / 2 - box / 2;
    ctx.fillStyle = "#94a3b8"; ctx.font = "14px monospace";
    ctx.fillText("Target: " + (st.target ?? ""), 16, 28);
    st.array.forEach((v, i) => {
      const x = ox + i * (box + 8);
      let color = lerpColor("#00e5ff", "#a78bfa", i / Math.max(1, st.array.length - 1));
      if (i < st.lo || i > st.hi) color = "#1e293b";
      if (st.comparing.includes(i)) color = "#ffd700";
      if (st.found >= 0 && i === st.found) color = "#00ff88";
      ctx.fillStyle = color;
      ctx.fillRect(x, y, box, box);
      ctx.fillStyle = "#0a0e1a"; ctx.font = `bold ${Math.max(11, box / 3)}px monospace`;
      ctx.fillText(String(v), x + 8, y + box / 2 + 4);
    });
  }

  function showSearch() {
    if (stopFn) stopFn();
    const ui = shell(`
      <select id="algo"><option>Linear Search</option><option>Binary Search</option><option>Jump Search</option></select>
      <button id="shuffle">Shuffle</button>
      <button id="play">Play</button>
      <button class="muted" id="pause">Pause</button>
      <button class="muted" id="back">Back</button>
      <button class="muted" id="fwd">Forward</button>
      <button class="muted" id="reset">Reset</button>
      <label>Size <input id="size" value="16" size="3"></label>
      <label>Target <input id="target" value="0" size="4"></label>
      <button id="apply">Apply</button>
      <label>Speed <input id="speed" type="range" min="1" max="10" value="5"></label>
    `);
    let array = randArray(16, 80);
    let target = array[Math.floor(Math.random() * array.length)];
    document.getElementById("target").value = target;
    let steps = [];
    let speed = 5;
    const p = player(() => (11 - speed) * 80);
    const applyStep = (st) => {
      st.target = target;
      drawSearch(ui.canvas, st);
      renderCode(ui.code, currentAlgo, st.line);
      ui.stats.innerHTML = `Comparisons: ${st.cmp}<br>Result: ${st.found >= 0 ? "index " + st.found : st.found === -2 ? "not found" : "…"}`;
      appendLog(ui.log, st.msg, st.cls);
    };
    const rebuild = (algo) => {
      setAlgo(algo);
      target = +document.getElementById("target").value || target;
      steps = searchSteps(algo, array, target);
      ui.log.innerHTML = "";
      appendLog(ui.log, "Ready — press Play.", "purple");
      renderCode(ui.code, algo, -1);
      const base = { array: algo === "Linear Search" ? array.slice() : array.slice().sort((x, y) => x - y), comparing: [], lo: 0, hi: array.length - 1, found: -1, target };
      drawSearch(ui.canvas, base);
      ui.stats.innerHTML = "Comparisons: 0<br>Result: —";
      p.set(steps, applyStep);
    };
    rebuild("Linear Search");
    document.getElementById("algo").onchange = (e) => rebuild(e.target.value);
    document.getElementById("shuffle").onclick = () => {
      array = randArray(+document.getElementById("size").value || 16, 80);
      target = array[Math.floor(Math.random() * array.length)];
      document.getElementById("target").value = target;
      rebuild(currentAlgo);
    };
    document.getElementById("apply").onclick = () => {
      const n = Math.min(32, Math.max(5, +document.getElementById("size").value || 16));
      if (array.length !== n) array = randArray(n, 80);
      rebuild(currentAlgo);
    };
    document.getElementById("play").onclick = () => p.play();
    document.getElementById("pause").onclick = () => p.pause();
    document.getElementById("fwd").onclick = () => p.fwd();
    document.getElementById("back").onclick = () => {
      const t = p.back();
      ui.log.innerHTML = "";
      p.set(steps, applyStep);
      for (let k = 0; k < t; k++) p.fwd();
    };
    document.getElementById("reset").onclick = () => rebuild(currentAlgo);
    document.getElementById("speed").oninput = (e) => { speed = +e.target.value; };
    window.onresize = () => rebuild(currentAlgo);
    stopFn = () => { p.stop(); window.onresize = null; };
  }

  const ROWS = 15, COLS = 31, DIRS = [[-1, 0], [1, 0], [0, -1], [0, 1]];

  function pathSteps(algo, walls, weight, start, end) {
    const steps = [];
    const rec = (event, r, c, type, msg) => {
      steps.push({ type, r, c, line: (CODE_LINE[algo] || {})[event] ?? -1, msg });
    };
    const inb = (r, c) => r >= 0 && r < ROWS && c >= 0 && c < COLS && !walls[r][c];
    const neigh = (r, c) => DIRS.map(([dr, dc]) => [r + dr, c + dc]).filter(([nr, nc]) => inb(nr, nc));
    const h = (r, c) => Math.abs(r - end[0]) + Math.abs(c - end[1]);
    const parent = Array.from({ length: ROWS }, () => Array(COLS).fill(null));
    const reconstruct = () => {
      const path = [];
      let cur = end;
      while (cur && !(cur[0] === start[0] && cur[1] === start[1])) {
        path.push(cur);
        cur = parent[cur[0]][cur[1]];
        if (!cur) return;
      }
      path.reverse().forEach(([r, c]) => rec("path", r, c, "path", `Path (${r},${c})`));
      rec("done", end[0], end[1], "done", "Path found");
    };
    rec("start", start[0], start[1], "visit", `Start (${start[0]},${start[1]})`);
    if (algo === "BFS" || algo === "DFS") {
      const q = [start];
      const seen = Array.from({ length: ROWS }, () => Array(COLS).fill(false));
      seen[start[0]][start[1]] = true;
      while (q.length) {
        const [r, c] = algo === "DFS" ? q.pop() : q.shift();
        if (r === end[0] && c === end[1]) { reconstruct(); return steps; }
        for (const [nr, nc] of neigh(r, c)) {
          if (seen[nr][nc]) continue;
          seen[nr][nc] = true; parent[nr][nc] = [r, c]; q.push([nr, nc]);
          rec("visit", nr, nc, "visit", `Visit (${nr},${nc})`);
        }
      }
      rec("fail", -1, -1, "fail", "No path");
    } else {
      const dist = Array.from({ length: ROWS }, () => Array(COLS).fill(Infinity));
      dist[start[0]][start[1]] = 0;
      const pq = [{ f: 0, r: start[0], c: start[1] }];
      const closed = Array.from({ length: ROWS }, () => Array(COLS).fill(false));
      while (pq.length) {
        pq.sort((x, y) => x.f - y.f);
        const cur = pq.shift();
        if (closed[cur.r][cur.c]) continue;
        closed[cur.r][cur.c] = true;
        if (!(cur.r === start[0] && cur.c === start[1])) rec("visit", cur.r, cur.c, "visit", `Expand (${cur.r},${cur.c})`);
        if (cur.r === end[0] && cur.c === end[1]) { reconstruct(); return steps; }
        for (const [nr, nc] of neigh(cur.r, cur.c)) {
          const cost = algo === "BFS" ? 1 : Math.max(1, weight[nr][nc]);
          const g = algo === "Greedy Best-First" ? 0 : dist[cur.r][cur.c] + (algo === "Dijkstra" || algo === "A*" ? cost : 1);
          const f = algo === "Greedy Best-First" ? h(nr, nc) : algo === "A*" ? g + h(nr, nc) : g;
          if (algo !== "Greedy Best-First" && g >= dist[nr][nc]) continue;
          if (algo === "Greedy Best-First" && dist[nr][nc] !== Infinity) continue;
          dist[nr][nc] = algo === "Greedy Best-First" ? 1 : g;
          parent[nr][nc] = [cur.r, cur.c];
          pq.push({ f, r: nr, c: nc });
        }
      }
      rec("fail", -1, -1, "fail", "No path");
    }
    return steps;
  }

  function carveMaze(walls) {
    for (let r = 0; r < ROWS; r++) walls[r].fill(true);
    const carved = Array.from({ length: ROWS }, () => Array(COLS).fill(false));
    const walk = (r, c) => {
      carved[r][c] = true; walls[r][c] = false;
      const dirs = [[-2, 0], [2, 0], [0, -2], [0, 2]].sort(() => Math.random() - 0.5);
      for (const [dr, dc] of dirs) {
        const nr = r + dr, nc = c + dc;
        if (nr > 0 && nr < ROWS && nc > 0 && nc < COLS && !carved[nr][nc]) {
          walls[r + dr / 2][c + dc / 2] = false;
          walk(nr, nc);
        }
      }
    };
    walk(1, 1);
  }

  function showPath() {
    if (stopFn) stopFn();
    const ui = shell(`
      <select id="algo"><option>BFS</option><option>DFS</option><option>Dijkstra</option><option>A*</option><option>Greedy Best-First</option></select>
      <button id="maze">Maze</button>
      <button id="walls">Walls</button>
      <button id="weights">Weights</button>
      <button class="muted" id="clear">Clear</button>
      <button id="play">Play</button>
      <button class="muted" id="pause">Pause</button>
      <button class="muted" id="back">Back</button>
      <button class="muted" id="fwd">Forward</button>
      <button class="muted" id="reset">Reset</button>
      <label>Speed <input id="speed" type="range" min="1" max="10" value="7"></label>
    `);
    const walls = Array.from({ length: ROWS }, () => Array(COLS).fill(false));
    const weight = Array.from({ length: ROWS }, () => Array(COLS).fill(1));
    let start = [7, 2], end = [7, 28];
    let visited = Array.from({ length: ROWS }, () => Array(COLS).fill(false));
    let path = Array.from({ length: ROWS }, () => Array(COLS).fill(false));
    let cursor = null, steps = [], dirty = true, speed = 7;
    let drag = null, paint = null;
    const p = player(() => (11 - speed) * 18);
    const cellGeom = () => {
      const w = ui.canvas.clientWidth, h = ui.canvas.clientHeight;
      const size = Math.max(8, Math.min(w / COLS, h / ROWS));
      return { size, ox: (w - size * COLS) / 2, oy: (h - size * ROWS) / 2, w, h };
    };
    const draw = () => {
      const ctx = ui.canvas.getContext("2d");
      const { size, ox, oy, w, h } = cellGeom();
      ui.canvas.width = w; ui.canvas.height = h;
      ctx.fillStyle = "#0a0e1a"; ctx.fillRect(0, 0, w, h);
      for (let r = 0; r < ROWS; r++) for (let c = 0; c < COLS; c++) {
        const x = ox + c * size, y = oy + r * size;
        let color = "#0f172a";
        if (walls[r][c]) color = "#1e293b";
        else if (weight[r][c] > 1) color = lerpColor("#0f172a", "#475569", (weight[r][c] - 1) / 4);
        if (visited[r][c]) color = "#a78bfa88";
        if (path[r][c]) color = "#00ff88";
        if (r === start[0] && c === start[1]) color = "#00e5ff";
        if (r === end[0] && c === end[1]) color = "#ff4444";
        ctx.fillStyle = color;
        ctx.fillRect(x + 1, y + 1, size - 2, size - 2);
        if (!walls[r][c] && weight[r][c] > 1 && size >= 16 && !(r === start[0] && c === start[1]) && !(r === end[0] && c === end[1])) {
          ctx.fillStyle = "#e2e8f0"; ctx.font = "10px monospace";
          ctx.fillText(String(weight[r][c]), x + size / 2 - 3, y + size / 2 + 3);
        }
        if (cursor && cursor[0] === r && cursor[1] === c) {
          ctx.strokeStyle = "#ffd700"; ctx.strokeRect(x + 2, y + 2, size - 4, size - 4);
        }
      }
    };
    const hit = (ev) => {
      const rect = ui.canvas.getBoundingClientRect();
      const { size, ox, oy } = cellGeom();
      const c = Math.floor((ev.clientX - rect.left - ox) / size);
      const r = Math.floor((ev.clientY - rect.top - oy) / size);
      if (r < 0 || r >= ROWS || c < 0 || c >= COLS) return null;
      return [r, c];
    };
    const resetOverlay = () => {
      visited = Array.from({ length: ROWS }, () => Array(COLS).fill(false));
      path = Array.from({ length: ROWS }, () => Array(COLS).fill(false));
      cursor = null;
    };
    const ensure = () => {
      if (!dirty && steps.length) return;
      steps = pathSteps(currentAlgo, walls, weight, start, end);
      dirty = false; resetOverlay();
      p.set(steps, applyStep);
    };
    let vis = 0, plen = 0;
    const applyStep = (st) => {
      if (st.type === "visit" && st.r >= 0) { visited[st.r][st.c] = true; cursor = [st.r, st.c]; vis++; }
      else if (st.type === "path") { path[st.r][st.c] = true; cursor = [st.r, st.c]; plen++; }
      else cursor = null;
      draw();
      renderCode(ui.code, currentAlgo, st.line);
      ui.stats.innerHTML = `Visited: ${vis}<br>Path: ${plen}`;
      appendLog(ui.log, st.msg, st.type === "path" || st.type === "done" ? "green" : st.type === "fail" ? "red" : "purple");
    };
    const markDirty = () => { dirty = true; steps = []; vis = 0; plen = 0; resetOverlay(); draw(); ui.stats.innerHTML = "Visited: 0<br>Path: 0"; };
    ui.canvas.oncontextmenu = (e) => e.preventDefault();
    ui.canvas.onmousedown = (e) => {
      const cell = hit(e); if (!cell) return;
      if (cell[0] === start[0] && cell[1] === start[1]) { drag = "s"; return; }
      if (cell[0] === end[0] && cell[1] === end[1]) { drag = "e"; return; }
      paint = e.button !== 2 && !walls[cell[0]][cell[1]];
      if (cell[0] === start[0] && cell[1] === start[1]) return;
      walls[cell[0]][cell[1]] = paint; markDirty();
    };
    ui.canvas.onmousemove = (e) => {
      const cell = hit(e); if (!cell) return;
      if (drag === "s" && !(cell[0] === end[0] && cell[1] === end[1])) { walls[cell[0]][cell[1]] = false; start = cell; markDirty(); }
      else if (drag === "e" && !(cell[0] === start[0] && cell[1] === start[1])) { walls[cell[0]][cell[1]] = false; end = cell; markDirty(); }
      else if (paint !== null && !(cell[0] === start[0] && cell[1] === start[1]) && !(cell[0] === end[0] && cell[1] === end[1])) {
        walls[cell[0]][cell[1]] = paint; markDirty();
      }
    };
    window.onmouseup = () => { drag = null; paint = null; };
    carveMaze(walls); start = [1, 1]; end = [ROWS - 2, COLS - 2]; walls[start[0]][start[1]] = false; walls[end[0]][end[1]] = false;
    setAlgo("BFS"); renderCode(ui.code, "BFS", -1); draw();
    appendLog(ui.log, "Draw walls, then Play. Dijkstra/A* use weights; BFS ignores them.", "cyan");
    document.getElementById("algo").onchange = (e) => { setAlgo(e.target.value); renderCode(ui.code, currentAlgo, -1); markDirty(); };
    document.getElementById("maze").onclick = () => { carveMaze(walls); start = [1, 1]; end = [ROWS - 2, COLS - 2]; weight.forEach((row) => row.fill(1)); markDirty(); };
    document.getElementById("walls").onclick = () => {
      walls.forEach((row, r) => row.forEach((_, c) => { walls[r][c] = Math.random() < 0.25 && !(r === start[0] && c === start[1]) && !(r === end[0] && c === end[1]); }));
      markDirty();
    };
    document.getElementById("weights").onclick = () => {
      for (let r = 0; r < ROWS; r++) for (let c = 0; c < COLS; c++)
        weight[r][c] = walls[r][c] || (r === start[0] && c === start[1]) || (r === end[0] && c === end[1]) ? 1 : 1 + Math.floor(Math.random() * 5);
      markDirty();
    };
    document.getElementById("clear").onclick = () => { walls.forEach((row) => row.fill(false)); weight.forEach((row) => row.fill(1)); start = [7, 2]; end = [7, 28]; markDirty(); };
    document.getElementById("play").onclick = () => { vis = 0; plen = 0; ensure(); p.play(); };
    document.getElementById("pause").onclick = () => p.pause();
    document.getElementById("fwd").onclick = () => { ensure(); p.fwd(); };
    document.getElementById("back").onclick = () => {
      const t = p.back(); vis = 0; plen = 0; resetOverlay(); ui.log.innerHTML = "";
      p.set(steps, applyStep); for (let k = 0; k < t; k++) p.fwd();
    };
    document.getElementById("reset").onclick = () => { p.reset(); vis = 0; plen = 0; resetOverlay(); draw(); ui.log.innerHTML = ""; appendLog(ui.log, "Reset.", "purple"); };
    document.getElementById("speed").oninput = (e) => { speed = +e.target.value; };
    window.onresize = draw;
    stopFn = () => { p.stop(); window.onresize = null; window.onmouseup = null; };
  }

  const routes = { sort: showSort, search: showSearch, path: showPath };
  window.addEventListener("hashchange", () => {
    const key = location.hash.replace("#", "");
    (routes[key] || showHome)();
  });
  const initial = location.hash.replace("#", "");
  (routes[initial] || showHome)();
})();
