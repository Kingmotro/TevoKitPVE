package com.tevonetwork.tevokitpve.Kit.kits;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.ItemStackFactory;
import com.tevonetwork.tevoapi.API.Util.UtilFirework;
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

public class Creeper extends Kit {

	public Creeper(Player owner) {
		super("Creeper", 5000, owner,
				new String[] { CC.tnInfo + "Sssssss... Boom!", "",
						CC.tnUse + "Right Click " + CC.tnInfo + " to charge bow and fire bombs. (Deals " + CC.tnValue + "4 damage" + CC.tnInfo + " to nearby players)",
						CC.tnUse + "Right Click " + CC.tnValue + "Firecharge " + CC.tnInfo + "to use " + CC.tnAbility + "Detonate" + CC.tnInfo + " (Deals " + CC.tnValue + "8 damage " + CC.tnInfo
								+ "to surrounding players but resulting in your death)",
						"", CC.tnInfo + "Weapons and Armor:",
						CC.tnValue + "Wooden Sword (Sharpness II)" + CC.comma_ + CC.tnValue + "Bow" + CC.comma_ + CC.tnValue + "Green Leather Helmet, Chestplate, Leggings and Boots" });
		setRankRequired(Rank.MYSTIC);
		ItemStackFactory isf = new ItemStackFactory();
		ItemStack sword = new ItemStack(Material.WOOD_SWORD);
		sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
		addItem(NMSUtils.setUnbreakable(sword));
		ItemStack bow = new ItemStack(Material.BOW);
		bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
		addItem(NMSUtils.setUnbreakable(bow));
		addItem(NMSUtils.setUnbreakable(isf.createItemStack(Items.FIRECHARGE, CC.tnAbility + "Detonate")));
		addItem(NMSUtils.setUnbreakable(new ItemStack(Material.ARROW)));
		setHelmet(NMSUtils.setUnbreakable(isf.createItemStack(Items.MOBHEADCREEPER, "Creeper Helmet")));
		setChestplate(NMSUtils.setUnbreakable(isf.createLeatherArmor(Armor.CHESTPLATE, Color.GREEN)));
		setLeggings(NMSUtils.setUnbreakable(isf.createLeatherArmor(Armor.LEGGINGS, Color.GREEN)));
		setBoots(NMSUtils.setUnbreakable(isf.createLeatherArmor(Armor.BOOTS, Color.GREEN)));
	}

	private boolean detonating = false;

