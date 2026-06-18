package me.gotitim.clientcrasher;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.Map;

public final class SimpleScheduler {
    private static final Map<Long, MutablePair<Long, Runnable>> tasks = new HashMap<>();
    private static long taskId = 0;
    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(_ -> {
            for (var entry : tasks.entrySet()) {
                var pair = entry.getValue();
                if (pair.left != 0) {
                    pair.setLeft(pair.left - 1);
                    return;
                }
                pair.right.run();
                tasks.remove(entry.getKey());
            }
        });
    }

    public static void schedule(Long ticks, Runnable callback) {
        tasks.put(++taskId, new MutablePair<>(ticks, callback));
    }
}
