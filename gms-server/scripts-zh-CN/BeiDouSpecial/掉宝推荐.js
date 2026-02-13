/*
 * 玩家推荐掉落物地图 NPC
 * 对应 Java 方法: cm.getPlayerRecommandMapInfo(Boolean includeBoos)
 */

var status = -1;
var mapDataList = null; // 缓存Java返回的数据
var selectedMapInfo = null; // 玩家当前选中的地图详情
var page = 0; // 当前页码
var PER_PAGE = 5; // 每页显示的地图数量（建议不要超过10，否则文本过长会断开连接）

function start() {
  action(1, 0, 0);
}

function action(mode, type, selection) {
  if (mode === -1) {
    cm.dispose();
    return;
  }

  // 如果在非初始状态点击了“上一步”或ESC，回退状态
  if (mode === 0) {
    if (status === 0) {
      cm.dispose();
      return;
    }
    status--;
  } else {
    status++;
  }

  if (status === 0) {
    // 第一层：选择查询类型
    var text =
      "你好，我是掉落推荐助手。\r\n根据你的职业和等级，我可以为你分析最适合你的打宝地图。\r\n\r\n";
    text += "#L0# 查询 #b小怪地图#k (挂机推荐)#l\r\n";
    text += "#L1# 查询 #rBOSS地图#k (攻坚推荐)#l";
    cm.sendSimple(text);
  } else if (status === 1) {
    // 初始化数据查询 (只在第一次进入此状态或重新查询时执行)
    if (mapDataList === null || (page === 0 && selection !== -1)) {
      // selection 为 0 (小怪) 或 1 (BOSS)
      var includeBoss = selection === 1;
      // 调用 Java 端实现的函数
      mapDataList = cm.getPlayerRecommandMapInfo(includeBoss);
      page = 0; // 重置页码
    }

    // 处理分页逻辑 (如果 selection < 0，说明是点击了上一页/下一页，保持在 status 1)
    if (selection < 0) {
      // 处理翻页
      var cmd = selection;
      if (cmd === -999) {
        // 下一页
        page++;
      } else if (cmd === -998) {
        // 上一页
        page--;
      }
      // 关键：减少 status 以抵消开头的 status++，保持在 status 1
      status--;
    }

    // 检查数据是否为空
    if (mapDataList == null || mapDataList.isEmpty()) {
      cm.sendOk(
        "抱歉，当前没有找到适合你等级和职业的推荐地图。\r\n请尝试提升等级后再来查询。",
      );
      cm.dispose();
      return;
    }

    // 构建显示文本
    var totalSize = mapDataList.size();
    var maxPage = Math.ceil(totalSize / PER_PAGE);

    // 边界修正
    if (page < 0) page = 0;
    if (page >= maxPage) page = maxPage - 1;

    var text = "查询结果 (第 " + (page + 1) + "/" + maxPage + " 页):\r\n";
    text += "选择地图查看具体掉落物：\r\n\r\n";

    var startIdx = page * PER_PAGE;
    var endIdx = Math.min(startIdx + PER_PAGE, totalSize);

    for (var i = startIdx; i < endIdx; i++) {
      var info = mapDataList.get(i);
      // 格式: [地图名] (ID) 总概率分
      // #L索引# ... #l
      text +=
        "#L" +
        i +
        "# #b" +
        info.getMapName() +
        "#k (ID:" +
        info.getMapId() +
        ")";
      text += "\r\n   推荐指数: #r" + info.getTotalChance() + "#k";
      if (info.isHasBoss()) {
        text += " [BOSS]";
      }
      text += "#l\r\n";
    }

    // 添加分页按钮
    text += "\r\n";
    if (page > 0) {
      text += "#L-998# [上一页]#l ";
    }
    if (page < maxPage - 1) {
      text += "#L-999# [下一页]#l";
    }

    cm.sendSimple(text);
  } else if (status === 2) {
    // 第二层：处理具体地图点击
    // selection 即为 list 中的 index

    // 如果误触发到这里且是翻页按钮（理论上不应该，因为在 status 1 已经处理了）
    // 直接回退到 status 0，让下次重新进入 status 1
    if (selection < 0) {
      status = 0;
      action(1, 0, 0);
      return;
    }

    // 获取选中的地图信息
    var index = selection;
    if (index >= 0 && index < mapDataList.size()) {
      selectedMapInfo = mapDataList.get(index);
    } else {
      cm.sendOk("发生错误，无法找到地图信息。");
      cm.dispose();
      return;
    }

    // 构建物品详情
    var mapId = selectedMapInfo.getMapId();
    var mapName = selectedMapInfo.getMapName();
    var items = selectedMapInfo.getItems(); // List<Integer>

    var text = "#b" + mapName + " (ID: " + mapId + ")#k 的推荐掉落：\r\n\r\n";

    if (items == null || items.isEmpty()) {
      text += "该地图暂无记录的目标物品掉落。";
    } else {
      // 遍历物品
      // 注意：如果物品太多，文本会溢出，这里也可以做分页，但通常一页能塞下几十个图标
      for (var i = 0; i < items.size(); i++) {
        var itemId = items.get(i);
        // #vID# 显示图片, #zID# 显示名字
        // 为了排版美观，一行显示一个或者两个
        text += "#v" + itemId + "# #b#z" + itemId + "##k\r\n";
        // 如果想显示属性，通常 NPC 无法直接显示动态属性数值，只能显示物品的静态Icon和Name
        // 玩家鼠标移动到 #v# 上面通常也不会显示属性（取决于客户端版本）
      }
    }

    // 可以在这里加一个“传送”功能
    // text += "\r\n#L9999# [立即传送] (可能需要费用)#l";

    cm.sendPrev(text); // 使用 sendPrev 允许玩家点 "Back" 返回列表
  } else if (status === 3) {
    // 如果在详情页点了某个按钮（比如传送）
    // if (selection == 9999) { cm.warp(selectedMapInfo.getMapId()); cm.dispose(); }
    cm.dispose();
  }
}
