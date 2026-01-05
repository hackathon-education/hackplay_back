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
      cursorBlink: false,
      rendererType: 'canvas',
      fontSize: 13,
      lineHeight: 1.1,
      fontFamily: "Cascadia Code, monospace",
      scrollback: 2000,
      convertEol: false,
      disableStdin: false,
      smoothScrollDuration: 0,
      theme: {
        background: "#0d1117",
        foreground: "#d1d5da"
      }
    });

    const fitAddon = new FitAddon();
    term.loadAddon(fitAddon);
    term.open(document.getElementById(id));

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

    // ✅ projectType 제거
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

    setTimeout(() => {
      fitAddon.fit();
      sendResize();
      term.focus();
    }, 0);

    let resizeTimer = null;

    window.addEventListener("resize", () => {
      clearTimeout(resizeTimer);
      resizeTimer = setTimeout(() => {
        fitAddon.fit();
        sendResize();
      }, 200);
    });

    /* ================= INPUT CONTROL ================= */
    let inputEnabled = true;

    let inBuf = "";
    let inputTimer = null;

    term.onData(data => {
      inBuf += data;

      if (!inputTimer) {
        inputTimer = setTimeout(() => {
          ws.send(inBuf);
          inBuf = "";
          inputTimer = null;
        }, 8); // 5~10ms
      }
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

    let outBuf = "";
    let flushScheduled = false;

    ws.onmessage = e => {
      if (typeof e.data !== "string") return;

      outBuf += e.data;

      if (!flushScheduled) {
        flushScheduled = true;
        requestAnimationFrame(() => {
          term.write(outBuf);
          outBuf = "";
          flushScheduled = false;
        });
      }
    };

    ws.onerror = () => {
      term.writeln("\n\x1b[31m[WebSocket Error]\x1b[0m");
    };

    ws.onclose = event => {
      inputEnabled = false;
      term.write("\r\n\x1b[33m[Terminal Closed]\x1b[0m\r\n");

      if (event.code !== 1000 && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        reconnectAttempts++;
        term.write(
          `\x1b[36m[Reconnecting... ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS}]\x1b[0m\r\n`
        );

        setTimeout(() => {
          pane.remove();
          createTerminal(isRun);
        }, 3000);
      }
    };

    return { term, ws, id };
  }
})();
