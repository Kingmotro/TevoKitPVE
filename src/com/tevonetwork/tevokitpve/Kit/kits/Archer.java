package com.tevonetwork.tevokitpve.Kit.kits;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevokitpve.Kit.Kit;
import com.tevonetwork.tevokitpve.Kit.KitManager;
import com.tevonetwork.tevokitpve.Kit.NMSUtils;

public class Archer extends Kit {

	public Archer(Player owner) {
		super("Archer", 1000, owner,
				new String[] { CC.tnInfo + "Get some target practice with the archer kit!", "",
						CC.tnInfo + "Every " + CC.tnAbility + "5" + CC.tnInfo + " successful hits activates " + CC.tnAbility + "Explosive Shot" + CC.tnInfo + " (Deals " + CC.tnValue + "5 damage"
								+ CC.tnInfo + " extra and explodes)",
						CC.tnInfo + "You take " + CC.tnValue + "20%" + CC.tnInfo + " less damage from arrow attacks.", "", CC.tnInfo + "Other Weapons/Armor:", CC.tnValue + "Bow (Infinity)" + CC.comma_
								+ CC.tnValue + "Leather Helmet, Chestplate, Leggings and Boots (Protection I)" + CC.comma_ + CC.tnValue + "1 Arrow" + CC.comma_ + CC.tnValue + "Stone Sword" });
		ItemStack bow = new ItemStack(Material.BOW);
		bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
		addItem(NMSUtils.setUnbreakable(bow));
		addItem(NMSUtils.setUnbreakable(new ItemStack(Material.STONE_SWORD)));
		addItem(NMSUtils.setUnbreakable(new ItemStack(Material.ARROW)));
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
		helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
		chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
		leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
		boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		setHelmet(NMSUtils.setUnbreakable(helmet));
		setChestplate(NMSUtils.setUnbreakable(chestplate));
		setLeggings(NMSUtils.setUnbreakable(leggings));
		setBoots(NMSUtils.setUnbreakable(boots));
	}
	
	private int shots = 0;
	private boolean explode = false;

	@EventHandler
	public void arrowhit(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Arrow)) {
			return;
		}
		if (!(e.getEntity() instanceof Player)) {
			return;
		}
		Player p = (Player) e.getEntity();
		Arrow arrow = (Arrow) e.getDamager();
		if (!(arrow.getShooter() instanceof Player)) {
			return;
		}
		Player shooter = (Player) arrow.getShooter();
		if (shooter.getName().equals(getPlayer().getName())) {
			if (this.explode) {
				arrow.getLocation().getWorld().playEffect(arrow.getLocation(), Effect.EXPLOSION_LARGE, 0, 50);
				arrow.getLocation().getWorld().playSound(arrow.getLocation(), Sound.EXPLODE, 5F, 1F);
				e.setDamage(e.getDamage() + 5.0);
				this.shots = 0;
				this.explode = false;
			}
			else {
				this.shots++;
				if (this.shots >= 5) {
					UtilPlayer.message(Category.ABILITY, shooter, CC.tnAbility + "Explosive Shot" + CC.tnInfo + " activated!");
					shooter.playSound(p.getLocation(), Sound.ORB_PICKUP, 2F, 1F);
					this.explode = true;
				}
			}
		}
		if (KitManager.hasKit(p)) {
			if (KitManager.getKit(p) instanceof Archer) {
				double damage = e.getDamage();
				double newdamage = damage - (damage * 0.2);
				e.setDamage(newdamage);
			}
		}
	}

}
