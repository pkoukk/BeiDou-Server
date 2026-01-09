package org.gms.client;

public class AutoBuffDef {
    public final int skillId;
    public final int minLevel;
    public final int costPerTick; // 每 10 分钟费用

    public AutoBuffDef(int skillId, int minLevel, int costPerTick) {
        this.skillId = skillId;
        this.minLevel = minLevel;
        this.costPerTick = costPerTick;
    }
}