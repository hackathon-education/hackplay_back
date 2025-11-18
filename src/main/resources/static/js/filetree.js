/* ============================================================
    HackPlay IDE - FILE TREE MODULE (완전한 VSCode 스타일)
============================================================ */

/* ============================================================
    디렉토리 - 트리 로드 API
============================================================ */
async function refreshTree() {
    try {
        const res = await fetch(`/api/v1/projects/${projectId}/dirs/tree`, {
            headers: {
                "Authorization": localStorage.getItem("token")
            }
        });

        const json = await res.json();

        if (res.ok && json.code === 200) {
            const treeHtml = buildTree(json.data, true); // root-only flag
            document
                .getElementById("file-tree")
                .innerHTML = treeHtml;

            bindTreeEvents();
            expandRoot();
        }
    } catch (e) {
        console.error(e);
    }
}

/* ============================================================
    루트 폴더만 자동 펼침
============================================================ */
function expandRoot() {
    const root = document.querySelector(".root-folder");
    if (root) 
        root
            .classList
            .add("expanded");
    }

/* ============================================================
    트리 구조 생성 (file-icons-js 아이콘 적용)
============================================================ */
function buildTree(node, isRoot = false) {
    // 루트 이름 강제 지정 (빈 문자열로 오지 않도록 처리)
    const displayName = isRoot
        ? (
            node.name && node.name.length > 0
                ? node.name
                : "workspace"
        )
        : node.name;

    if (node.type === "DIRECTORY") {
        return `
      <div class="folder ${isRoot
            ? "root-folder"
            : ""}">
        <div class="item dir-item"
             data-path="${node
                .path}"
             oncontextmenu="showMenu(event,'DIR','${node
                .path}')">

          <i class="bi bi-folder"></i>
          <span class="item-name">${displayName}</span>
        </div>

        <div class="folder-children">
          ${ (
                    node.children || []
                )
                .map(child => buildTree(child, false))
                .join("")}
        </div>
      </div>
    `;
    }

    // 파일 아이콘 자동 매칭
    const ext = node
        .name
        .includes(".")
            ? node
                .name
                .split(".")
                .pop()
            : "default";

    // file-icons-js 에 있는 아이콘 확장자들
    const knownIcons = [
        "js",
        "ts",
        "json",
        "html",
        "css",
        "md",
        "java",
        "py",
        "xml",
        "yml",
        "yaml",
        "png",
        "jpg",
        "jpeg",
        "svg",
        "txt"
    ];

    const iconClass = knownIcons.includes(ext)
        ? `icon icon-${ext}`
        : "icon icon-file-empty";

    return `
    <div class="item file-item"
         data-path="${node.path}"
         oncontextmenu="showMenu(event,'FILE','${node.path}')">
      
      <i class="bi bi-file-earmark"></i>
      <span class="item-name">${node.name}</span>
    </div>
  `;
}

/* ============================================================
    이벤트 바인딩 (클릭 + 드래그)
============================================================ */
function bindTreeEvents() {

    /* ---------- 기본 우클릭 메뉴 막기 ---------- */
    document
        .querySelectorAll(".dir-item, .file-item")
        .forEach(el => {
            el.addEventListener("contextmenu", e => {
                e.preventDefault();
                e.stopPropagation();
            });
        });

    // 폴더 클릭 → 접기/펼치기
    document
        .querySelectorAll(".dir-item")
        .forEach(dir => {
            dir.addEventListener("click", e => {
                if (!e.target.closest(".more-btn")) {
                    dir
                        .parentElement
                        .classList
                        .toggle("expanded");
                }
            });
        });

    // 파일 열기
    document
        .querySelectorAll(".file-item")
        .forEach(item => {
            item.addEventListener("click", () => {
                currentPath = item.dataset.path;
                currentType = "FILE";
                openFile(currentPath);
            });
        });

    // 드래그 설정
    document
        .querySelectorAll(".dir-item, .file-item")
        .forEach(el => {
            el.draggable = true;

            el.addEventListener("dragstart", e => {
                e
                    .dataTransfer
                    .setData("path", el.dataset.path);
                e
                    .dataTransfer
                    .setData(
                        "type",
                        el.classList.contains("dir-item")
                            ? "DIR"
                            : "FILE"
                    );
            });
        });

    // 폴더에 드랍하여 이동시키기
    document
        .querySelectorAll(".dir-item")
        .forEach(dir => {
            dir.addEventListener("dragover", e => {
                e.preventDefault();
                dir
                    .classList
                    .add("drag-over");
            });

            dir.addEventListener("dragleave", () => {
                dir
                    .classList
                    .remove("drag-over");
            });

            dir.addEventListener("drop", async e => {
                e.preventDefault();
                dir
                    .classList
                    .remove("drag-over");

                const srcPath = e
                    .dataTransfer
                    .getData("path");
                const srcType = e
                    .dataTransfer
                    .getData("type");
                const destPath = dir.dataset.path;

                if (srcPath === destPath) 
                    return;
                
                await moveItem(srcPath, destPath, srcType);
                refreshTree();
            });
        });
}

