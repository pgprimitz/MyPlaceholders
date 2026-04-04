package dev.nutellim.MyPlaceholders.controllers;

import dev.nutellim.MyPlaceholders.MyPlaceholder;
import dev.nutellim.MyPlaceholders.models.types.AnimationPlaceholder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AnimationTickManager {

    private final MyPlaceholder plugin;
    private final Set<AnimationPlaceholder> animations = ConcurrentHashMap.newKeySet();
    private BukkitTask task;
    private volatile long globalTick = 0;

    public AnimationTickManager(MyPlaceholder plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null && !task.isCancelled()) return;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                globalTick++;
                for (AnimationPlaceholder animation : animations) {
                    animation.tick(globalTick);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 1L, 1L);
    }

    public void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
        globalTick = 0;
    }

    public void register(AnimationPlaceholder animation) {
        animations.add(animation);
    }

    public void unregister(AnimationPlaceholder animation) {
        animations.remove(animation);
    }

    public void unregisterAll() {
        animations.clear();
    }

    public Collection<AnimationPlaceholder> getAnimations() {
        return Collections.unmodifiableCollection(animations);
    }
}
