package com.tevonetwork.tevokitpve;

import org.bukkit.configuration.file.FileConfiguration;

import com.tevonetwork.tevoapi.API.Configs.ConfigFile;

public class ConfigManager {

	private ConfigFile system;
	
	public void load()
	{
		TevoKitPVE kitpve = TevoKitPVE.getInstance();
		this.system = new ConfigFile(kitpve, kitpve.getDataFolder(), "system", false);
		
		reloadSystem();
	}
	
	public FileConfiguration getSystem()
	{
		return this.system.getConfig();
	}
	
	public void saveSystem()
	{
		this.system.save();
	}
	
	public void reloadSystem()
	{
		this.system.reload();
	}
}
