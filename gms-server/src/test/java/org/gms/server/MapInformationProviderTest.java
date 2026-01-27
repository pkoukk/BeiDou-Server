package org.gms.server;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.Job;
import org.gms.scripting.AbstractPlayerInteraction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.mockito.Mockito.*;

/**
 * MapInformationProvider 测试示例
 * 演示如何使用 MapInformationProvider 查询怪物和地图信息
 * 以及如何测试推荐打装地图功能
 */
public class MapInformationProviderTest {

    @BeforeAll
    public static void setupAll() {
        // 确保MapInformationProvider初始化
        MapInformationProvider.getInstance().initialize();
    }

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

    /**
     * 完整测试：推荐打装地图功能
     * 模拟玩家查询推荐打装地图的完整流程
     */
    @Test
    public void testRecommendedEquipMapsComplete() {
        System.out.println("\n=== 完整测试：推荐打装地图功能 ===");

        // 1. 创建模拟对象
        Client mockClient = mock(Client.class);
        Character mockPlayer = mock(Character.class);

        // 2. 设置玩家属性 - 30级战士
        when(mockClient.getPlayer()).thenReturn(mockPlayer);
        when(mockPlayer.getJob()).thenReturn(Job.FIGHTER); // 战士职业
        when(mockPlayer.getLevel()).thenReturn(30); // 30级

        System.out.println("测试玩家: 30级战士");

        // 3. 创建AbstractPlayerInteraction实例
        AbstractPlayerInteraction api = new AbstractPlayerInteraction(mockClient);

        // 4. 调用推荐打装地图方法
        List<AbstractPlayerInteraction.MapRecommendation> recommendations = api.getRecommendedEquipMaps();

        // 5. 输出结果
        System.out.println("\n推荐地图数量: " + recommendations.size());

        if (recommendations.isEmpty()) {
            System.out.println("未找到推荐地图（可能是数据库中没有相关掉落数据）");
        } else {
            System.out.println("\n前10个推荐地图:");
            int count = 0;
            for (AbstractPlayerInteraction.MapRecommendation rec : recommendations) {
                if (count >= 10)
                    break;

                System.out.println("\n地图ID: " + rec.mapId);
                System.out.println("  权重: " + String.format("%.6f", rec.weight));
                System.out.println("  怪物数量: " + rec.monsters.size());

                // 显示前3个怪物
                int monsterCount = 0;
                for (AbstractPlayerInteraction.MonsterEquipInfo monster : rec.monsters) {
                    if (monsterCount >= 3)
                        break;

                    System.out.println("  - 怪物ID: " + monster.monsterId +
                            ", 数量: " + monster.count +
                            ", 掉落装备数: " + monster.equips.size());
                    monsterCount++;
                }

                count++;
            }
        }
    }

    /**
     * 测试不同职业和等级的推荐地图
     */
    @Test
    public void testRecommendedEquipMapsForDifferentJobs() {
        System.out.println("\n=== 测试不同职业的推荐打装地图 ===");

        // 测试多个职业
        Job[] testJobs = {
                Job.FIGHTER, // 战士
                Job.MAGICIAN, // 法师
                Job.BOWMAN, // 弓箭手
                Job.THIEF, // 飞侠
                Job.PIRATE // 海盗
        };

        int[] testLevels = { 30, 50, 70 };

        for (Job job : testJobs) {
            for (int level : testLevels) {
                Client mockClient = mock(Client.class);
                Character mockPlayer = mock(Character.class);

                when(mockClient.getPlayer()).thenReturn(mockPlayer);
                when(mockPlayer.getJob()).thenReturn(job);
                when(mockPlayer.getLevel()).thenReturn(level);

                AbstractPlayerInteraction api = new AbstractPlayerInteraction(mockClient);
                List<AbstractPlayerInteraction.MapRecommendation> recommendations = api.getRecommendedEquipMaps();

                System.out.println("\n职业: " + job.name() + ", 等级: " + level +
                        " -> 推荐地图数: " + recommendations.size());

                if (!recommendations.isEmpty()) {
                    AbstractPlayerInteraction.MapRecommendation top = recommendations.get(0);
                    System.out.println("  最佳地图: " + top.mapId +
                            ", 权重: " + String.format("%.6f", top.weight));
                }
            }
        }
    }

    /**
     * 测试获取指定地图的装备掉落详情
     */
    @Test
    public void testGetMapEquipDetail() {
        System.out.println("\n=== 测试获取指定地图的装备掉落详情 ===");

        Client mockClient = mock(Client.class);
        Character mockPlayer = mock(Character.class);

        // 设置为30级战士
        when(mockClient.getPlayer()).thenReturn(mockPlayer);
        when(mockPlayer.getJob()).thenReturn(Job.FIGHTER);
        when(mockPlayer.getLevel()).thenReturn(30);

        // 需要mock更多的方法来支持getMapEquipDetail
        // 这里简化处理，只展示API调用

        AbstractPlayerInteraction api = new AbstractPlayerInteraction(mockClient);

        // 测试几个常见地图
        int[] testMapIds = { 104000000, 103000000, 100000000 };

        for (int mapId : testMapIds) {
            try {
                AbstractPlayerInteraction.MapDetailInfo detail = api.getMapEquipDetail(mapId);

                if (detail != null) {
                    System.out.println("\n地图ID: " + mapId);
                    System.out.println("  怪物种类数: " + detail.monsters.size());

                    // 显示前3个怪物
                    int count = 0;
                    for (AbstractPlayerInteraction.MonsterEquipInfo monster : detail.monsters) {
                        if (count >= 3)
                            break;
                        System.out.println("  - 怪物ID: " + monster.monsterId +
                                ", 掉落装备数: " + monster.equips.size());
                        count++;
                    }
                } else {
                    System.out.println("\n地图ID: " + mapId + " - 无相关掉落数据");
                }
            } catch (Exception e) {
                System.out.println("\n地图ID: " + mapId + " - 查询失败: " + e.getMessage());
            }
        }
    }

