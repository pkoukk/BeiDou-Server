/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gms.server;

import org.gms.provider.*;
import org.gms.provider.wz.WZFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 地图信息提供者
 * 负责解析wz文件中的地图数据，记录怪物ID与地图的关系
 * 
 * @author YourName
 */
public class MapInformationProvider {
    private static final Logger log = LoggerFactory.getLogger(MapInformationProvider.class);
    private static final MapInformationProvider instance = new MapInformationProvider();

    public static MapInformationProvider getInstance() {
        return instance;
    }

    protected DataProvider mapData;

    // 缓存：怪物ID -> 地图列表（地图ID -> 该地图上这个怪物的数量）
    protected Map<Integer, Map<Integer, Integer>> monsterMapCache = new ConcurrentHashMap<>();

    // 缓存：地图ID -> 怪物列表（怪物ID -> 该地图上这个怪物的数量）
    protected Map<Integer, Map<Integer, Integer>> mapMonsterCache = new ConcurrentHashMap<>();

    // 标记是否已初始化
    private volatile boolean initialized = false;

    private MapInformationProvider() {
        mapData = DataProviderFactory.getDataProvider(WZFiles.MAP);
    }

    /**
     * 初始化地图信息，解析所有地图的怪物数据
     */
    public synchronized void initialize() {
        if (initialized) {
            return;
        }

        log.info("开始初始化地图信息...");
        long startTime = System.currentTimeMillis();

        try {
            DataDirectoryEntry root = mapData.getRoot();

            // 遍历所有地图文件夹
            for (DataDirectoryEntry mapDir : root.getSubdirectories()) {
                if (!mapDir.getName().startsWith("Map")) {
                    continue;
                }

                // 遍历文件夹中的所有地图文件
                for (DataFileEntry mapFile : mapDir.getFiles()) {
                    try {
                        String fileName = mapFile.getName();
                        // 地图文件名格式：xxxxxxxxx.img
                        if (fileName.endsWith(".img")) {
                            String mapIdStr = fileName.substring(0, fileName.length() - 4);
                            int mapId = Integer.parseInt(mapIdStr);

                            // 解析这个地图的怪物数据
                            parseMapMonsters(mapId, mapDir.getName() + "/" + fileName);
                        }
                    } catch (Exception e) {
                        log.warn("解析地图文件失败: " + mapFile.getName(), e);
                    }
                }
            }

            initialized = true;
            long endTime = System.currentTimeMillis();
            log.info("地图信息初始化完成，耗时: {}ms，共解析 {} 个地图，{} 种怪物",
                    endTime - startTime, mapMonsterCache.size(), monsterMapCache.size());
        } catch (Exception e) {
            log.error("初始化地图信息失败", e);
        }
    }

    /**
     * 解析单个地图的怪物数据
     * 
     * @param mapId 地图ID
     * @param path  地图文件路径
     */
    private void parseMapMonsters(int mapId, String path) {
        try {
            Data mapDataEntry = mapData.getData(path);
            if (mapDataEntry == null) {
                return;
            }

            Data lifeData = mapDataEntry.getChildByPath("life");
            if (lifeData == null) {
                return;
            }

            Map<Integer, Integer> monstersInMap = new HashMap<>();

            // 遍历地图中的所有生物
            for (Data life : lifeData.getChildren()) {
                try {
                    String type = DataTool.getString(life.getChildByPath("type"), "");

                    // 只处理怪物类型（type = "m"）
                    if (!"m".equals(type)) {
                        continue;
                    }

                    String idStr = DataTool.getString(life.getChildByPath("id"), "");
                    if (idStr.isEmpty()) {
                        continue;
                    }

                    int monsterId = Integer.parseInt(idStr);

                    // 统计这个地图上该怪物的数量
                    monstersInMap.put(monsterId, monstersInMap.getOrDefault(monsterId, 0) + 1);
                } catch (Exception e) {
                    // 跳过解析失败的生物
                }
            }

            // 如果这个地图有怪物，更新缓存
            if (!monstersInMap.isEmpty()) {
                mapMonsterCache.put(mapId, monstersInMap);

                // 更新怪物到地图的映射
                for (Map.Entry<Integer, Integer> entry : monstersInMap.entrySet()) {
                    int monsterId = entry.getKey();
                    int count = entry.getValue();

                    monsterMapCache.computeIfAbsent(monsterId, k -> new HashMap<>())
                            .put(mapId, count);
                }
            }
        } catch (Exception e) {
            log.warn("解析地图 {} 的怪物数据失败", mapId, e);
        }
    }

