package com.tevonetwork.tevokitpve.PVE;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.tevonetwork.tevoapi.TevoAPI;
import com.tevonetwork.tevoapi.API.Regions.Region;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.Messages.AnnounceMSG;
import com.tevonetwork.tevoapi.Core.Travel.SendtoServer;
import com.tevonetwork.tevokitpve.TevoKitPVE;
import com.tevonetwork.tevokitpve.Kit.KitManager;

public class PVEManager {

	private static Region spawn_region;
	private static TevoKitPVE main = TevoKitPVE.getInstance();
	private static HashMap<String, Boolean> pvp_enabled = new HashMap<String, Boolean>();
	public static boolean stopping = false;
	
	public static void setSpawnRegion(Location min, Location max)
	{
		spawn_region = new Region(min, max);
		setSysLoc("spawnregion.min", min);
		setSysLoc("spawnregion.max", max);
	}
	
	public static Region getSpawnRegion()
	{
		return spawn_region;
	}
	
	public static Location getSpawnpoint()
	{
		return TevoAPI.getInstance().getWorldManager().getWorldSpawn("kitpve");
	}
	
	public static void setPVPEnabled(Player p, boolean enabled)
	{
		pvp_enabled.put(p.getName(), enabled);
	}
	
	public static boolean isPVPEnabled(Player p)
	{
		return pvp_enabled.get(p.getName());
	}
	
	public static void handleLogout(Player p)
	{
		pvp_enabled.remove(p.getName());
	}
	
	public static void load()
	{
		if (!main.getConfigManager().getSystem().contains("spawnregion"))
		{
			main.getUtilLogger().logNormal("PVPManager> Spawn region has not been set!");
			return;
		}
		spawn_region = new Region(getSysLoc("spawnregion.min"), getSysLoc("spawnregion.max"));
	}
	
	public static void presafeStop()
	{
		stopping = true;
		AnnounceMSG.toServer(CC.cRED + CC.fBold + "Server is restarting in 5 seconds, PVP has been disabled, you will be transferred to the lobby!");
		for (Player players : Bukkit.getOnlinePlayers())
		{
			KitManager.unequipKit(players, false);
			PVEManager.setPVPEnabled(players, false);
			UtilPlayer.setoutCombat(players, true);
		}
		new BukkitRunnable() {
			@Override
			public void run()
			{
				for (Player players : Bukkit.getOnlinePlayers())
				{
					UtilPlayer.message(Category.SERVER, players, CC.tnInfo + "Server Restarting... Transferring you to lobby...");
					new SendtoServer(players, "hubborderline");
				}
				safeStop();
			}
		}.runTaskLater(main, 100L);
	}
	
	private static void safeStop()
	{
		main.getUtilLogger().logNormal("PVPManager> Server is safe stopping in 4 seconds!");
		KitManager.shutdown();
		new BukkitRunnable() {
			@Override
			public void run()
			{
				main.getUtilLogger().logNormal("PVPManager> Server is stopping!");
				Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "stop");
			}
		}.runTaskLater(main, 80L);
	}
	
	private static void setSysLoc(String path, Location loc)
	{
		if ((loc == null) || (path == null))
		{
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
	
	private static Location getSysLoc(String path)
	{
		if (path == null)
		{
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
		loc = new Location(world, x, y, z, (float)yaw, (float)pitch);
		return loc;
	}
	
}
