/* ============================================================
    HackPlay IDE - Entry Script
============================================================ */

window.addEventListener("DOMContentLoaded", () => {
  console.log("🚀 HackPlay IDE Starting...");

  // 프로젝트 ID 확인
  if (!window.projectId) {
    console.error("❌ projectId not found");
    return;
  }

  console.log("📋 Project ID:", window.projectId);

  // 초기화 순서 중요!
  initMonaco();          // 1. Monaco Editor
  refreshTree();         // 2. File Tree

  // 3. 터미널 자동 생성 (5초 후)
  requestIdleCallback(() => {
    HackPlayTerminal.createTerminal();
  });

  console.log("✅ HackPlay IDE Ready");
});

// 전역 디버그 함수들
window.debugIDE = {
  refreshTree: () => window.refreshTree && window.refreshTree(),
  openFile: (path) => window.openFile && window.openFile(path),
  createTerminal: () => window.HackPlayTerminal && window.HackPlayTerminal.createTerminal(),
  checkStatus: () => {
    console.log({
      projectId: window.projectId,
      monaco: !!window.monaco,
      editor: !!window.EditorCore,
      terminal: !!window.HackPlayTerminal,
      fileTree: !!document.querySelector('#file-tree')
    });
  }
};
