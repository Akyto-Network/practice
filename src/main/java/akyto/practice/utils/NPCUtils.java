package akyto.practice.utils;

import akyto.practice.Practice;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.*;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.IOUtils;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class NPCUtils {
    public static EntityPlayer createNPC(UUID npcSkin, String name, Location npcLocation) {
        MinecraftServer server = ((CraftServer) Practice.API.getServer()).getServer();
        WorldServer world = ((CraftWorld) npcLocation.getWorld()).getHandle();

        GameProfile npcProfile = new GameProfile(npcSkin, /*"NPC " + */name);

        EntityPlayer npc = new EntityPlayer(server, world, npcProfile, new PlayerInteractManager(world));

        npc.setLocation(npcLocation.getX(), npcLocation.getY(), npcLocation.getZ(), npcLocation.getYaw(), npcLocation.getPitch());

        // Skin changer
        try {
            String jsonResponse = IOUtils.toString(new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + npcSkin + "?unsigned=false"), StandardCharsets.UTF_8);
            if (jsonResponse.isEmpty()) return npc;

            JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(jsonResponse);

            JSONArray jsonObjectTest = (JSONArray) jsonObject.get("properties");

            JSONObject skinJSON = (JSONObject) jsonObjectTest.getFirst();

            String skin = (String) skinJSON.get("value");

            String signature = (String) skinJSON.get("signature");

            npcProfile.getProperties().put("textures", new Property("textures", skin, signature));
        } catch (IOException | ParseException | URISyntaxException e) {
            Practice.API.getLogger().warning("Cannot get " + name + "'s skin");
        }

		return npc;
    }

    public static void spawnNPCs(Player player, List<EntityPlayer> npcs, boolean follow) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        for (EntityPlayer npc : npcs) {
            if (player.getWorld().getUID() != npc.getWorld().getWorld().getUID())
                continue;
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER));
            connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
            connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) ((npc.yaw % 360.) * 256 / 360)));
            DataWatcher watcher = npc.getDataWatcher();
            watcher.watch(10, (byte) 127);
            connection.sendPacket(new PacketPlayOutEntityMetadata(npc.getId(), npc.getDataWatcher(), follow));

            if (player.getServer().getOnlinePlayers().stream().noneMatch(p -> p.getPlayer().getName().equals(npc.getName()))) {
                player.getServer().getScheduler().runTaskLaterAsynchronously(Practice.API, () -> {
                    connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER));
                }, 60L);
            }
        }
    }

    public static void removeNPC(Player player, List<EntityPlayer> npcs) {
        // TODO
    }
}
