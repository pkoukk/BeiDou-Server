/**
 * 功能：根据玩家等级和职业推荐打装地图
 * 作者：AI Assistant
 * 版本：1.0
 * 日期：2025-01-27
 *
 * 功能说明：
 * 1. 玩家打开脚本后，显示推荐的打装地图列表（按权重排序）
 * 2. 点击地图后，显示该地图的怪物列表和掉落的装备信息
 * 3. 装备根据玩家职业和等级（±10级）进行筛选
 */

var MonsterInformationProvider;
var ItemInformationProvider;
var LifeFactory;

var recommendedMaps; // 推荐地图列表
var currentMapDetail; // 当前查看的地图详情

function start() {
  if (MonsterInformationProvider == null) {
    MonsterInformationProvider = Java.type(
      "org.gms.server.life.MonsterInformationProvider",
    );
    ItemInformationProvider = Java.type(
      "org.gms.server.ItemInformationProvider",
    );
    LifeFactory = Java.type("org.gms.server.life.LifeFactory");
  }

  // 获取推荐地图列表
  recommendedMaps = cm.getRecommendedEquipMaps();

  levelmain();
}

function leveldispose() {
  cm.dispose();
}

function levelnull() {
  cm.dispose();
}

/**
 * 主界面：显示推荐地图列表
 */
function levelmain() {
  if (recommendedMaps == null || recommendedMaps.isEmpty()) {
    cm.sendOkLevel(
      "dispose",
      "当前没有找到适合您等级和职业的打装地图。\r\n请提升等级后再试。",
      2,
    );
    return;
  }

  var player = cm.getPlayer();
  var msgtext = `#e#b【推荐打装地图】#k#n\r\n`;
  msgtext += `职业：#d${player.getJob().getName()}#k  等级：#d${player.getLevel()}#k\r\n`;
  msgtext += "#d" + "\r\n".padStart(28, "——") + "#k\r\n";
  msgtext += "以下地图按综合掉落权重排序，点击查看详情：\r\n\r\n";

  var displayCount = Math.min(20, recommendedMaps.size()); // 最多显示20个地图

  for (var i = 0; i < displayCount; i++) {
    var mapRec = recommendedMaps.get(i);
    var mapId = mapRec.mapId;
    var weight = mapRec.weight;
    var monsterCount = mapRec.monsters.size();

    var mapName = getMapName(mapId);
    var weightStr = formatWeight(weight);

    msgtext += `#L${mapId}##fUI/UIWindow.img/UserList/Friend/icon04# `;
    msgtext += `#b${mapName}#k  [ ID: #r${mapId}#k ]\r\n`;
    msgtext += `    权重：#d${weightStr}#k  |  掉落怪物种类：#d${monsterCount}#k 种\r\n`;
    msgtext += "#l\r\n";
  }

  if (recommendedMaps.size() > displayCount) {
    msgtext += `\r\n#r（仅显示前${displayCount}个地图）#k`;
  }

  cm.sendNextSelectLevel("showMapDetail", msgtext, 2);
}

/**
 * 地图详情界面：显示地图中的怪物和掉落装备
 */
