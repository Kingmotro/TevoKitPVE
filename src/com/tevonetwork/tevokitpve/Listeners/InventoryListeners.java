package com.tevonetwork.tevokitpve.Listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer.playerSounds;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Economy.EconomyManager;
import com.tevonetwork.tevokitpve.Kit.Kit;
import com.tevonetwork.tevokitpve.Kit.KitManager;

public class InventoryListeners implements Listener{

	@EventHandler
	public void onInvClick(InventoryClickEvent e)
	{
		if (!(e.getWhoClicked() instanceof Player))
		{
			return;
		}
		Player p = (Player)e.getWhoClicked();
		if ((e.getSlotType() == SlotType.ARMOR) && (p.getGameMode() != GameMode.CREATIVE))
		{
			UtilPlayer.sound(p, playerSounds.GUI_NULL);
			e.setCancelled(true);
			return;
		}
		if (e.getInventory().getTitle().equalsIgnoreCase("Purchase this kit?"))
		{
			e.setCancelled(true);
			if ((e.getCurrentItem() != null) && (e.getCurrentItem().getType() != Material.AIR))
			{
				ItemStack clicked = e.getCurrentItem();
				if (clicked.hasItemMeta())
				{
					if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(CC.tnEnable + "CONFIRM"))
					{
						if (KitManager.pending_purchase.containsKey(p.getName()))
						{
							Kit kit = KitManager.pending_purchase.get(p.getName());
							if ((EconomyManager.getTokensBal(p) - kit.getCost()) >= 0)
							{
								p.closeInventory();
								EconomyManager.takeTokens(p, kit.getCost());
								KitManager.unlockKit(p, kit);
								UtilPlayer.sound(p, playerSounds.TRANSACTIONSUCCESS);
								UtilPlayer.transaction(p, -kit.getCost(), "purchasing " + CC.tnValue + kit.getName() + CC.tnInfo + " kit", null);
								UtilPlayer.message(Category.KIT, p, CC.tnInfo + "You unlocked " + CC.tnAbility + kit.getName() + CC.end);
								KitManager.equipKit(kit);
								KitManager.pending_purchase.remove(p.getName());
							}
							else
							{
								p.closeInventory();
								KitManager.pending_purchase.remove(p.getName());
								UtilPlayer.sound(p, playerSounds.TRANSACTIONFAILED);
								UtilPlayer.message(Category.TOKENS, p, CC.tnError + "You cannot afford this!");
							}
						}
						else
						{
							p.closeInventory();
							KitManager.pending_purchase.remove(p.getName());
						}
					}
					if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(CC.tnDisable + "CANCEL"))
					{
						p.closeInventory();
						UtilPlayer.sound(p, playerSounds.TRANSACTIONFAILED);
						KitManager.pending_purchase.remove(p.getName());
					}
				}
			}
		}
		
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent e)
	{
		if (e.getInventory().getTitle().equalsIgnoreCase("Purchase this kit?"))
		{
			Player p = (Player)e.getPlayer();
			KitManager.pending_purchase.remove(p.getName());
		}
	}
	
}
