/* ============================================================
    HackPlay IDE - Monaco Editor Core
============================================================ */

let editor;
let currentPath = null;
let currentType = null;

/* ============================================================
    Monaco Editor 초기화
============================================================ */
function initMonaco() {
    require.config({
    paths: {
        "vs": "https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.45.0/min/vs"
    }
    });

    window.MonacoEnvironment = {
    getWorkerUrl: function (moduleId, label) {
        return `data:text/javascript;charset=utf-8,${encodeURIComponent(`
        self.MonacoEnvironment = {
            baseUrl: 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.45.0/min/'
        };
        importScripts('https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.45.0/min/vs/base/worker/workerMain.js');
        `)}`;
    }
    };

  require(['vs/editor/editor.main'], () => {
    editor = monaco.editor.create(document.getElementById('editor-container'), {
      value: "",
      theme: "vs-dark",
      language: "plaintext",
      automaticLayout: true,
      fontSize: 14,
      minimap: { enabled: true }
    });
    
    console.log("✅ Monaco Editor initialized");
  });
}

/* ============================================================
    파일 열기 API
============================================================ */
async function openFile(path) {
  console.log("📖 Opening file:", path);
  
  try {
    const res = await fetch(`/api/v1/projects/${window.projectId}/files?path=${encodeURIComponent(path)}`, {
      headers: { "Authorization": localStorage.getItem("token") }
    });

    const json = await res.json();
    if (json.code !== 200) {
      console.error("❌ Failed to open file:", json);
      alert("파일을 열 수 없습니다.");
      return;
    }

    const content = json.data.content || "";
    
    if (!editor) {
      console.warn("⚠️ Monaco editor not ready yet");
      setTimeout(() => openFile(path), 500);
      return;
    }
    
    editor.setValue(content);
    document.getElementById("editor-path").textContent = path;
    currentPath = path;

    /* -------------------------------
        파일 확장자로 언어 자동 설정
    --------------------------------*/
    const ext = path.split(".").pop().toLowerCase();

    const languageMap = {
      js: "javascript",
      ts: "typescript",
      jsx: "javascript",
      tsx: "typescript",
      json: "json",
      html: "html",
      css: "css",
      scss: "scss",
      md: "markdown",
      yml: "yaml",
      yaml: "yaml",
      xml: "xml",
      java: "java",
      py: "python",
      c: "c",
      cpp: "cpp",
      sql: "sql",
      sh: "shell",
    };

    const lang = languageMap[ext] || "plaintext";

    // Monaco에 언어 적용
    monaco.editor.setModelLanguage(editor.getModel(), lang);
    
    console.log("✅ File opened:", path, "Language:", lang);
  } catch (err) {
    console.error("❌ Error opening file:", err);
    alert("파일 열기 중 오류가 발생했습니다.");
  }
}

/* ============================================================
    파일 저장 API
============================================================ */
async function saveFile() {
  if (!currentPath) return alert("파일을 먼저 선택하세요.");

  try {
    await fetch(`/api/v1/projects/${window.projectId}/files`, {
      method: "PATCH",
      headers: {
        "Authorization": localStorage.getItem("token"),
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        path: currentPath,
        content: editor.getValue()
      })
    });

    console.log("✅ File saved:", currentPath);
    alert("파일 저장 완료");
  } catch (err) {
    console.error("❌ Error saving file:", err);
    alert("파일 저장 중 오류가 발생했습니다.");
  }
}

/* ============================================================
    전역 함수 노출 - 중요!
============================================================ */
window.EditorCore = {
  openFile: openFile,
  saveFile: saveFile,
  getCurrentPath: () => currentPath,
  getEditor: () => editor
};

// 직접 전역 함수로도 노출
window.openFile = openFile;
window.saveFile = saveFile;
window.initMonaco = initMonaco;