    /**
     * 获取指定怪物所在的所有地图及其数量
     * 
     * @param monsterId 怪物ID
     * @return Map<地图ID, 怪物数量>，如果怪物不存在则返回空Map
     */
    public Map<Integer, Integer> getMapsByMonsterId(int monsterId) {
        ensureInitialized();
        return monsterMapCache.getOrDefault(monsterId, Collections.emptyMap());
    }

    /**
     * 获取多个怪物所在的所有地图及其数量
     * 
     * @param monsterIds 怪物ID集合
     * @return Map<地图ID, Map<怪物ID, 怪物数量>>
     */
    public Map<Integer, Map<Integer, Integer>> getMapsByMonsterIds(Collection<Integer> monsterIds) {
        ensureInitialized();

        Map<Integer, Map<Integer, Integer>> result = new HashMap<>();

        for (int monsterId : monsterIds) {
            Map<Integer, Integer> maps = monsterMapCache.get(monsterId);
            if (maps != null) {
                for (Map.Entry<Integer, Integer> entry : maps.entrySet()) {
                    int mapId = entry.getKey();
                    int count = entry.getValue();

                    result.computeIfAbsent(mapId, k -> new HashMap<>())
                            .put(monsterId, count);
                }
            }
        }

        return result;
    }

    /**
     * 获取指定地图上的所有怪物及其数量
     * 
     * @param mapId 地图ID
     * @return Map<怪物ID, 怪物数量>，如果地图不存在则返回空Map
     */
    public Map<Integer, Integer> getMonstersByMapId(int mapId) {
        ensureInitialized();
        return mapMonsterCache.getOrDefault(mapId, Collections.emptyMap());
    }

    /**
     * 检查指定地图是否有指定怪物
     * 
     * @param mapId     地图ID
     * @param monsterId 怪物ID
     * @return true如果地图上有这个怪物
     */
    public boolean hasMonsterInMap(int mapId, int monsterId) {
        ensureInitialized();
        Map<Integer, Integer> monsters = mapMonsterCache.get(mapId);
        return monsters != null && monsters.containsKey(monsterId);
    }

    /**
     * 获取指定地图上指定怪物的数量
     * 
     * @param mapId     地图ID
     * @param monsterId 怪物ID
     * @return 怪物数量，如果不存在则返回0
     */
    public int getMonsterCountInMap(int mapId, int monsterId) {
        ensureInitialized();
        Map<Integer, Integer> monsters = mapMonsterCache.get(mapId);
        return monsters != null ? monsters.getOrDefault(monsterId, 0) : 0;
    }

    /**
     * 获取所有有怪物的地图ID列表
     * 
     * @return 地图ID集合
     */
    public Set<Integer> getAllMapIds() {
        ensureInitialized();
        return Collections.unmodifiableSet(mapMonsterCache.keySet());
    }

    /**
     * 获取所有有地图记录的怪物ID列表
     * 
     * @return 怪物ID集合
     */
    public Set<Integer> getAllMonsterIds() {
        ensureInitialized();
        return Collections.unmodifiableSet(monsterMapCache.keySet());
    }

    /**
     * 确保已初始化
     */
    private void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }

    /**
     * 清除所有缓存
     */
    public synchronized void clearCache() {
        monsterMapCache.clear();
        mapMonsterCache.clear();
        initialized = false;
        log.info("地图信息缓存已清除");
    }
}
