/* ============================================================
    HackPlay IDE - Entry Script
============================================================ */

window.addEventListener("DOMContentLoaded", () => {
  console.log("🚀 HackPlay IDE Starting...");
  
  initMonaco();          // Monaco Editor
  refreshTree();         // File Tree
  
  console.log("✅ HackPlay IDE Ready");
});