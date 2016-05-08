package com.tevonetwork.tevokitpve.Kit;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer.playerSounds;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.LogLevel;
import com.tevonetwork.tevoapi.Core.Rank;
import com.tevonetwork.tevokitpve.TevoKitPVE;
import com.tevonetwork.tevokitpve.PVE.PVEManager;

public abstract class Kit implements Listener {

	private Player kit_owner;
	private int token_cost = 0;
	private String name;
	private String[] description;
	private Rank rank_required;
	private ItemStack helmet;
	private ItemStack chestplate;
	private ItemStack leggings;
	private ItemStack boots;
	private ArrayList<ItemStack> items = new ArrayList<ItemStack>();
	protected TevoKitPVE main = TevoKitPVE.getInstance();

	public Kit(String name, int cost, Player owner, String[] description) {
		this.token_cost = cost;
		this.name = name;
		this.kit_owner = owner;
		this.description = description;
	}

	public void equip() {
		if (this.kit_owner == null) {
			main.getUtilLogger().logLevel(LogLevel.WARNING, "Kit> Could not equip kit " + this.name + "owner is null!");
			return;
		}
		UtilPlayer.clearInv(this.kit_owner);
		this.kit_owner.updateInventory();
		this.kit_owner.setExp(0.0F);
		this.kit_owner.setLevel(0);
		Bukkit.getServer().getPluginManager().registerEvents(this, main);
		startTasks();
		this.kit_owner.getInventory().setHelmet(this.helmet);
		this.kit_owner.getInventory().setChestplate(this.chestplate);
		this.kit_owner.getInventory().setLeggings(this.leggings);
		this.kit_owner.getInventory().setBoots(this.boots);
		for (ItemStack is : this.items) {
			this.kit_owner.getInventory().addItem(is);
		}
		UtilPlayer.sound(this.kit_owner, playerSounds.EQUIP);
		UtilPlayer.message(Category.KIT, this.kit_owner, CC.tnInfo + "You " + CC.tnEnable + "equipped " + CC.tnValue + this.name + CC.tnInfo + " kit.");
		UtilPlayer.messageFooter(this.kit_owner);
		UtilPlayer.messageNoCategory(this.kit_owner, CC.cYellow + CC.fBold + "Kit - " + CC.cGreen + CC.fBold + this.name);
		if (this.description != null) {
			for (String line : this.description) {
				UtilPlayer.messageNoCategory(this.kit_owner, line);
			}
		}
		UtilPlayer.messageFooter(this.kit_owner);
	}

	public void unequip(boolean silent) {
		HandlerList.unregisterAll(this);
		stopTasks();
		onUnequip();
		UtilPlayer.clearInv(this.kit_owner);
		if (!silent) {
			UtilPlayer.message(Category.KIT, this.kit_owner, CC.tnInfo + "You " + CC.tnDisable + "unequipped " + CC.tnValue + this.name + CC.tnInfo + " kit.");
			UtilPlayer.sound(this.kit_owner, playerSounds.UNEQUIP);
		}
	}

	public void startTasks() {
	}

	public void stopTasks() {
	}

	protected void onUnequip() {
	}

	// Setters
	public void setName(String name) {
		this.name = name;
	}

	protected void setOwner(Player p) {
		this.kit_owner = p;
	}

	protected void setHelmet(ItemStack itemstack) {
		this.helmet = itemstack;
	}

	protected void setChestplate(ItemStack itemstack) {
		this.chestplate = itemstack;
	}

	protected void setLeggings(ItemStack itemstack) {
		this.leggings = itemstack;
	}

	protected void setBoots(ItemStack itemstack) {
		this.boots = itemstack;
	}

	protected void setItems(ArrayList<ItemStack> items) {
		this.items = items;
	}

	protected void setCost(int tokens) {
		this.token_cost = tokens;
	}

	protected void setRankRequired(Rank rank) {
		this.rank_required = rank;
	}

	protected void setDescription(String[] description) {
		this.description = description;
	}

	protected void addItem(ItemStack itemstack) {
		this.items.add(itemstack);
	}

	// Getters
	public String getName() {
		return this.name;
	}

	public String[] getDescription() {
		return this.description;
	}

	public Player getPlayer() {
		return this.kit_owner;
	}

	public ItemStack getHelmet() {
		return this.helmet;
	}

	public ItemStack getChestplate() {
		return this.chestplate;
	}

	public ItemStack getLeggings() {
		return this.leggings;
	}

	public ItemStack getBoots() {
		return this.boots;
	}

	public ArrayList<ItemStack> getItems() {
		return this.items;
	}

	public int getCost() {
		return this.token_cost;
	}

	public Rank getRankRequired() {
		return this.rank_required;
	}

	protected boolean eventAllowed(Player p) {
		if ((PVEManager.getSpawnRegion() != null) && (PVEManager.getSpawnRegion().containsBlock(p.getLocation()))) {
			return false;
		}
		if (!PVEManager.isPVPEnabled(p)) {
			return false;
		}
		if (!p.getName().equals(getPlayer().getName())) {
			return false;
		}
		return true;
	}

	protected boolean eventAllowed(Block block) {
		if ((PVEManager.getSpawnRegion() != null) && (PVEManager.getSpawnRegion().containsBlock(block.getLocation()))) {
			return false;
		}
		return true;
	}

	protected boolean isPlayer(Player p) {
		return p.getName().equals(this.kit_owner.getName());
	}

	protected void customSilentDamage(String cause, Player damagee, Player damager, double dmg) {
		double damg = (dmg < damagee.getHealth()) ? dmg : damagee.getHealth();
		UtilPlayer.setLastDamageCause(damagee, cause);
		UtilPlayer.addDamager(damagee, damager.getName());
		damagee.setHealth(damagee.getHealth() - damg);
	}

	protected void customDamage(String cause, Player damagee, Player damager, double dmg) {
		UtilPlayer.setLastDamageCause(damagee, cause);
		UtilPlayer.addDamager(damagee, damager.getName());
		damagee.damage(dmg);
	}
}
