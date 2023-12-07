package me.gotitim.clientcrasher;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.Map;

public final class SimpleScheduler {
    private static final Map<Long, MutablePair<Long, Runnable>> tasks = new HashMap<>();
    private static long taskId = 0;
    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tasks.forEach((id, task) -> {
                if (task.getLeft() != 0) {
                    task.setLeft(task.left - 1);
                    return;
                }
                task.getRight().run();
            });
        });
    }

    public static void schedule(Long ticks, Runnable callback) {
        tasks.put(++taskId, new MutablePair<>(ticks, callback));
    }
}
