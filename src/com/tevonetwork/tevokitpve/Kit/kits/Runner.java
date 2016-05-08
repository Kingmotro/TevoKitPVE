package com.tevonetwork.tevokitpve.Kit.kits;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.tevonetwork.tevoapi.API.Cooldown.Cooldown;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.ItemStackFactory;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.Items;
import com.tevonetwork.tevokitpve.Kit.Kit;
import com.tevonetwork.tevokitpve.Kit.NMSUtils;

public class Runner extends Kit {

	public Runner(Player owner) {
		super("Runner", 3000, owner, new String[] { CC.tnInfo + "Use the hyper runner kit to take out your foes with a touch of speed.", "",
				CC.tnUse + "Right Click " + CC.tnValue + "Sugar " + CC.tnInfo + "to use " + CC.tnAbility + "Sugar Rush " + CC.tnInfo
						+ "(Gives you additional speed and adds knockback to your sword for " + CC.tnValue + "4 seconds " + CC.tnInfo + ", " + CC.tnValue + "25 second cooldown" + CC.tnInfo + ")",
				CC.tnInfo + "You have a constant speed effect.", "", CC.tnInfo + "Weapons and Armor:",
				CC.tnValue + "Iron Helmet, Chestplate, Leggings and Boots" + CC.comma_ + CC.tnValue + "Stone Sword (Sharpness I)" });
		ItemStack sword = new ItemStack(Material.STONE_SWORD);
		sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		addItem(NMSUtils.setUnbreakable(sword));
		addItem(new ItemStackFactory().createItemStack(Items.SUGAR, CC.tnAbility + "Sugar Rush"));
		setHelmet(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_HELMET)));
		setChestplate(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_CHESTPLATE)));
		setLeggings(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_LEGGINGS)));
		setBoots(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_BOOTS)));
	}

	private BukkitTask reset;
	private BukkitTask speed;
	private boolean hyper = false;

	@Override
	public void startTasks() {
		this.speed = new BukkitRunnable() {

			@Override
			public void run() {
				if (hyper) {
					return;
				}
				PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 120, 0, false, false);
				getPlayer().addPotionEffect(speed, true);
			}
		}.runTaskTimer(main, 0L, 100L);
	}

	@Override
	public void stopTasks() {
		if (this.reset != null) {
			this.reset.cancel();
		}
		if (this.speed != null) {
			this.speed.cancel();
		}
		getPlayer().removePotionEffect(PotionEffectType.SPEED);
	}

	@Override
	protected void onUnequip() {
		Cooldown.removePlayerCooldown(getPlayer(), "Sugar Rush");
	}

	@EventHandler
	public void sugarRush(PlayerInteractEvent e) {
		if ((e.getAction() != Action.RIGHT_CLICK_AIR) && (e.getAction() != Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		Player p = e.getPlayer();
		if (!eventAllowed(p)) {
			return;
		}
		if (p.getItemInHand().getType() != Material.SUGAR) {
			return;
		}
		if (!p.getItemInHand().hasItemMeta()) {
			return;
		}
		if ((p.getItemInHand().getItemMeta().getDisplayName() != null) && (p.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(CC.tnAbility + "Sugar Rush"))) {
			if (Cooldown.isPlayeronCooldown(p, "Sugar Rush")) {
				UtilPlayer.onc(p, "Sugar Rush");
				return;
			}
			for (ItemStack is : p.getInventory().getContents()) {
				if ((is != null) && (is.getType() == Material.STONE_SWORD)) {
					is.addEnchantment(Enchantment.KNOCKBACK, 2);
					p.updateInventory();
				}
			}
			Cooldown.addCooldown(p, "Sugar Rush", 25);
			PotionEffect speedboost = new PotionEffect(PotionEffectType.SPEED, 80, 2);
			p.addPotionEffect(speedboost, true);
			this.hyper = true;
			p.playSound(p.getLocation(), Sound.BURP, 5.0F, 1.0F);
			p.playSound(p.getLocation(), Sound.FIREWORK_LAUNCH, 5.0F, 2.0F);
			UtilPlayer.message(Category.ABILITY, p, CC.tnInfo + "You used " + CC.tnAbility + "Sugar Rush" + CC.end);
			new BukkitRunnable() {
				@Override
				public void run() {
					for (ItemStack is : getPlayer().getInventory().getContents()) {
						if ((is != null) && (is.getType() == Material.STONE_SWORD)) {
							is.removeEnchantment(Enchantment.KNOCKBACK);
							getPlayer().updateInventory();
						}
					}
					getPlayer().removePotionEffect(PotionEffectType.SPEED);
					PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 120, 0, false, false);
					getPlayer().addPotionEffect(speed);
					hyper = false;
				}
			}.runTaskLater(main, 80L);
		}
	}

}