    /**
     * 性能测试：测试推荐地图的查询性能
     */
    @Test
    public void testPerformance() {
        System.out.println("\n=== 推荐地图功能性能测试 ===");

        Client mockClient = mock(Client.class);
        Character mockPlayer = mock(Character.class);

        when(mockClient.getPlayer()).thenReturn(mockPlayer);
        when(mockPlayer.getJob()).thenReturn(Job.FIGHTER);
        when(mockPlayer.getLevel()).thenReturn(50);

        AbstractPlayerInteraction api = new AbstractPlayerInteraction(mockClient);

        // 预热
        api.getRecommendedEquipMaps();

        // 测试10次
        long totalTime = 0;
        int iterations = 10;

        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            List<AbstractPlayerInteraction.MapRecommendation> recommendations = api.getRecommendedEquipMaps();
            long endTime = System.currentTimeMillis();

            long elapsed = endTime - startTime;
            totalTime += elapsed;

            System.out.println("第" + (i + 1) + "次查询: " + elapsed + "ms, 结果数: " + recommendations.size());
        }

        System.out.println("\n平均查询时间: " + (totalTime / iterations) + "ms");
    }

    /**
     * 数据验证测试：验证推荐地图的数据正确性
     */
    @Test
    public void testDataValidation() {
        System.out.println("\n=== 推荐地图数据验证测试 ===");

        Client mockClient = mock(Client.class);
        Character mockPlayer = mock(Character.class);

        when(mockClient.getPlayer()).thenReturn(mockPlayer);
        when(mockPlayer.getJob()).thenReturn(Job.FIGHTER);
        when(mockPlayer.getLevel()).thenReturn(30);

        AbstractPlayerInteraction api = new AbstractPlayerInteraction(mockClient);
        List<AbstractPlayerInteraction.MapRecommendation> recommendations = api.getRecommendedEquipMaps();

        if (recommendations.isEmpty()) {
            System.out.println("无推荐地图数据，跳过验证");
            return;
        }

        System.out.println("开始验证 " + recommendations.size() + " 个推荐地图...");

        boolean hasErrors = false;

        for (AbstractPlayerInteraction.MapRecommendation rec : recommendations) {
            // 验证地图ID有效
            if (rec.mapId <= 0) {
                System.out.println("错误: 无效的地图ID: " + rec.mapId);
                hasErrors = true;
            }

            // 验证权重大于0
            if (rec.weight < 0) {
                System.out.println("错误: 地图 " + rec.mapId + " 权重为负: " + rec.weight);
                hasErrors = true;
            }

            // 验证怪物列表不为空
            if (rec.monsters == null || rec.monsters.isEmpty()) {
                System.out.println("错误: 地图 " + rec.mapId + " 没有怪物数据");
                hasErrors = true;
            }

            // 验证每个怪物的数据
            if (rec.monsters != null) {
                for (AbstractPlayerInteraction.MonsterEquipInfo monster : rec.monsters) {
                    if (monster.monsterId <= 0) {
                        System.out.println("错误: 地图 " + rec.mapId + " 有无效的怪物ID: " + monster.monsterId);
                        hasErrors = true;
                    }

                    if (monster.count <= 0) {
                        System.out.println(
                                "错误: 地图 " + rec.mapId + " 怪物 " + monster.monsterId + " 数量无效: " + monster.count);
                        hasErrors = true;
                    }

                    if (monster.equips == null || monster.equips.isEmpty()) {
                        System.out.println("错误: 地图 " + rec.mapId + " 怪物 " + monster.monsterId + " 没有装备掉落");
                        hasErrors = true;
                    }

                    // 验证装备掉落数据
                    if (monster.equips != null) {
                        for (AbstractPlayerInteraction.EquipDropInfo equip : monster.equips) {
                            if (equip.itemId <= 0) {
                                System.out.println("错误: 无效的装备ID: " + equip.itemId);
                                hasErrors = true;
                            }

                            if (equip.chance <= 0 || equip.chance > 1000000) {
                                System.out.println("错误: 装备 " + equip.itemId + " 掉率异常: " + equip.chance);
                                hasErrors = true;
                            }
                        }
                    }
                }
            }
        }

        if (hasErrors) {
            System.out.println("\n验证失败: 发现数据错误");
        } else {
            System.out.println("\n验证通过: 所有数据正确");
        }

        // 验证权重排序
        boolean sortedCorrectly = true;
        for (int i = 1; i < recommendations.size(); i++) {
            if (recommendations.get(i - 1).weight < recommendations.get(i).weight) {
                System.out.println("错误: 权重排序不正确");
                sortedCorrectly = false;
                break;
            }
        }

        if (sortedCorrectly) {
            System.out.println("权重排序: 正确");
        }
    }
}
