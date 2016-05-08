package com.tevonetwork.tevokitpve.GUIs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.ItemStackFactory;
import com.tevonetwork.tevoapi.Core.Items;
import com.tevonetwork.tevokitpve.Kit.Kit;
import com.tevonetwork.tevokitpve.Kit.KitManager;

public class PurchaseConfirmation {

	public PurchaseConfirmation(Player p, Kit kit)
	{
		Inventory menu = Bukkit.createInventory(null, 9, "Purchase this kit?");
		ItemStackFactory isf = new ItemStackFactory();
		KitManager.pending_purchase.put(p.getName(), kit);
		menu.setItem(4, isf.createItemStack(Items.PAPER, CC.tnInfo + "Purchase " + CC.tnAbility + kit.getName() + CC.tnInfo + " kit for " + CC.tnValue + kit.getCost() + " Tevo Tokens" + CC.tnInfo + "?"));
		menu.setItem(0, isf.createItemStack(Items.EMERALDBLOCK, CC.tnEnable + "CONFIRM"));
		menu.setItem(8, isf.createItemStack(Items.REDSTONEBLOCK, CC.tnDisable + "CANCEL"));
		p.openInventory(menu);
	}
	
}
