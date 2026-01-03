(function () {
  const FitAddon = window.FitAddon?.FitAddon;
  if (!FitAddon) {
    console.error("❌ FitAddon not loaded");
    return;
  }

  let counter = 0;
  let reconnectAttempts = 0;
  const MAX_RECONNECT_ATTEMPTS = 3;

  window.HackPlayTerminal = {
    createTerminal: () => createTerminal(false),
    openRunTerminal: () => createTerminal(true)
  };

  function createTerminal(isRun) {
    counter++;
    const id = (isRun ? "run-" : "term-") + counter;

    const container = document.getElementById("terminal-container");
    if (!container) {
      console.error("❌ terminal-container not found");
      return;
    }

    /* ================= DOM ================= */
    const pane = document.createElement("div");
    pane.className = "terminal-pane";
    pane.innerHTML = `<div class="terminal-view" id="${id}"></div>`;
    container.appendChild(pane);

    /* ================= XTERM ================= */
    const term = new Terminal({
      cursorBlink: true,
      fontSize: 14,
      fontFamily: "Cascadia Code, monospace",
      scrollback: 3000,
      convertEol: false, // 🔴 PTY에서는 반드시 false
      theme: {
        background: "#0d1117",
        foreground: "#d1d5da"
      }
    });

    const fitAddon = new FitAddon();
    term.loadAddon(fitAddon);
    term.open(document.getElementById(id));

    // 브라우저 단축키 충돌 방지
    term.attachCustomKeyEventHandler(e => {
      if (e.ctrlKey && ["w", "r"].includes(e.key.toLowerCase())) return false;
      return true;
    });

    /* ================= WebSocket ================= */
    const protocol = location.protocol === "https:" ? "wss" : "ws";
    const projectId = window.projectId;

    if (!projectId) {
      term.writeln("❌ projectId missing");
      return;
    }

    const url = isRun
      ? `${protocol}://${location.host}/ws/run?projectId=${projectId}`
      : `${protocol}://${location.host}/ws/terminal?projectId=${projectId}`;

    const ws = new WebSocket(url);

    /* ================= Resize ================= */
    function sendResize() {
      if (ws.readyState !== WebSocket.OPEN) return;
      ws.send(JSON.stringify({
        type: "resize",
        cols: term.cols,
        rows: term.rows
      }));
    }

    requestAnimationFrame(() => {
      fitAddon.fit();
      sendResize();
      term.focus();
    });

    window.addEventListener("resize", () => {
      fitAddon.fit();
      sendResize();
    });

    /* ================= WS Events ================= */
    ws.onopen = () => {
      reconnectAttempts = 0;
      term.writeln(
        isRun
          ? "\x1b[36m[Run Terminal Connected]\x1b[0m"
          : "\x1b[32m[Terminal Connected]\x1b[0m"
      );
    };

    ws.onmessage = e => {
      if (typeof e.data === "string") {
        term.write(e.data);
      }
    };

    ws.onerror = () => {
      term.writeln("\n\x1b[31m[WebSocket Error]\x1b[0m");
    };

    ws.onclose = event => {
      term.writeln("\n\x1b[33m[Terminal Closed]\x1b[0m");
      term.setOption("disableStdin", true);

      if (event.code !== 1000 && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        reconnectAttempts++;
        term.writeln(
          `\x1b[36m[Reconnecting... ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS}]\x1b[0m`
        );
        setTimeout(() => {
          pane.remove();
          createTerminal(isRun);
        }, 3000);
      }
    };

    /* ================= INPUT ================= */
    term.onData(data => {
      if (ws.readyState !== WebSocket.OPEN) return;

      if (isRun && data === "\u0003") {
        ws.send("STOP");
        term.writeln("\n[Stopping project...]\n");
        return;
      }

      ws.send(data);
    });

    return { term, ws, id };
  }
})();
