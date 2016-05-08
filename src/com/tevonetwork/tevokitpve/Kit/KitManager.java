package com.tevonetwork.tevokitpve.Kit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.tevonetwork.tevoapi.TevoAPI;
import com.tevonetwork.tevoapi.API.MySQL.SQLCallback;
import com.tevonetwork.tevoapi.API.MySQL.SQLManager;
import com.tevonetwork.tevoapi.API.MySQL.SQLRunnable;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer.playerSounds;
import com.tevonetwork.tevoapi.API.Util.UtilUUID;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.LogLevel;
import com.tevonetwork.tevoapi.Core.Rank;
import com.tevonetwork.tevokitpve.TevoKitPVE;
import com.tevonetwork.tevokitpve.GUIs.PurchaseConfirmation;
import com.tevonetwork.tevokitpve.NPC.NPC;
import com.tevonetwork.tevokitpve.PVE.PVEManager;

public class KitManager {

	private static TevoKitPVE main = TevoKitPVE.getInstance();
	private static SQLManager sql = TevoAPI.getInstance().getSQLManager();
	private static HashMap<String, Kit> player_Kits = new HashMap<String, Kit>();
	public static HashMap<String, Kit> pending_purchase = new HashMap<String, Kit>();
	private static HashMap<String, ArrayList<String>> unlock_Cache = new HashMap<String, ArrayList<String>>();
	private static ArrayList<NPC> npcs = new ArrayList<NPC>();
	private static BukkitTask npc_Reattach;
	private static String table_KitPVEKits = "KitPVE_Kits";

	public static void load() {
		main.getUtilLogger().logNormal("KitManager> Spawning NPCs in 5 seconds!");
		new BukkitRunnable() {

			@Override
			public void run() {
				FileConfiguration system = main.getConfigManager().getSystem();
				if (!system.contains("npcs")) {
					main.getUtilLogger().logNormal("KitManager> No NPCs have been setup!");
					return;
				}
				Set<String> keys = main.getConfigManager().getSystem().getConfigurationSection("npcs").getKeys(false);
				Iterator<String> itr = keys.iterator();
				int loaded = 0;
				while (itr.hasNext()) {
					String kitname = itr.next();
					if (getKitFromString(kitname) != null) {
						NPC npc = new NPC(getSysLoc("npcs." + kitname), getKitFromString(kitname));
						npc.spawn();
						npcs.add(npc);
						main.getUtilLogger().logNormal("KitManager> Loaded NPC for kit " + kitname);
						loaded++;
					}
				}
				main.getUtilLogger().logNormal("KitManager> Loaded " + loaded + " NPCs!");
			}
		}.runTaskLater(main, 100L);

		npc_Reattach = new BukkitRunnable() {

			@Override
			public void run() {
				for (NPC npc : npcs) {
					npc.reattach();
				}
			}
		}.runTaskTimer(main, 200L, 200L);
	}

	public static void shutdown() {
		if (npc_Reattach != null) {
			npc_Reattach.cancel();
		}
		int despawned = 0;
		for (Entity ent : PVEManager.getSpawnpoint().getWorld().getEntities()) {
			if (ent.hasMetadata("NPC")) {
				ent.remove();
				despawned++;
			}
		}
		main.getUtilLogger().logNormal("KitManager> Despawned " + despawned + " NPCs!");
	}

	public static void equipKit(Kit kit) {
		if (kit.getPlayer() == null) {
			return;
		}
		if (PVEManager.stopping) {
			UtilPlayer.message(Category.PVP, kit.getPlayer(), CC.tnInfo + "Server restarting!");
			return;
		}
		if (kit.getPlayer().getGameMode() == GameMode.CREATIVE) {
			return;
		}
		if (!isInCache(kit.getPlayer())) {
			return;
		}
		if (kit.getRankRequired() != null) {
			if (!UtilPlayer.hasRank(kit.getPlayer(), kit.getRankRequired())) {
				UtilPlayer.sound(kit.getPlayer(), playerSounds.TRANSACTIONFAILED);
				UtilPlayer.messageFooter(kit.getPlayer());
				UtilPlayer.messageNoCategory(kit.getPlayer(), CC.cYellow + CC.fBold + "Kit - " + CC.cGreen + CC.fBold + kit.getName());
				if (kit.getDescription() != null) {
					for (String line : kit.getDescription()) {
						UtilPlayer.messageNoCategory(kit.getPlayer(), line);
					}
				}
				UtilPlayer.messageFooter(kit.getPlayer());
				UtilPlayer.message(Category.KIT, kit.getPlayer(), CC.tnInfo + "This kit is for players with " + Rank.getRankPrefix(kit.getRankRequired()) + CC.tnInfo
						+ " rank and above. You can purchase a rank at " + CC.tnValue + "store.tevonetwork.com");
				return;
			}
		}
		if (!hasunlockedKit(kit.getPlayer(), kit)) {
			new PurchaseConfirmation(kit.getPlayer(), kit);
			UtilPlayer.messageFooter(kit.getPlayer());
			UtilPlayer.messageNoCategory(kit.getPlayer(), CC.cYellow + CC.fBold + "Kit - " + CC.cGreen + CC.fBold + kit.getName());
			if (kit.getDescription() != null) {
				for (String line : kit.getDescription()) {
					UtilPlayer.messageNoCategory(kit.getPlayer(), line);
				}
			}
			UtilPlayer.messageFooter(kit.getPlayer());
			return;
		}
		if (hasKit(kit.getPlayer())) {
			unequipKit(kit.getPlayer(), true);
		}
		kit.equip();
		player_Kits.put(kit.getPlayer().getName(), kit);
	}

