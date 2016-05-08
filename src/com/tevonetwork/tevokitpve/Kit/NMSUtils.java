package com.tevonetwork.tevokitpve.Kit;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import net.minecraft.server.v1_8_R3.NBTTagCompound;

public class NMSUtils {

	public static ItemStack setUnbreakable(ItemStack is)
	{
		if (isBreakable(is))
		{
			net.minecraft.server.v1_8_R3.ItemStack nms_is = CraftItemStack.asNMSCopy(is);
			NBTTagCompound comp = new NBTTagCompound();
			comp.setBoolean("Unbreakable", true);
			nms_is.setTag(comp);
			ItemStack newis = CraftItemStack.asBukkitCopy(nms_is);
			ItemMeta meta = newis.getItemMeta();
			if (is.hasItemMeta())
			{
				if (is.getItemMeta().hasDisplayName())
				{
					meta.setDisplayName(is.getItemMeta().getDisplayName());
				}
				if (is.getItemMeta().hasLore())
				{
					meta.setLore(is.getItemMeta().getLore());
				}
				if ((is.getType() == Material.LEATHER_BOOTS) || (is.getType() == Material.LEATHER_CHESTPLATE) || (is.getType() == Material.LEATHER_HELMET) || (is.getType() == Material.LEATHER_LEGGINGS))
				{
					((LeatherArmorMeta)meta).setColor(((LeatherArmorMeta)is.getItemMeta()).getColor());
				}
			}
			meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			newis.setItemMeta(meta);
			newis.addEnchantments(is.getEnchantments());
			return newis;
		}
		return is;
	}
	
	private static boolean isBreakable(ItemStack is)
	{
		if (is == null)
		{
			return false;
		}
		if (is.getType() == Material.AIR)
		{
			return false;
		}
		Material type = is.getType();
		return type == Material.BOW || type == Material.WOOD_SWORD || type == Material.IRON_SWORD || type == Material.GOLD_SWORD || type == Material.DIAMOND_SWORD || type == Material.STONE_SWORD ||
				type == Material.LEATHER_CHESTPLATE || type == Material.LEATHER_BOOTS || type == Material.LEATHER_HELMET || type == Material.LEATHER_LEGGINGS ||
				type == Material.CHAINMAIL_BOOTS || type == Material.CHAINMAIL_CHESTPLATE || type == Material.CHAINMAIL_HELMET || type == Material.CHAINMAIL_LEGGINGS ||
				type == Material.IRON_BOOTS || type == Material.IRON_CHESTPLATE || type == Material.IRON_HELMET || type == Material.IRON_LEGGINGS ||
				type == Material.DIAMOND_BOOTS || type == Material.DIAMOND_CHESTPLATE || type == Material.DIAMOND_HELMET || type == Material.DIAMOND_LEGGINGS ||
				type == Material.GOLD_BOOTS || type == Material.GOLD_CHESTPLATE || type == Material.GOLD_HELMET || type == Material.GOLD_LEGGINGS ||
				type == Material.IRON_SPADE || type == Material.IRON_AXE || type == Material.IRON_HOE || type == Material.IRON_PICKAXE || type == Material.FLINT_AND_STEEL ||
				type == Material.WOOD_SPADE || type == Material.WOOD_HOE || type == Material.WOOD_PICKAXE || type == Material.WOOD_AXE ||
				type == Material.STONE_HOE || type == Material.STONE_PICKAXE || type == Material.STONE_SPADE || type == Material.STONE_SWORD ||
				type == Material.DIAMOND_SPADE || type == Material.DIAMOND_PICKAXE || type == Material.DIAMOND_AXE || type == Material.DIAMOND_HOE ||
				type == Material.FISHING_ROD || type == Material.SHEARS;
	}
	
}