/* ============================================================
    컨텍스트 메뉴
============================================================ */
function showMenu(event, type, path) {
    event.stopPropagation();
    currentPath = path;
    currentType = type;

    const menu = document.getElementById("menu");

    menu.innerHTML = (type === "DIR")
        ? `
        <button onclick="createFile()">파일 생성</button>
        <button onclick="createFolder()">폴더 생성</button>
        <button onclick="renameItem()">이름 변경</button>
        <button onclick="moveDir()">폴더 이동</button>
        <button onclick="deleteItem()">삭제</button>
      `
        : `
        <button onclick="openFile('${path}')">열기</button>
        <button onclick="renameItem()">이름 변경</button>
        <button onclick="moveFile()">파일 이동</button>
        <button onclick="deleteItem()">삭제</button>
      `;

    const menuWidth = 160;
    const x = Math.min(event.pageX, window.innerWidth - menuWidth - 4);
    const y = event.pageY;

    menu.style.display = "flex";
    menu.style.left = x + "px";
    menu.style.top = y + "px";
}

window.addEventListener("click", () => {
    document
        .getElementById("menu")
        .style
        .display = "none";
});

/* ============================================================
    부모 경로
============================================================ */
function getParentPath() {
    if (!currentPath) 
        return "";
    if (currentType === "DIR") 
        return currentPath;
    return currentPath.substring(0, currentPath.lastIndexOf("/"));
}

/* ============================================================
    CRUD - 파일/폴더 생성, 이름변경, 삭제, 이동
============================================================ */
async function createFile() {
    const name = prompt("파일 이름?");
    if (!name) 
        return;
    
    await fetch(`/api/v1/projects/${projectId}/files`, {
        method: "POST",
        headers: {
            "Authorization": localStorage.getItem("token"),
            "Content-Type": "application/json"
        },
        body: JSON.stringify({name, parentPath: getParentPath(), content: ""})
    });

    refreshTree();
}

async function createFolder() {
    const name = prompt("폴더 이름?");
    if (!name) 
        return;
    
    await fetch(`/api/v1/projects/${projectId}/dirs`, {
        method: "POST",
        headers: {
            "Authorization": localStorage.getItem("token"),
            "Content-Type": "application/json"
        },
        body: JSON.stringify({name, parentPath: getParentPath()})
    });

    refreshTree();
}

async function renameItem() {
    const newName = prompt("새 이름?");
    if (!newName) 
        return;
    
    const endpoint = currentType === "DIR"
        ? `/api/v1/projects/${projectId}/dirs/rename`
        : `/api/v1/projects/${projectId}/files/rename`;

    await fetch(endpoint, {
        method: "PATCH",
        headers: {
            "Authorization": localStorage.getItem("token"),
            "Content-Type": "application/json"
        },
        body: JSON.stringify({currentPath, newName})
    });

    refreshTree();
}

async function deleteItem() {
    if (!confirm("정말 삭제할까요?")) 
        return;
    
    const api = currentType === "FILE"
        ? "files"
        : "dirs";

    await fetch(
        `/api/v1/projects/${projectId}/${api}?path=${encodeURIComponent(currentPath)}`,
        {
            method: "DELETE",
            headers: {
                "Authorization": localStorage.getItem("token")
            }
        }
    );

    refreshTree();
}

async function moveFile() {
    const newParentDir = prompt("옮길 폴더 경로?");
    if (!newParentDir) 
        return;
    
    await moveItem(currentPath, newParentDir, "FILE");
    refreshTree();
}

async function moveDir() {
    const newParentDir = prompt("옮길 폴더 경로?");
    if (!newParentDir) 
        return;
    
    await moveItem(currentPath, newParentDir, "DIR");
    refreshTree();
}

async function moveItem(srcPath, destDir, type) {
    const endpoint = type === "DIR"
        ? `/api/v1/projects/${projectId}/dirs/move`
        : `/api/v1/projects/${projectId}/files/move`;

    await fetch(endpoint, {
        method: "PATCH",
        headers: {
            "Authorization": localStorage.getItem("token"),
            "Content-Type": "application/json"
        },
        body: JSON.stringify({currentPath: srcPath, newParentDir: destDir})
    });
}