	public static void unequipKit(Player p, boolean silent) {
		if (player_Kits.containsKey(p.getName())) {
			player_Kits.get(p.getName()).unequip(silent);
			if (player_Kits.get(p.getName()) instanceof BlockChanging) {
				for (BlockState affected : ((BlockChanging) player_Kits.get(p.getName())).getAffectedBlocks()) {
					affected.update(true);
				}
			}
			player_Kits.remove(p.getName());
		}
	}

	public static boolean hasKit(Player p) {
		return player_Kits.containsKey(p.getName());
	}

	public static Kit getKit(Player p) {
		return player_Kits.get(p.getName());
	}

	public static boolean setKitNPC(Location loc, String kitname) {
		if (getKitFromString(kitname) == null) {
			return false;
		}
		Kit kit = getKitFromString(kitname);
		for (NPC npc : npcs) {
			if (npc.getKit().getName().equalsIgnoreCase(kit.getName())) {
				npc.despawn();
				npcs.remove(npc);
				break;
			}
		}
		NPC newnpc = new NPC(loc, kit);
		newnpc.spawn();
		npcs.add(newnpc);
		setSysLoc("npcs." + kit.getName(), loc);
		main.getUtilLogger().logNormal("KitManager> NPC for kit " + kit.getName() + " has been set.");
		return true;
	}

	public static boolean removeKitNPC(String kitname) {
		if (getKitFromString(kitname) == null) {
			return false;
		}
		Kit kit = getKitFromString(kitname);
		NPC npc = null;
		for (NPC current : npcs) {
			if (current.getKit().getName().equalsIgnoreCase(kit.getName())) {
				npc = current;
				break;
			}
		}
		if (npc == null) {
			return false;
		}
		npc.despawn();
		npcs.remove(npc);
		main.getConfigManager().getSystem().set("npcs." + kit.getName(), null);
		main.getConfigManager().saveSystem();
		main.getUtilLogger().logNormal("KitManager> NPC for kit " + kit.getName() + " has been removed.");
		return true;
	}

	public static void handleNPCClick(Entity clicked, Player clicker) {
		if (!(clicked instanceof Zombie)) {
			return;
		}
		Zombie npc = (Zombie) clicked;
		if (!npc.hasMetadata("NPC")) {
			return;
		}
		if (getKitFromString(npc.getMetadata("NPC").get(0).asString()) != null) {
			Kit kit = getKitFromString(npc.getMetadata("NPC").get(0).asString());
			if (hasKit(clicker)) {
				if (getKit(clicker).getName().equalsIgnoreCase(kit.getName())) {
					return;
				}
			}
			kit.setOwner(clicker);
			equipKit(kit);
		}
	}

	public static void unlockKit(Player p, Kit kit) {
		final String kitname = kit.getName();
		final String uuid = UtilUUID.toString(p.getUniqueId());
		final String pname = p.getName();
		if (unlock_Cache.containsKey(pname)) {
			unlock_Cache.get(pname).add(kitname);
		}
		SQLCallback<Boolean> call = new SQLCallback<Boolean>() {
			@Override
			public void execute(Boolean response) {
				if (!response) {
					SQLCallback<Boolean> call2 = new SQLCallback<Boolean>() {

						@Override
						public void execute(Boolean response) {
							if (response) {
								main.getServer().getScheduler().runTaskAsynchronously(main,
										new SQLRunnable("UPDATE " + table_KitPVEKits + " SET " + kitname + " = 1 WHERE UUID ='" + uuid + "';", "KitManager"));
							}
							else {
								main.getServer().getScheduler().runTaskAsynchronously(main,
										new SQLRunnable("INSERT INTO " + table_KitPVEKits + " (UUID," + kitname + ") VALUES('" + uuid + "',1);", "KitManager"));
							}
							main.getUtilLogger().logNormal("KitManager> " + pname + " has unlocked " + kitname + "!");
						}
					};
					runCallback("SELECT UUID FROM " + table_KitPVEKits + " WHERE UUID ='" + uuid + "';", call2);
				}
			}
		};
		runCallback(uuid, kitname, call);
	}

