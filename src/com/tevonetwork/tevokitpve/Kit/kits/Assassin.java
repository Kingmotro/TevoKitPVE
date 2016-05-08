package com.tevonetwork.tevokitpve.Kit.kits;

import java.util.HashMap;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.ItemStackFactory;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.API.Util.ItemStackFactory.Armor;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.Items;
import com.tevonetwork.tevoapi.Core.Rank;
import com.tevonetwork.tevokitpve.Kit.Kit;
import com.tevonetwork.tevokitpve.Kit.NMSUtils;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;

public class Assassin extends Kit {

	public Assassin(Player owner) {
		super("Assassin", 5000, owner,
				new String[] { CC.tnInfo + "No one will know what hit them with this kit... Literally!", "",
						CC.tnUse + "Right Click " + CC.tnValue + "Stick" + CC.tnInfo + " to use " + CC.tnAbility + "Shuriken" + CC.tnInfo + " (Throws your weapon and deals " + CC.tnValue + "5 damage "
								+ CC.tnInfo + "on hit, also marks the target with" + CC.tnValue + " Mark of the Assassin " + CC.tnInfo + "which after " + CC.tnValue + "3 seconds" + CC.tnInfo
								+ " deals " + CC.tnValue + "4 damage " + CC.tnInfo + "ignoring armor)",
						CC.tnAbility + "Shuriken " + CC.tnInfo + "returns to you once thrown.", CC.tnInfo + "You have a constant speed effect.", "", CC.tnInfo + "Weapons and Armor:",
						CC.tnValue + "Shuriken (Sharpness II)" + CC.comma_ + CC.tnValue + "Black Leather Helmet, Chestplate, Leggings and Boots" });
		setRankRequired(Rank.MYSTIC);
		ItemStackFactory isf = new ItemStackFactory();
		ItemStack shuriken = isf.createItemStack(Items.STICK, CC.tnAbility + "Shuriken");
		shuriken.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
		addItem(NMSUtils.setUnbreakable(shuriken));
		setHelmet(NMSUtils.setUnbreakable(isf.createLeatherArmor(Armor.HELMET, Color.BLACK)));
		setChestplate(NMSUtils.setUnbreakable(isf.createLeatherArmor(Armor.CHESTPLATE, Color.BLACK)));
		setLeggings(NMSUtils.setUnbreakable(isf.createLeatherArmor(Armor.LEGGINGS, Color.BLACK)));
		setBoots(NMSUtils.setUnbreakable(isf.createLeatherArmor(Armor.BOOTS, Color.BLACK)));
	}

	private Item shur;
	private int failed_attempts;
	private boolean thrown;
	private BukkitTask speed;
	private BukkitTask shurik_return;
	private HashMap<String, BukkitTask> marked = new HashMap<String, BukkitTask>();

	@Override
	public void startTasks() {
		this.speed = new BukkitRunnable() {
			@Override
			public void run() {
				PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 120, 1, false, false);
				getPlayer().addPotionEffect(speed, true);
			}
		}.runTaskTimer(main, 0L, 100L);

