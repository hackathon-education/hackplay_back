(function () {

  const FitAddon = window.FitAddon?.FitAddon;

  let terminals = {};
  let terminalCounter = 0;
  let activeTerminalId = null;

  window.HackPlayTerminal = {
    createTerminal: () => createTerminalInternal(false),
    openRunTerminal: () => createTerminalInternal(true),
    activateTerminal,
    closeTerminal,

    // 여기에 terminals 추가 ⭐
    terminals: terminals,
    splitTerminal,
    clearActiveTerminal
  };

  window.createTerminalInternal = (isRunTerminal) => createTerminalInternal(isRunTerminal);
  window.splitTerminal = splitTerminal;
  window.clearActiveTerminal = clearActiveTerminal;

  /* ================================
      CREATE TERMINAL
  ================================= */
  function createTerminalInternal(isRunTerminal) {
    terminalCounter++;
    const id = (isRunTerminal ? "run-" : "term-") + terminalCounter;

    const tab = createTab(id, isRunTerminal);
    const pane = createPane(id);

    document.getElementById("terminal-tabs").appendChild(tab);
    document.getElementById("terminal-container").appendChild(pane);

    const termConfig = {
      cursorBlink: true,
      fontSize: 14,
      fontFamily: "Cascadia Code, monospace",
      scrollback: 5000,
      theme: {
        background: "#0d1117",
        foreground: "#d1d5da"
      }
    };

    const term = new Terminal(termConfig);
    const fitAddon = new FitAddon();
    term.loadAddon(fitAddon);

    term.open(document.getElementById(`${id}-view`));
    setTimeout(() => fitAddon.fit(), 20);

    let ws = isRunTerminal
      ? connectRunSocket(term, id)
      : connectPtySocket(term, id);

    terminals[id] = {
      id,
      term,
      ws,
      fitAddon,
      pane,
      tab,
      isRunTerminal
    };

    activateTerminal(id);

    window.addEventListener("resize", () => fitAddon.fit());

    return id;
  }

  /* ================================
      PTY TERMINAL SOCKET
  ================================= */
  function connectPtySocket(term, id) {
    const ws = new WebSocket(`ws://${location.host}/ws/terminal`);

    ws.onopen = () => {
      term.writeln("\x1b[32m[PTY connected]\x1b[0m");
    };

    ws.onmessage = e => term.write(e.data);

    ws.onclose = () => {
      term.writeln("\n\x1b[31m[PTY disconnected]\x1b[0m");
    };

    term.onData(data => {
      if (ws.readyState === WebSocket.OPEN) ws.send(data);
    });

    return ws;
  }

  /* ================================
      RUN TERMINAL SOCKET
  ================================= */
function connectRunSocket(term, id) {
  if (!window.projectId) {
    term.writeln("\x1b[31m[Error: projectId undefined]\x1b[0m");
    return null;
  }

  const ws = new WebSocket(`ws://${location.host}/ws/run?projectId=${window.projectId}`);

  ws.onopen = () => {
    term.writeln("\x1b[36m[Run Terminal Connected]\x1b[0m");
    term.writeln("[Waiting for logs...]\n");
  };

  // 강력한 sanitize
  function sanitizeTerminalOutput(data) {
    return data
      .replace(/\u0000/g, "")
      .replace(/\x1b\[[0-9;]*[A-Za-z]/g, "")
      .replace(/\r\n/g, "\n")
      .replace(/\r/g, "\n")
      .replace(/\t/g, "  ");
  }

  let buffer = "";

  ws.onmessage = e => {
    const cleaned = sanitizeTerminalOutput(e.data);
    buffer += cleaned;

    let lines = buffer.split("\n");
    buffer = lines.pop();

    lines.forEach(line => {
      term.writeln(line);
      console.log("📦 [RunTerminal line]:", JSON.stringify(line));
    });
  };

  ws.onclose = () => {
    term.writeln("\n\x1b[33m[Run Terminal Closed]\x1b[0m");
  };

  term.onData(data => {
    if (data === "\u0003") {
      if (ws.readyState === WebSocket.OPEN) {
        ws.send("STOP");
        term.writeln("\n[Stopping project...]\n");
      }
    }
  });

  return ws;
}

function splitTerminal() {
  return createTerminalInternal(false);
}

function clearActiveTerminal() {
  if (!activeTerminalId) return;
  const t = terminals[activeTerminalId];
  if (t && t.term) {
    t.term.clear();
    t.term.writeln("\x1b[32m[Terminal cleared]\x1b[0m");
  }
}

  /* ================================
      UI - TAB & PANE
  ================================= */
  function createTab(id, isRun) {
    const div = document.createElement("div");
    div.className = "terminal-tab";
    div.id = id + "-tab";
    div.innerHTML = isRun ? "🚀 Run" : "📟 Terminal";

    div.onclick = () => activateTerminal(id);
    return div;
  }

  function createPane(id) {
    const div = document.createElement("div");
    div.className = "terminal-pane";
    div.id = id + "-pane";

    div.innerHTML = `
      <div class="terminal-header">
        <button onclick="HackPlayTerminal.closeTerminal('${id}')">✕</button>
      </div>
      <div class="terminal-view" id="${id}-view"></div>
    `;

    return div;
  }

  /* ================================
      UI - ACTIVATE & CLOSE
  ================================= */
  function activateTerminal(id) {
    document.querySelectorAll(".terminal-pane").forEach(p => p.style.display = "none");
    document.querySelectorAll(".terminal-tab").forEach(t => t.classList.remove("active"));

    terminals[id].pane.style.display = "flex";
    terminals[id].tab.classList.add("active");

    terminals[id].fitAddon.fit();
    terminals[id].term.focus();

    activeTerminalId = id;
  }

  function closeTerminal(id) {
    const t = terminals[id];
    if (!t) return;

    if (t.ws) t.ws.close();
    t.term.dispose();
    t.pane.remove();
    t.tab.remove();

    delete terminals[id];

    const keys = Object.keys(terminals);
    if (keys.length > 0) activateTerminal(keys[0]);
  }

  window.stopProject = function () {
    console.log("🛑 stopProject() called");

    for (const id in terminals) {
      const t = terminals[id];

      // RUN terminal만 종료
      if (t.isRunTerminal && t.ws && t.ws.readyState === WebSocket.OPEN) {
        console.log("🛑 STOP signal sent to", id);
        t.ws.send("STOP");
        t.term.writeln("\n\x1b[33m[Stopping project...]\x1b[0m\n");
      }
    }
  };

})();