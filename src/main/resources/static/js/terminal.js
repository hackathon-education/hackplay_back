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

    /* ---------- DOM ---------- */
    const pane = document.createElement("div");
    pane.className = "terminal-pane";
    pane.innerHTML = `<div class="terminal-view" id="${id}"></div>`;
    container.appendChild(pane);

    /* ---------- XTERM ---------- */
    const term = new Terminal({
      cursorBlink: true,
      fontSize: 14,
      fontFamily: "Cascadia Code, monospace",
      scrollback: 3000,
      theme: {
        background: "#0d1117",
        foreground: "#d1d5da"
      },
      convertEol: true // 줄바꿈 처리 개선
    });

    const fitAddon = new FitAddon();
    term.loadAddon(fitAddon);
    term.open(document.getElementById(id));

    requestAnimationFrame(() => {
      fitAddon.fit();
      term.focus();
    });

    /* ---------- WebSocket ---------- */
    const protocol = location.protocol === "https:" ? "wss" : "ws";
    const projectId = window.projectId;

    if (!projectId) {
      term.writeln("❌ projectId missing");
      return;
    }

    const url = isRun
      ? `${protocol}://${location.host}/ws/run?projectId=${projectId}`
      : `${protocol}://${location.host}/ws/terminal?projectId=${projectId}`;

    console.log("🔌 Connecting to:", url);
    const ws = new WebSocket(url);

    ws.onopen = () => {
      reconnectAttempts = 0; // 연결 성공시 재시도 횟수 리셋
      term.writeln(
        isRun
          ? "\x1b[36m[Run Terminal Connected]\x1b[0m"
          : "\x1b[32m[Terminal Connected]\x1b[0m"
      );
      console.log("✅ WebSocket connected:", url);
    };

    ws.onmessage = e => {
      if (typeof e.data === "string") {
        term.write(e.data);
      }
    };

    ws.onerror = (err) => {
      console.error("❌ WebSocket Error:", err);
      term.writeln("\n\x1b[31m[WebSocket Error]\x1b[0m");
    };

    ws.onclose = (event) => {
      console.warn("🔌 WebSocket closed:", event.code, event.reason);
      term.writeln("\n\x1b[33m[Terminal Closed]\x1b[0m");
      
      // 정상적인 종료가 아닌 경우에만 재연결 시도
      if (event.code !== 1000 && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        reconnectAttempts++;
        term.writeln(`\x1b[36m[Reconnecting... (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})]\x1b[0m`);
        
        setTimeout(() => {
          console.log("🔄 Attempting reconnect...");
          // 기존 pane 제거 후 새로 생성
          pane.remove();
          createTerminal(isRun);
        }, 3000);
      } else if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
        term.writeln("\x1b[31m[Max reconnect attempts reached. Please refresh the page.]\x1b[0m");
      }
    };

    /* ---------- INPUT ---------- */
    term.onData(data => {
      if (ws.readyState === WebSocket.OPEN) {
        if (isRun && data === "\u0003") {
          ws.send("STOP");
          term.writeln("\n[Stopping project...]\n");
        } else {
          ws.send(data);
        }
      } else {
        console.warn("⚠️ WebSocket not open, data not sent:", data);
      }
    });

    window.addEventListener("resize", () => {
      try {
        fitAddon.fit();
      } catch (err) {
        console.warn("⚠️ Terminal fit error:", err);
      }
    });

    // 터미널 객체 반환 (디버그용)
    return { term, ws, id };
  }
})();
