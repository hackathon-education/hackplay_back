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
  });
}

/* ============================================================
    파일 열기 API
============================================================ */
async function openFile(path) {
  const res = await fetch(`/api/v1/projects/${projectId}/files?path=${encodeURIComponent(path)}`, {
    headers: { "Authorization": localStorage.getItem("token") }
  });

  const json = await res.json();
  if (json.code !== 200) {
    alert("파일을 열 수 없습니다.");
    return;
  }

  const content = json.data.content || "";
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
}

/* ============================================================
    파일 저장 API
============================================================ */
async function saveFile() {
  if (!currentPath) return alert("파일을 먼저 선택하세요.");

  await fetch(`/api/v1/projects/${projectId}/files`, {
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

  alert("파일 저장 완료");
}
