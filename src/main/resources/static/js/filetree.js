/* ============================================================
   HackPlay filetree.js (fixed version)
   - Tree load + proper folder expansion
============================================================ */

const treeEl = document.getElementById("file-tree");

document.addEventListener("DOMContentLoaded", () => {
  refreshTree();
});

async function refreshTree() {
  const projectId = window.projectId;
  if (!projectId) {
    console.error("❌ projectId missing");
    return;
  }

  try {
    const res = await fetch(`/api/v1/projects/${projectId}/dirs/tree`, {
      headers: { Authorization: localStorage.getItem("token") }
    });
    
    const json = await res.json();
    if (json.code !== 200) {
      console.error("❌ API returned error code:", json.code);
      return;
    }
    
    document.getElementById("file-tree").innerHTML = buildTree(json.data, true);
    bindTreeEvents();
    console.log("✅ Tree refreshed and events bound");
  } catch (err) {
    console.error("❌ tree load failed:", err);
    treeEl.innerHTML = `<div style="padding:8px;color:#ff6b6b;">트리 로드 실패</div>`;
  }
}

function buildTree(node, isRoot = false) {
  if (node.type === "DIRECTORY") {
    // 루트는 expanded, 나머지는 기본적으로 접힘
    const expandedClass = isRoot ? "expanded" : "";
    
    return `
      <div class="folder ${expandedClass}" data-path="${node.path}">
        <div class="dir-item" data-path="${node.path}">
          📁 ${node.name || "workspace"}
        </div>
        <div class="folder-children">
          ${(node.children || []).map(c => buildTree(c)).join("")}
        </div>
      </div>
    `;
  }
  
  return `
    <div class="file-item" data-path="${node.path}">
      📄 ${node.name}
    </div>
  `;
}

function bindTreeEvents() {
  // 파일 클릭 이벤트
  document.querySelectorAll(".file-item").forEach(el => {
    el.onclick = (e) => {
      e.stopPropagation();
      console.log("📄 File clicked:", el.dataset.path);
      openFile(el.dataset.path);
    };
  });
  
  // 폴더 클릭 이벤트 (접힘/펼침)
  document.querySelectorAll(".dir-item").forEach(el => {
    el.onclick = (e) => {
      e.stopPropagation();
      const folder = el.closest('.folder');
      if (folder) {
        const wasExpanded = folder.classList.contains('expanded');
        folder.classList.toggle('expanded');
        console.log(`📁 Folder ${wasExpanded ? 'collapsed' : 'expanded'}:`, el.dataset.path);
      }
    };
  });
  
  console.log(`🔗 Events bound: ${document.querySelectorAll(".file-item").length} files, ${document.querySelectorAll(".dir-item").length} folders`);
}

function openFile(path) {
  const projectId = window.projectId;

  console.log("📖 Opening file:", path);

  fetch(`/api/v1/projects/${projectId}/files?path=${encodeURIComponent(path)}`, {
    method: "GET",
    headers: {
      'Authorization': localStorage.getItem("token")
    }
  })
    .then((res) => {
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return res.text();
    })
    .then((content) => {
      // editor-core.js 쪽에서 제공하는 함수 사용
      if (window.EditorCore && typeof window.EditorCore.openFile === "function") {
        window.EditorCore.openFile(path, content);
      } else if (typeof openFileInEditor === "function") {
        openFileInEditor(path, content);
      } else {
        console.warn("⚠️ No editor function found. Available functions:", Object.keys(window).filter(k => k.includes('editor') || k.includes('Editor')));
      }
    })
    .catch((err) => {
      console.error("❌ openFile failed:", err);
    });
}

window.addEventListener("click", () => {
  const menu = document.getElementById("menu");
  if (!menu) return;
  menu.style.display = "none";
});

// 디버그용 전역 함수들
window.refreshTree = refreshTree;
window.bindTreeEvents = bindTreeEvents;
