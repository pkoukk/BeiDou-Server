var status = 0;

function start() {
  cm.sendYesNo(
    "把4个相同的装备放在装备栏前4个位置，\r\n花费金币，可将其精炼为一件更高级的装备，是否精炼？",
  );
}

function action(mode, type, selection) {
  if (mode == -1) {
    cm.dispose();
  } else {
    if (status == 1 && mode == 0) {
      cm.dispose();
      return;
    } else if (status >= 2 && mode == 0) {
      cm.dispose();
      return;
    }
    if (mode == 1) status++;
    else status--;
    if (status == 1) {
      if (cm.RefineEquip()) {
        cm.sendOk("#b精炼成功.....");
        cm.dispose();
      } else {
        cm.sendOk("#b精炼失败.....");
        cm.dispose();
      }
    } else {
      cm.dispose();
    }
  }
}
