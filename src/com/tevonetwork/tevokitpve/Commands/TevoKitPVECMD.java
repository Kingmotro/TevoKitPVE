package com.tevonetwork.tevokitpve.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.tevonetwork.tevoapi.Core.Messages.AuthorMSG;
import com.tevonetwork.tevokitpve.TevoKitPVE;

public class TevoKitPVECMD implements CommandExecutor{

	private TevoKitPVE main = TevoKitPVE.getInstance();
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		AuthorMSG.sendAuthorStamp("KitPVP", main.getDescription().getVersion(), sender);
		return false;
	}

	
}
