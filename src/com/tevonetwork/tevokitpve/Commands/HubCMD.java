package com.tevonetwork.tevokitpve.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.Messages.CategoryMSG;
import com.tevonetwork.tevoapi.Core.Travel.SendtoServer;

public class HubCMD implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player p = (Player)sender;
			if (UtilPlayer.isinCombat(p))
			{	
				UtilPlayer.message(Category.PVP, p, CC.tnError + "You are still in combat mode!");
				return true;
			}
			UtilPlayer.message(Category.TRAVEL, p, CC.tnInfo + "Sending you to " + CC.tnValue + "Hub" + CC.end);
			new SendtoServer(p, "hubborderline");
		}
		else
		{
			CategoryMSG.senderMessagePlayersOnly(sender, Category.PVP);
		}
		return true;
	}

	
	
}