	@EventHandler
	public void detonate(PlayerInteractEvent e) {
		if ((e.getAction() != Action.RIGHT_CLICK_AIR) && (e.getAction() != Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		final Player p = e.getPlayer();
		if (!eventAllowed(p)) {
			return;
		}
		if (this.detonating) {
			return;
		}
		if ((p.getItemInHand() == null) || (!p.getItemInHand().hasItemMeta())) {
			return;
		}
		if ((p.getItemInHand().getItemMeta().getDisplayName() != null) && (p.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(CC.tnValue + "Detonate"))) {
			this.detonating = true;
			UtilPlayer.message(Category.ABILITY, p, CC.tnInfo + "You used " + CC.tnAbility + "Detonate" + CC.end);
			new BukkitRunnable() {
				int runs = 0;

				@Override
				public void run() {
					if (runs >= 8) {
						UtilPlayer.setLastDamageCause(p, "Detonate");
						Location loc = p.getLocation();
						loc.getWorld().spigot().playEffect(loc, Effect.EXPLOSION_LARGE, 0, 0, 3.0F, 3.0F, 3.0F, 0F, 8, 30);
						loc.getWorld().playSound(loc, Sound.EXPLODE, 4F, 0.8F);
						for (Player vic : UtilPlayer.getinRadius(p, 5)) {
							customDamage("Detonate", vic, p, 8.0);
						}
						p.setHealth(0.0);
						cancel();
						return;
					}
					FireworkEffect effect = FireworkEffect.builder().with(Type.BALL).trail(false).flicker(false).withColor(Color.GREEN).build();
					UtilFirework.playFirework(p.getEyeLocation(), effect);
					runs++;
				}
			}.runTaskTimer(main, 0L, 5L);
		}
	}

	private BukkitTask update;
	private boolean drawn = false;
	private boolean charged = false;
	private int charges;

	@Override
	public void startTasks() {
		this.update = new BukkitRunnable() {

			@Override
			public void run() {
				if (drawn) {
					if (getPlayer().getItemInHand().getType() != Material.BOW) {
						if (charges > 0) {
							getPlayer().setExp(0.0F);
						}
						charges = 0;
						charged = false;
						drawn = false;
						return;
					}
					if (charged) {
						return;
					}
					if (charges >= 40) {
						charged = true;
						getPlayer().playSound(getPlayer().getLocation(), Sound.CLICK, 3F, 1.2F);
						UtilPlayer.message(Category.ABILITY, getPlayer(), CC.tnInfo + "Bow Charged!");
						return;
					}
					float pitch = charges * 0.025F;
					if (pitch > 1.0F) {
						pitch = 1.0F;
					}
					getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.CREEPER_HISS, 3F, (1.0F + pitch));
					getPlayer().setExp((charges * 0.0249999F));
					charges++;
				}
			}
		}.runTaskTimer(main, 0L, 1L);
	}

	@Override
	public void stopTasks() {
		if (this.update != null) {
			this.update.cancel();
		}
	}

	@Override
	public void onUnequip() {
		getPlayer().setExp(0.0F);
	}

	@EventHandler
	public void stopCharge(PlayerDeathEvent e) {
		if (e.getEntity().getName().equals(getPlayer().getName())) {
			this.drawn = false;
			this.charges = 0;
			this.charged = false;
		}
	}

	@EventHandler
	public void bombDraw(PlayerInteractEvent e) {
		if ((e.getAction() != Action.RIGHT_CLICK_AIR) && (e.getAction() != Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		Player p = e.getPlayer();
		if (!isPlayer(p)) {
			return;
		}
		if (!eventAllowed(p)) {
			e.setCancelled(true);
			return;
		}
		if (p.getItemInHand().getType() == Material.BOW) {
			this.drawn = true;
			this.charges = 0;
			this.charged = false;
			getPlayer().setExp(0.0F);
		}
	}

	@EventHandler
	public void bombShoot(EntityShootBowEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}
		if (!(e.getProjectile() instanceof Arrow)) {
			return;
		}
		Player p = (Player) e.getEntity();
		if (!isPlayer(p)) {
			return;
		}
		if (!eventAllowed(p)) {
			e.setCancelled(true);
			return;
		}
		if (this.charged) {
			this.drawn = false;
			this.charges = 0;
			this.charged = false;
			getPlayer().setExp(0.0F);
			Arrow arrow = (Arrow) e.getProjectile();
			Snowball snow = (Snowball) arrow.getWorld().spawnEntity(arrow.getLocation(), EntityType.SNOWBALL);
			snow.setVelocity(arrow.getVelocity());
			snow.setShooter(arrow.getShooter());
			snow.setMetadata("Ability", new FixedMetadataValue(main, "Bomb"));
			MiscDisguise dis = new MiscDisguise(DisguiseType.PRIMED_TNT);
			DisguiseAPI.disguiseToAll(snow, dis);
			arrow.remove();
		}
		else {
			e.setCancelled(true);
			UtilPlayer.message(Category.ABILITY, p, CC.tnInfo + "You need to charge the bow fully to fire bombs!");
			this.drawn = false;
			this.charges = 0;
			this.charged = false;
			getPlayer().setExp(0.0F);
		}
	}

	@EventHandler
	public void bombHit(ProjectileHitEvent e) {
		if (!(e.getEntity() instanceof Snowball)) {
			return;
		}
		if (!(e.getEntity().getShooter() instanceof Player)) {
			return;
		}
		Snowball snow = (Snowball) e.getEntity();
		Player p = (Player) snow.getShooter();
		if (!snow.hasMetadata("Ability")) {
			return;
		}
		if (!snow.getMetadata("Ability").get(0).asString().equalsIgnoreCase("Bomb")) {
			return;
		}
		if (!eventAllowed(snow.getLocation().getBlock())) {
			return;
		}
		if (!eventAllowed(p)) {
			return;
		}
		Location loc = snow.getLocation();
		loc.getWorld().playSound(loc, Sound.EXPLODE, 3F, 1.4F);
		loc.getWorld().spigot().playEffect(loc, Effect.EXPLOSION_LARGE, 0, 0, 1.0F, 1.0F, 1.0F, 0F, 4, 30);
		for (Entity ent : snow.getNearbyEntities(3.0, 3.0, 3.0)) {
			if (ent instanceof Player) {
				Player vic = (Player) ent;
				customDamage("Bomb", vic, p, 4.0);
			}
		}
	}

}
