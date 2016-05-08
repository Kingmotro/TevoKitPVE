package com.tevonetwork.tevokitpve;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.tevonetwork.tevoapi.API.Util.UtilLogger;
import com.tevonetwork.tevokitpve.Commands.HubCMD;
import com.tevonetwork.tevokitpve.Commands.KitpveCMD;
import com.tevonetwork.tevokitpve.Commands.TevoKitPVECMD;
import com.tevonetwork.tevokitpve.Kit.KitManager;
import com.tevonetwork.tevokitpve.Listeners.BlockListeners;
import com.tevonetwork.tevokitpve.Listeners.EntityListeners;
import com.tevonetwork.tevokitpve.Listeners.InventoryListeners;
import com.tevonetwork.tevokitpve.Listeners.PlayerListeners;
import com.tevonetwork.tevokitpve.PVE.KillStreakSender;
import com.tevonetwork.tevokitpve.PVE.PVEManager;

public class TevoKitPVE extends JavaPlugin{

	private static TevoKitPVE instance;
	private UtilLogger logger;
	private ConfigManager cfm;
	
	@Override
	public void onEnable()
	{
		instance = this;
		checkDependencies();
		this.logger = new UtilLogger(this);
		startManagers();
		registerCMDs();
		registerListeners();
		startTasks();
		this.logger.logEnableDisable(true);
	}
	
	@Override
	public void onDisable()
	{
		KitManager.shutdown();
		this.logger.logEnableDisable(false);
	}
	
	public static TevoKitPVE getInstance()
	{
		return instance;
	}
	
	public UtilLogger getUtilLogger()
	{
		return this.logger;
	}
	
	public ConfigManager getConfigManager()
	{
		return this.cfm;
	}
	
	private void registerCMDs()
	{
		this.logger.logNormal("Plugin> Registering Commands...");
		this.getCommand("kitpvp").setExecutor(new KitpveCMD());
		this.getCommand("hub").setExecutor(new HubCMD());
		this.getCommand("tevokitpvp").setExecutor(new TevoKitPVECMD());
		this.logger.logNormal("Plugin> Commands have been registered!");
	}
	
	private void registerListeners()
	{
		this.logger.logNormal("Plugin> Registering Listeners...");
		this.getServer().getPluginManager().registerEvents(new EntityListeners(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
		this.getServer().getPluginManager().registerEvents(new InventoryListeners(), this);
		this.getServer().getPluginManager().registerEvents(new BlockListeners(), this);
		this.logger.logNormal("Plugin> Listeners have been registered!");
	}
	
	private void startManagers()
	{
		this.logger.logNormal("Plugin> Starting Managers...");
		this.cfm = new ConfigManager();
		this.cfm.load();
		KitManager.load();
		PVEManager.load();
		this.logger.logNormal("Plugin> Managers have been started!");
	}
	
	private void startTasks()
	{
		BukkitScheduler s = this.getServer().getScheduler();
		s.scheduleSyncRepeatingTask(this, new KillStreakSender(), 100L, 18L);
	}
	
	private void checkDependencies()
	{
		PluginManager pm = Bukkit.getServer().getPluginManager();
		if (pm.getPlugin("TevoAPI") == null)
		{
			this.getLogger().warning("Plugin> TevoAPI not found! Disabling.");
			this.getServer().getPluginManager().disablePlugin(this);
		}
		else
		{
			this.getLogger().info("Plugin> TevoAPI has been found!");
		}
		if (pm.getPlugin("LibsDisguises") == null)
		{
			this.getLogger().warning("Plugin> LibsDisguises not found! Disabling.");
			this.getServer().getPluginManager().disablePlugin(this);
		}
		else
		{
			this.getLogger().info("Plugin> LibsDisguises has been found!");
		}
	}
}