		this.shurik_return = new BukkitRunnable() {

			@Override
			public void run() {
				if (thrown) {
					failed_attempts++;
					if (failed_attempts >= 120) {
						if (shur != null) {
							shur.remove();
						}
						ItemStack newshuriken = NMSUtils.setUnbreakable(new ItemStackFactory().createItemStack(Items.STICK, CC.tnAbility + "Shuriken"));
						newshuriken.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
						getPlayer().getInventory().setItem(0, newshuriken);
						failed_attempts = 0;
						thrown = false;
						return;
					}
					if ((shur == null) || (shur.isDead())) {
						return;
					}
					Location ploc = getPlayer().getLocation();
					Vector dir = ploc.toVector().subtract(shur.getLocation().toVector()).normalize();
					if (shur.getLocation().getY() < getPlayer().getEyeLocation().getY() && shur.getLocation().getY() < (getPlayer().getEyeLocation().getY() - 0.5)) {
						shur.setVelocity(dir.setY(1.0));
					}
					else {
						shur.setVelocity(dir);
					}

				}
			}
		}.runTaskTimer(main, 0L, 1L);
	};

	@Override
	public void stopTasks() {
		if (this.speed != null) {
			this.speed.cancel();
			getPlayer().removePotionEffect(PotionEffectType.SPEED);
		}
		if (this.shurik_return != null) {
			this.shurik_return.cancel();
		}
		if (this.shur != null) {
			this.shur.remove();
		}
		for (BukkitTask tasks : marked.values()) {
			tasks.cancel();
		}
	};

	@EventHandler
	public void useShuriken(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (!isPlayer(p)) {
			return;
		}
		if ((e.getAction() != Action.RIGHT_CLICK_AIR) && (e.getAction() != Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		if (!eventAllowed(p)) {
			return;
		}
		if (p.getItemInHand() != null) {
			if ((p.getItemInHand().hasItemMeta()) && (p.getItemInHand().getItemMeta().getDisplayName() != null)) {
				if (p.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(CC.tnAbility + "Shuriken")) {
					Snowball shurik = p.launchProjectile(Snowball.class, p.getLocation().getDirection().multiply(3.0));
					shurik.setMetadata("Ability", new FixedMetadataValue(main, "Shuriken"));
					shurik.setMetadata("AbilityOwner", new FixedMetadataValue(main, p.getName()));
					MiscDisguise dis = new MiscDisguise(DisguiseType.DROPPED_ITEM, 280);
					DisguiseAPI.disguiseToAll(shurik, dis);
					p.setItemInHand(null);
					this.thrown = true;
					UtilPlayer.message(Category.ABILITY, p, CC.tnInfo + "You used " + CC.tnAbility + "Shuriken" + CC.end);
				}
			}
		}
	}

	@EventHandler
	public void onShurikenDamage(EntityDamageByEntityEvent e) {
		if (e.isCancelled()) {
			return;
		}
		if (!(e.getEntity() instanceof Player)) {
			return;
		}
		if (!(e.getDamager() instanceof Snowball)) {
			return;
		}
		final Player p = (Player) e.getEntity();
		Snowball shurik = (Snowball) e.getDamager();
		if (!(shurik.getShooter() instanceof Player)) {
			return;
		}
		final Player shooter = (Player) shurik.getShooter();
		if (!eventAllowed(shooter)) {
			return;
		}
		e.setDamage(5.0);
		if (!this.marked.containsKey(p.getName())) {
			UtilPlayer.message(Category.ABILITY, p, CC.tnInfo + "You have been marked with " + CC.tnValue + "Mark of the Assassin" + CC.end);
			p.playSound(p.getLocation(), "mob.guardian.curse", 5F, 1.2F);
			this.marked.put(p.getName(), new BukkitRunnable() {

				@Override
				public void run() {
					customSilentDamage("Mark of the Assassin", p, shooter, 4.0);
					p.playSound(p.getLocation(), Sound.SILVERFISH_HIT, 100F, 1.6F);
					p.getWorld().spigot().playEffect(p.getEyeLocation(), Effect.POTION_SWIRL, 0, 0, 0.2F, 0.2F, 0.2F, 0F, 6, 40);
					marked.remove(p.getName());
				}
			}.runTaskLater(main, 60L));
		}
	}

	@EventHandler
	public void removePlayer(PlayerDeathEvent e) {
		if (this.marked.containsKey(e.getEntity().getName())) {
			this.marked.get(e.getEntity().getName()).cancel();
			this.marked.remove(e.getEntity().getName());
		}
	}

	@EventHandler
	public void onShurikenHit(ProjectileHitEvent e) {
		if (!(e.getEntity() instanceof Snowball)) {
			return;
		}
		Snowball shurik = (Snowball) e.getEntity();
		if (!shurik.hasMetadata("Ability")) {
			return;
		}
		if (!(shurik.getShooter() instanceof Player)) {
			return;
		}
		final Player p = (Player) shurik.getShooter();
		if (!eventAllowed(p)) {
			return;
		}
		if (shurik.getMetadata("Ability").get(0).asString().equalsIgnoreCase("Shuriken")) {
			ItemStack shuriken = NMSUtils.setUnbreakable(new ItemStackFactory().createItemStack(Items.STICK, CC.tnAbility + "Shuriken"));
			shuriken.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
			this.shur = shurik.getLocation().getWorld().dropItem(shurik.getLocation(), shuriken);
			this.shur.setMetadata("AbilityOwner", new FixedMetadataValue(main, p.getName()));
		}
	}

	@EventHandler
	public void onShurikPickup(PlayerPickupItemEvent e) {
		if (e.isCancelled()) {
			return;
		}
		if (e.getItem().getItemStack() == null) {
			return;
		}
		Player p = e.getPlayer();
		ItemStack item = e.getItem().getItemStack();
		if ((item.hasItemMeta()) && (item.getItemMeta().getDisplayName() != null) && (item.getItemMeta().getDisplayName().equalsIgnoreCase(CC.tnAbility + "Shuriken"))) {
			if (e.getItem().hasMetadata("AbilityOwner")) {
				if (!e.getItem().getMetadata("AbilityOwner").get(0).asString().equalsIgnoreCase(p.getName())) {
					e.setCancelled(true);
					return;
				}
			}
			if (p.getName().equals(getPlayer().getName())) {
				this.thrown = false;
				failed_attempts = 0;
			}
		}
	}

}
