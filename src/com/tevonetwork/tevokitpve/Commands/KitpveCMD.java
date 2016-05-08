package com.tevonetwork.tevokitpve.Commands;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import com.tevonetwork.tevoapi.API.Permissions.PermissionsHandler;
import com.tevonetwork.tevoapi.API.Portals.PortalManager;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.Rank;
import com.tevonetwork.tevoapi.Core.Messages.CategoryMSG;
import com.tevonetwork.tevoapi.Core.Messages.PermMSG;
import com.tevonetwork.tevokitpve.Kit.KitManager;
import com.tevonetwork.tevokitpve.PVE.PVEManager;

public class KitpveCMD implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player p = (Player)sender;
			if (UtilPlayer.hasRank(p, Rank.ADMIN))
			{
				if (args.length > 0)
				{
					if (args[0].equalsIgnoreCase("stop"))
					{
						if (!UtilPlayer.hasRank(p, Rank.DEVELOPER))
						{
							PermMSG.noPerm(sender, Rank.DEVELOPER);
							return true;
						}
						PVEManager.presafeStop();
					}
					else if (args[0].equalsIgnoreCase("cleanup"))
					{
						if (!UtilPlayer.hasRank(p, Rank.DEVELOPER))
						{
							PermMSG.noPerm(sender, Rank.DEVELOPER);
							return true;
						}
						World world = PVEManager.getSpawnpoint().getWorld();
						int cleanedup = 0;
						for (Entity ent : world.getEntities())
						{
							if (ent instanceof ArmorStand)
							{
								ArmorStand stand = (ArmorStand)ent;
								if (((CraftArmorStand)stand).getHandle().isInvisible())
								{
									if (!ent.hasMetadata("NPC"))
									{
										stand.remove();
										cleanedup++;
									}
								}
							}
							if (ent instanceof Zombie)
							{
								if (!ent.hasMetadata("NPC"))
								{
									ent.remove();
									cleanedup++;
								}
							}
							if (ent instanceof Item)
							{
								ent.remove();
								cleanedup++;
							}
						}
						UtilPlayer.message(Category.PVP, p, CC.tnInfo + "Cleaned up " + CC.tnValue + cleanedup + CC.tnInfo + " entities!");
					}
					else if (args[0].equalsIgnoreCase("npc"))
					{
						if (!UtilPlayer.hasRank(p, Rank.DEVELOPER))
						{
							PermMSG.noPerm(sender, Rank.DEVELOPER);
							return true;
						}
						if (args.length > 2)
						{
							if (args[1].equalsIgnoreCase("set"))
							{
								if (KitManager.setKitNPC(p.getLocation(), args[2]))
								{
									UtilPlayer.message(Category.PVP, p, CC.tnInfo + "Successfully set npc for kit " + CC.tnValue + args[2] + CC.end);
								}
								else
								{
									UtilPlayer.message(Category.PVP, p, CC.tnError + "Failed to set NPC, Invalid kit!");
								}
							}
							else if (args[1].equalsIgnoreCase("remove"))
							{
								if (KitManager.removeKitNPC(args[2]))
								{
									UtilPlayer.message(Category.PVP, p, CC.tnInfo + "Successfully removed npc for kit " + CC.tnValue + args[2] + CC.end);
								}
								else
								{
									UtilPlayer.message(Category.PVP, p, CC.tnError + "Failed to remove npc, Invalid kit or kit does not have npc!");
								}
							}
							else
							{
								CategoryMSG.senderArgsErr(sender, Category.PVP, "/kitpve npc <remove/set> <kitname>");
							}
						}
						else
						{
							CategoryMSG.senderArgsErr(sender, Category.PVP, "/kitpve npc <remove/set> <kitname>");
						}
					}
					else if (args[0].equalsIgnoreCase("region"))
					{
						if (!UtilPlayer.hasRank(p, Rank.DEVELOPER))
						{
							PermMSG.noPerm(sender, Rank.DEVELOPER);
							return true;
						}
						if (PortalManager.hasSelection(p))
						{
							PVEManager.setSpawnRegion(PortalManager.getPlayerMINSelection(p), PortalManager.getPlayerMAXSelection(p));
							UtilPlayer.message(Category.PVP, p, CC.tnInfo + "Successfully set the spawn region!");
						}
						else
						{
							UtilPlayer.message(Category.PVP, p, CC.tnError + "You must make a selection with the portal wand first!");
						}
					}
					else if (args[0].equalsIgnoreCase("admin"))
					{
						KitManager.unequipKit(p, false);
						p.setGameMode(GameMode.CREATIVE);
						UtilPlayer.message(Category.PVP, p, CC.tnInfo + "You have removed all kits and you are now free to roam!");
						UtilPlayer.setoutCombat(p, false);
					}
					else
					{
						UtilPlayer.messageHeader(Category.PVP, p, "KitPVE Command Usage");
						UtilPlayer.messageNoCategory(p, CC.tnUse + "/kitpve stop" + CC.tnInfo + " Safely stops the server while transferring players to lobby.");
						UtilPlayer.messageNoCategory(p, CC.tnUse + "/kitpve npc remove <kitname>" + CC.tnInfo + " Removes the npc associated with the specified kit.");
						UtilPlayer.messageNoCategory(p, CC.tnUse + "/kitpve npc set <kitname>" + CC.tnInfo + " Spawns and sets the npc position for the associated kit (existing npcs associated are moved).");
						UtilPlayer.messageNoCategory(p, CC.tnUse + "/kitpve region" + CC.tnInfo + " Sets the spawn region to your current selection with the portal wand.");
						UtilPlayer.messageNoCategory(p, CC.tnUse + "/kitpve cleanup" + CC.tnInfo + " Cleans up any invalid npcs, holograms and drops.");
						UtilPlayer.messageNoCategory(p, CC.tnUse + "/kitpve admin" + CC.tnInfo + " Unequips any kits you have and makes it safe for you to build and interact.");
						UtilPlayer.messageFooter(p);
					}
				}
				else
				{
					UtilPlayer.messageHeader(Category.PVP, p, "KitPVE Command Usage");
					UtilPlayer.messageNoCategory(p, CC.tnUse + "/kitpve stop" + CC.tnInfo + " Safely stops the server while transferring players to lobby.");
					UtilPlayer.messageNoCategory(p, CC.tnUse + "/kitpve npc remove <kitname>" + CC.tnInfo + " Removes the npc associated with the specified kit.");
					UtilPlayer.messageNoCategory(p, CC.tnUse + "/kitpve npc set <kitname>" + CC.tnInfo + " Spawns and sets the npc position for the associated kit (existing npcs associated are moved).");
					UtilPlayer.messageNoCategory(p, CC.tnUse + "/kitpve region" + CC.tnInfo + " Sets the spawn region to your current selection with the portal wand.");
					UtilPlayer.messageNoCategory(p, CC.tnUse + "/kitpve cleanup" + CC.tnInfo + " Cleans up any invalid npcs, holograms and drops.");
					UtilPlayer.messageNoCategory(p, CC.tnUse + "/kitpve admin" + CC.tnInfo + " Unequips any kits you have and makes it safe for you to build and interact.");
					UtilPlayer.messageFooter(p);
				}
			}
			else
			{
				PermMSG.noPerm(sender, Rank.DEVELOPER);
			}
		}
		else
		{
			if (PermissionsHandler.hasRankSender(sender, Rank.DEVELOPER))
			{
				if (args.length > 0)
				{
					if (args[0].equalsIgnoreCase("stop"))
					{
						PVEManager.presafeStop();
					}
					else
					{
						CategoryMSG.senderInvArgsErr(sender, Category.PVP, "/kitpve stop");
					}
				}
				else
				{
					CategoryMSG.senderArgsErr(sender, Category.PVP, "/kitpve stop");
				}
			}
		}
		return false;
	}

}