	public static boolean hasunlockedKit(Player p, Kit kit) {
		String kitname = kit.getName();
		if (unlock_Cache.containsKey(p.getName())) {
			return (unlock_Cache.get(p.getName())).contains(kitname);
		}
		return false;
	}

	public static boolean isInCache(Player p) {
		return unlock_Cache.containsKey(p.getName());
	}

	public static void removeCache(Player p) {
		unlock_Cache.remove(p.getName());
	}

	public static void addPlayerCache(Player p) {
		final String uuid = UtilUUID.toString(p.getUniqueId());
		final String pname = p.getName();
		new BukkitRunnable() {

			@Override
			public void run() {
				ArrayList<String> unlocked = new ArrayList<String>();
				try {
					if (!sql.existanceQuery("SELECT UUID FROM " + table_KitPVEKits + " WHERE UUID ='" + uuid + "';")) {
						sql.standardQuery("INSERT INTO " + table_KitPVEKits + " (UUID) VALUES('" + uuid + "');");
					}
					ResultSet set = sql.sqlQuery("SELECT * FROM " + table_KitPVEKits + " WHERE UUID ='" + uuid + "';");
					if ((set != null) && (set.next())) {
						for (Kits kit : Kits.values()) {
							if (set.getBoolean(Kits.getName(kit))) {
								unlocked.add(Kits.getName(kit));
							}
							else {
								if (Kits.isUnlockedDefault(kit)) {
									unlocked.add(Kits.getName(kit));
								}
							}
						}
						set.close();
					}
				}
				catch (SQLException e) {
					main.getUtilLogger().logLevel(LogLevel.WARNING, "KitManager> Cache callback failed!");
					e.printStackTrace();
				}
				final ArrayList<String> cache = unlocked;
				new BukkitRunnable() {

					@Override
					public void run() {
						unlock_Cache.put(pname, cache);
					}
				}.runTask(main);
			}
		}.runTaskAsynchronously(main);
	}

	private static void runCallback(final String query, final SQLCallback<Boolean> callback) {
		new BukkitRunnable() {

			@Override
			public void run() {
				boolean ret = false;
				try {
					ret = sql.existanceQuery(query);
				}
				catch (SQLException e) {
					main.getUtilLogger().logLevel(LogLevel.WARNING, "KitManager> SQL callback encountered an issue!");
				}
				final boolean callboolean = ret;
				new BukkitRunnable() {

					@Override
					public void run() {
						callback.execute(callboolean);
					}
				}.runTask(main);
			}
		}.runTaskAsynchronously(main);
	}

	private static void runCallback(final String uuid, final String kitname, final SQLCallback<Boolean> callback) {
		new BukkitRunnable() {

			@Override
			public void run() {
				boolean ret = false;
				try {
					ResultSet set = sql.sqlQuery("SELECT * FROM " + table_KitPVEKits + " WHERE UUID ='" + uuid + "';");
					if ((set != null) && (set.next())) {
						ret = set.getBoolean(kitname);
						set.close();
					}
				}
				catch (SQLException e) {
					main.getUtilLogger().logLevel(LogLevel.WARNING, "KitManager> SQL callback encountered an issue!");
				}
				final boolean callboolean = ret;
				new BukkitRunnable() {

					@Override
					public void run() {
						callback.execute(callboolean);
					}
				}.runTask(main);
			}
		}.runTaskAsynchronously(main);
	}

	private static void setSysLoc(String path, Location loc) {
		if ((loc == null) || (path == null)) {
			return;
		}
		FileConfiguration system = main.getConfigManager().getSystem();
		system.set(path + ".world", loc.getWorld().getName());
		system.set(path + ".x", loc.getX());
		system.set(path + ".y", loc.getY());
		system.set(path + ".z", loc.getZ());
		system.set(path + ".yaw", loc.getYaw());
		system.set(path + ".pitch", loc.getPitch());
		main.getConfigManager().saveSystem();
	}

	private static Location getSysLoc(String path) {
		if (path == null) {
			return null;
		}
		Location loc = null;
		FileConfiguration system = main.getConfigManager().getSystem();
		World world = Bukkit.getWorld(system.getString(path + ".world"));
		double x = system.getDouble(path + ".x");
		double y = system.getDouble(path + ".y");
		double z = system.getDouble(path + ".z");
		double yaw = system.getDouble(path + ".yaw");
		double pitch = system.getDouble(path + ".pitch");
		loc = new Location(world, x, y, z, (float) yaw, (float) pitch);
		return loc;
	}

	public static Kit getKitFromString(String kitname) {
		return Kits.getKit(kitname);
	}

}
