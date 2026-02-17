package org.gms.client;

import org.gms.server.TimerManager;

import java.util.concurrent.ScheduledFuture;

public class AutoBuffInstance {
    private final Character chr;
    private final int skillId;
    private final int level;

    private ScheduledFuture<?> task;

    public AutoBuffInstance(Character chr, int skillId, int level) {
        this.chr = chr;
        this.skillId = skillId;
        this.level = level;
    }

    public void start() {
        Skill skill = SkillFactory.getSkill(skillId);
        var effect = skill.getEffect(level);

        int duration = effect.getDuration();
        int refresh = Math.max(1000, duration - 3000); // 提前 3 秒

        // 首次立即 apply
        effect.applyTo(chr);

        task = TimerManager.getInstance().schedule(() -> {
            if (!shouldContinue()) {
                stop();
                return;
            }
            effect.applyTo(chr);
            start(); // 递归续
        }, refresh);
    }

    public void refresh() {
        if (task != null) {
            task.cancel(false);
        }
        start();
    }

    private boolean shouldContinue() {
        return chr.isLoggedIn()
                && chr.isAlive()
                && chr.getAutoBuffEnabled()
                && chr.hasAutoBuff(skillId);
    }

    public void stop() {
        if (task != null) {
            task.cancel(false);
        }
    }
}