function levelshowMapDetail(mapId) {
  var map = cm.getMap(mapId);
  if (map == null) {
    cm.sendOkLevel("main", "该地图不存在或无法访问。", 2);
    return;
  }

  var mapName = getMapName(mapId);
  var msgtext = `#fMap/MapHelper/tuto/BalloonMsg1/3#\r\n`;
  msgtext += `#e#b【${mapName}】#k#n\r\n`;
  msgtext += `地图ID：#r${mapId}#k\r\n`;
  msgtext += "#d" + "\r\n".padStart(30, "——") + "#k\r\n";

  // 获取该地图的详细信息
  var mapDetail = null;
  for (var i = 0; i < recommendedMaps.size(); i++) {
    var mapRec = recommendedMaps.get(i);
    if (mapRec.mapId == mapId) {
      mapDetail = mapRec;
      break;
    }
  }

  if (mapDetail == null || mapDetail.monsters.isEmpty()) {
    msgtext += "\r\n该地图暂无适合您的装备掉落信息。";
    cm.sendLastLevel("main", msgtext, 2);
    return;
  }

  msgtext += "\r\n#e掉落装备的怪物列表：#n\r\n\r\n";

  var monsters = mapDetail.monsters;
  for (var i = 0; i < monsters.size(); i++) {
    var monsterInfo = monsters.get(i);
    var monsterId = monsterInfo.monsterId;
    var monsterCount = monsterInfo.count;
    var equipDrops = monsterInfo.equips;

    var monsterName = getMonsterName(monsterId);
    var monsterLevel = getMonsterLevel(monsterId);
    var monsterHp = getMonsterHp(monsterId);

    msgtext += `#L${i}##fUI/UIWindow.img/Quest/icon2#\r\n`;
    msgtext += `#b${monsterName}#k  [ Lv.${monsterLevel} | HP: ${formatNumber(monsterHp)} ]\r\n`;

    if (monsterCount > 1) {
      msgtext += `    地图数量：#d${monsterCount}#k 只\r\n`;
    }

    msgtext += "    #e装备掉落：#n\r\n";

    // 显示装备掉落信息
    for (var j = 0; j < equipDrops.size(); j++) {
      var equipDrop = equipDrops.get(j);
      var itemId = equipDrop.itemId;
      var chance = equipDrop.chance;

      var itemName = ItemInformationProvider.getInstance().getName(itemId);
      var dropRate = (chance / 10000).toFixed(4);
      var playerDropRate = (
        (chance / 10000) *
        cm.getPlayer().getDropRate() *
        cm.getPlayer().getFamilyDrop()
      ).toFixed(4);

      msgtext += `      #v${itemId}# #d${itemName}#k\r\n`;
      msgtext += `        基础掉率: #b${dropRate}%#k  |  你的掉率: #r${playerDropRate}%#k\r\n`;
    }

    msgtext += "#l\r\n";
  }

  msgtext +=
    "\r\n#fUI/UIWindow.img/PvP/Scroll/enabled/next2#  点击#b【上一项】#k返回地图列表";

  cm.sendLastLevel("main", msgtext, 2);
}

/**
 * 获取地图名称
 */
function getMapName(mapId) {
  try {
    var mapName = cm.getMap(mapId).getMapName();
    if (mapName == null || mapName == "") {
      return `地图${mapId}`;
    }
    return mapName;
  } catch (e) {
    return `地图${mapId}`;
  }
}

/**
 * 获取怪物名称
 */
function getMonsterName(monsterId) {
  try {
    var monster = LifeFactory.getMonster(monsterId);
    if (
      monster != null &&
      monster.getName() != null &&
      monster.getName() != "MISSINGNO"
    ) {
      return monster.getName();
    }
  } catch (e) {
    // 忽略
  }
  return `#o${monsterId}#`;
}

/**
 * 获取怪物等级
 */
function getMonsterLevel(monsterId) {
  try {
    return LifeFactory.getMonsterLevel(monsterId);
  } catch (e) {
    return "??";
  }
}

/**
 * 获取怪物血量
 */
function getMonsterHp(monsterId) {
  try {
    var monster = LifeFactory.getMonster(monsterId);
    if (monster != null) {
      return monster.getMaxHp();
    }
  } catch (e) {
    // 忽略
  }
  return 0;
}

/**
 * 格式化权重值
 */
function formatWeight(weight) {
  if (weight >= 1) {
    return weight.toFixed(2);
  } else if (weight >= 0.01) {
    return weight.toFixed(4);
  } else {
    return weight.toExponential(2);
  }
}

/**
 * 格式化数字（添加千位分隔符）
 */
function formatNumber(num) {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}
