package org.gms.server;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * MapInformationProvider 测试示例
 * 演示如何使用 MapInformationProvider 查询怪物和地图信息
 */
public class MapInformationProviderTest {

    @Test
    public void testGetMapsByMonsterId() {
        // 获取实例
        MapInformationProvider mapInfo = MapInformationProvider.getInstance();

        // 测试：查询单个怪物所在的所有地图
        int testMonsterId = 100100; // 示例怪物ID
        Map<Integer, Integer> maps = mapInfo.getMapsByMonsterId(testMonsterId);

        System.out.println("=== 查询怪物 " + testMonsterId + " 所在的地图 ===");
        if (maps.isEmpty()) {
            System.out.println("未找到该怪物的地图信息");
        } else {
            for (Map.Entry<Integer, Integer> entry : maps.entrySet()) {
                System.out.println("地图ID: " + entry.getKey() + ", 怪物数量: " + entry.getValue());
            }
        }
    }

    @Test
    public void testGetMapsByMonsterIds() {
        MapInformationProvider mapInfo = MapInformationProvider.getInstance();

        // 测试：查询多个怪物所在的地图（用于推荐打装地图）
        Set<Integer> monsterIds = new HashSet<>();
        monsterIds.add(100100);
        monsterIds.add(100101);
        monsterIds.add(100102);

        Map<Integer, Map<Integer, Integer>> result = mapInfo.getMapsByMonsterIds(monsterIds);

        System.out.println("\n=== 查询多个怪物所在的地图（适合打装） ===");
        if (result.isEmpty()) {
            System.out.println("未找到相关地图信息");
        } else {
            for (Map.Entry<Integer, Map<Integer, Integer>> mapEntry : result.entrySet()) {
                int mapId = mapEntry.getKey();
                System.out.println("地图ID: " + mapId);

                for (Map.Entry<Integer, Integer> monsterEntry : mapEntry.getValue().entrySet()) {
                    System.out.println("  - 怪物ID: " + monsterEntry.getKey() +
                            ", 数量: " + monsterEntry.getValue());
                }
            }
        }
    }

    @Test
    public void testGetMonstersByMapId() {
        MapInformationProvider mapInfo = MapInformationProvider.getInstance();

        // 测试：查询指定地图上的所有怪物
        int testMapId = 104000000; // 示例：明珠港
        Map<Integer, Integer> monsters = mapInfo.getMonstersByMapId(testMapId);

        System.out.println("\n=== 查询地图 " + testMapId + " 上的所有怪物 ===");
        if (monsters.isEmpty()) {
            System.out.println("该地图上没有怪物或地图不存在");
        } else {
            for (Map.Entry<Integer, Integer> entry : monsters.entrySet()) {
                System.out.println("怪物ID: " + entry.getKey() + ", 数量: " + entry.getValue());
            }
        }
    }

    @Test
    public void testHasMonsterInMap() {
        MapInformationProvider mapInfo = MapInformationProvider.getInstance();

        // 测试：检查地图上是否有指定怪物
        int mapId = 104000000;
        int monsterId = 100100;

        boolean hasMonster = mapInfo.hasMonsterInMap(mapId, monsterId);

        System.out.println("\n=== 检查地图 " + mapId + " 是否有怪物 " + monsterId + " ===");
        System.out.println("结果: " + (hasMonster ? "是" : "否"));
    }

    @Test
    public void testGetMonsterCountInMap() {
        MapInformationProvider mapInfo = MapInformationProvider.getInstance();

        // 测试：获取地图上指定怪物的数量
        int mapId = 104000000;
        int monsterId = 100100;

        int count = mapInfo.getMonsterCountInMap(mapId, monsterId);

        System.out.println("\n=== 地图 " + mapId + " 上怪物 " + monsterId + " 的数量 ===");
        System.out.println("数量: " + count);
    }

    @Test
    public void testGetStatistics() {
        MapInformationProvider mapInfo = MapInformationProvider.getInstance();

        // 测试：获取统计信息
        Set<Integer> allMapIds = mapInfo.getAllMapIds();
        Set<Integer> allMonsterIds = mapInfo.getAllMonsterIds();

        System.out.println("\n=== 统计信息 ===");
        System.out.println("有怪物的地图总数: " + allMapIds.size());
        System.out.println("有地图的怪物总数: " + allMonsterIds.size());

        // 显示前10个地图ID作为示例
        System.out.println("\n前10个有怪物的地图ID:");
        allMapIds.stream().limit(10).forEach(mapId -> System.out.println("  - " + mapId));

        // 显示前10个怪物ID作为示例
        System.out.println("\n前10个有地图的怪物ID:");
        allMonsterIds.stream().limit(10).forEach(monsterId -> System.out.println("  - " + monsterId));
    }

    @Test
    public void testIntegrationWithEquipRecommendation() {
        // 演示如何在推荐打装地图功能中使用
        MapInformationProvider mapInfo = MapInformationProvider.getInstance();

        // 假设我们找到了掉落适合装备的怪物ID
        Set<Integer> dropMonsterIds = new HashSet<>();
        dropMonsterIds.add(100100);
        dropMonsterIds.add(100101);
        dropMonsterIds.add(100102);
        dropMonsterIds.add(100103);

        System.out.println("\n=== 推荐打装地图示例 ===");
        System.out.println("需要打的怪物: " + dropMonsterIds);

        // 获取这些怪物所在的地图
        Map<Integer, Map<Integer, Integer>> mapMonsters = mapInfo.getMapsByMonsterIds(dropMonsterIds);

        System.out.println("\n推荐地图列表:");
        // 计算每个地图的权重（简化版）
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : mapMonsters.entrySet()) {
            int mapId = entry.getKey();
            Map<Integer, Integer> monsters = entry.getValue();

            // 计算权重：怪物种类数 + 总数量
            int weight = monsters.size() * 10 + monsters.values().stream().mapToInt(Integer::intValue).sum();

            System.out.println("地图ID: " + mapId + ", 权重: " + weight);
            System.out.println("  包含怪物: " + monsters);
        }
    }
}
