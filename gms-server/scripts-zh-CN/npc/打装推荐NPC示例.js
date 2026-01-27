/**
 * 测试NPC - 打装地图推荐系统
 * NPC ID: 可以使用任意空闲的NPC ID
 *
 * 使用方法：
 * 1. 将此文件放在 scripts-zh-CN/npc/ 目录下
 * 2. 重命名为对应的NPC ID，例如 9000000.js
 * 3. 在游戏中与该NPC对话即可打开推荐系统
 */

function start() {
  var text = "#fUI/UIWindow.img/Quest/icon6#\r\n";
  text += "#e#b【打装地图推荐系统】#k#n\r\n\r\n";
  text += "欢迎使用智能打装地图推荐系统！\r\n\r\n";
  text += "本系统将根据您的 #r职业#k 和 #r等级#k，\r\n";
  text += "为您推荐最适合获取装备的地图。\r\n\r\n";
  text += "#e功能特点：#n\r\n";
  text += "  #b●#k 智能筛选适合您的装备\r\n";
  text += "  #b●#k 综合计算掉落权重\r\n";
  text += "  #b●#k 详细显示怪物和掉率信息\r\n\r\n";
  text +=
    "#L1##fUI/UIWindow.img/PvP/Scroll/enabled/next2# #b查看推荐地图#k#l\r\n";
  text += "#L2##fUI/UIWindow.img/Quest/icon3# #d关于本系统#k#l\r\n";

  cm.sendSimple(text);
}

function action(mode, type, selection) {
  if (mode == -1 || mode == 0) {
    cm.dispose();
    return;
  }

  if (selection == 1) {
    // 打开推荐地图脚本
    cm.openNpc(cm.getNpc(), "掉宝地图");
    cm.dispose();
  } else if (selection == 2) {
    // 显示系统说明
    var text = "#fUI/UIWindow.img/Quest/icon6#\r\n";
    text += "#e#b【系统说明】#k#n\r\n\r\n";
    text += "#e推荐算法：#n\r\n";
    text += "系统会分析您的职业和等级（±10级），\r\n";
    text += "从所有地图中筛选出掉落适合装备的地图，\r\n";
    text += "并根据 #r装备掉率#k × #r怪物数量#k 计算权重，\r\n";
    text += "为您推荐综合收益最高的打装地点。\r\n\r\n";
    text += "#e显示信息：#n\r\n";
    text += "  #b地图列表#k - 显示推荐地图和权重\r\n";
    text += "  #b怪物信息#k - 等级、血量、数量\r\n";
    text += "  #b装备掉落#k - 基础掉率和实际掉率\r\n\r\n";
    text += "#e注意事项：#n\r\n";
    text += "  • 首次使用可能需要加载缓存\r\n";
    text += "  • 只显示已加载的地图\r\n";
    text += "  • 掉率会受到您的掉率加成影响\r\n\r\n";
    text += "祝您游戏愉快，收获满满！";

    cm.sendOk(text);
    cm.dispose();
  }
}
