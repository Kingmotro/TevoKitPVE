package com.tevonetwork.tevokitpve.PVE;

import org.bukkit.scheduler.BukkitRunnable;

import com.tevonetwork.tevokitpve.Listeners.PlayerListeners;

public class KillStreakCountdown extends BukkitRunnable{

	private String key;
	private int time = 0;
	
	public KillStreakCountdown(String key, int time)
	{
		this.time = time;
		this.key = key;
	}
	
	@Override
	public void run()
	{
		if (this.time <= 0)
		{
			cancel();
			PlayerListeners.killstreak.remove(this.key);
			PlayerListeners.killstreakcountdown.remove(this.key);
		}
		this.time -= 1;
	}
	
	public void setTime(int time)
	{
		this.time = time;
	}
	
}
