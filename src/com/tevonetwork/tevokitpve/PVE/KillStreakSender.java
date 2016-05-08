package com.tevonetwork.tevokitpve.PVE;

import org.bukkit.entity.Player;

import com.tevonetwork.tevoapi.API.Titles.ActionBar;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.UtilServer;
import com.tevonetwork.tevokitpve.Listeners.PlayerListeners;

public class KillStreakSender implements Runnable{

	@Override
	public void run()
	{
		for (Player ps : UtilServer.getPlayers())
		{
			if (PVEManager.isPVPEnabled(ps))
			{
				String streak = PlayerListeners.killstreakcount.get(ps.getName()) == null ? String.valueOf(0) : String.valueOf(PlayerListeners.killstreakcount.get(ps.getName()));
				ActionBar streakbar = new ActionBar(CC.cYellow + CC.fBold + "Current Killstreak: " + CC.tnValue + CC.fBold + streak);
				streakbar.send(ps);
			}
		}
	}
	
}
