package com.tevonetwork.tevokitpve.Kit.kits;

import java.util.HashMap;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevokitpve.Kit.Kit;
import com.tevonetwork.tevokitpve.Kit.KitManager;
import com.tevonetwork.tevokitpve.Kit.NMSUtils;

public class Axeman extends Kit{

	public Axeman(Player owner)
	{
		super("Axeman", 6000, owner, new String[] {
				CC.tnInfo + "Gain axes and slaughter your enemies either from range or melee.",
				"",
				CC.tnUse + "Right Click " + CC.tnAbility + "Axe " + CC.tnInfo + "to use " + CC.tnAbility + "Throwing Axe" + CC.tnInfo + " (Deals " + CC.tnValue + "8 damage " 
				+ CC.tnInfo + "on hit and applies " + CC.tnValue + "Slowness I" + CC.tnInfo + " and " + CC.tnValue + "Bleeding" + CC.tnInfo + " on the damaged player for" + CC.tnValue + " 3 seconds" + CC.tnInfo + ")",
				CC.tnInfo + "You recieve " + CC.tnAbility + "1 Axe" + CC.tnInfo + " every " + CC.tnValue + "15 seconds" + CC.tnInfo + " (Maximum of " + CC.tnValue + "15" + CC.tnInfo + " axes at once)",
				CC.tnAbility + "Bleeding" + CC.tnInfo + " deals " + CC.tnValue + "6 damage " + CC.tnInfo + "over 3 seconds.",
				CC.tnInfo + "You are immune to " + CC.tnAbility + "Throwing Axe" + CC.end,
				CC.tnInfo + "You take " + CC.tnValue + "20%" + CC.tnInfo + " less damage from melee attacks involving axes.",
				"",
				CC.tnInfo + "Weapons and Armor:",
				CC.tnValue + "Iron Axe, Chestplate, Leggings and Boots" + CC.comma_ + CC.tnValue + "Diamond Helmet"
		});
		addItem(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_AXE)));
		setHelmet(NMSUtils.setUnbreakable(new ItemStack(Material.DIAMOND_HELMET)));
		setChestplate(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_CHESTPLATE)));
		setLeggings(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_LEGGINGS)));
		setBoots(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_BOOTS)));
	}
	
	@EventHandler
	public void useThrowAxe(PlayerInteractEvent e)
	{
		if ((e.getAction() != Action.RIGHT_CLICK_AIR) && (e.getAction() != Action.RIGHT_CLICK_BLOCK))
		{
			return;
		}
		Player p = e.getPlayer();
		if (!eventAllowed(p))
		{
			return;
		}
		if ((p.getItemInHand() != null) && (p.getItemInHand().getType() == Material.IRON_AXE))
		{
			p.setItemInHand(null);
			Snowball axe = p.launchProjectile(Snowball.class);
			MiscDisguise dis = new MiscDisguise(DisguiseType.DROPPED_ITEM, 258);
			DisguiseAPI.disguiseToAll(axe, dis);
			axe.setMetadata("Ability", new FixedMetadataValue(main, "Throwing Axe"));
			axe.setMetadata("AbilityOwner", new FixedMetadataValue(main, p.getName()));
			UtilPlayer.message(Category.ABILITY, p, CC.tnInfo + "You used " + CC.tnAbility + "Throwing Axe" + CC.end);
		}
	}
	
	@EventHandler
	public void damageEvent(EntityDamageByEntityEvent e)
	{
		if (!(e.getEntity() instanceof Player))
		{
			return;
		}
		final Player p = (Player)e.getEntity();
		if (!eventAllowed(p))
		{
			return;
		}
		if ((e.getDamager() instanceof Snowball) && (e.getDamager().hasMetadata("Ability")))
		{
			Snowball axe = (Snowball)e.getDamager();
			if ((axe.getShooter() instanceof Player) && (axe.getMetadata("Ability").get(0).asString().equalsIgnoreCase("Throwing Axe")))
			{
				final Player shooter = (Player)axe.getShooter();
				if (!eventAllowed(shooter))
				{
					return;
				}
				if ((KitManager.getKit(p) != null) && (KitManager.getKit(p) instanceof Axeman))
				{
					return;
				}
				shooter.playSound(shooter.getLocation(), Sound.ITEM_BREAK, 2F, 0.8F);
				bleeding.put(p, new BukkitRunnable() {
					int counter = 0;
					@Override
					public void run()
					{
						customDamage("Bleed", p, shooter, 2.0);
						if (counter >= 2)
						{
							bleeding.remove(p);
							cancel();
						}
						counter++;
					}
				}.runTaskTimer(main, 0L, 20L));
				PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 60, 0);
				p.addPotionEffect(slow, true);
				e.setDamage(8.0);
				p.getWorld().spigot().playEffect(p.getLocation(), Effect.STEP_SOUND, 152, 0, 0.5F, 1.2F, 0.5F, 0, 4, 30);
				
			}
		}
		if (e.getDamager() instanceof Player)
		{
			Player damager = (Player)e.getDamager();
			if (!eventAllowed(damager))
			{
				return;
			}
			if ((KitManager.getKit(p) != null) && (KitManager.getKit(p) instanceof Axeman))
			{
				if (damager.getItemInHand() != null)
				{
					if ((damager.getItemInHand().getType() == Material.WOOD_AXE) || (damager.getItemInHand().getType() == Material.STONE_AXE)
							|| (damager.getItemInHand().getType() == Material.IRON_AXE) || (damager.getItemInHand().getType() == Material.DIAMOND_AXE))
					{
						double originaldmg = e.getDamage();
						double newdmg = originaldmg - (0.2 * originaldmg);
						e.setDamage(newdmg);
					}
				}
			}
		}
	}
	
	private BukkitTask axegain;
	private HashMap<Player, BukkitTask> bleeding = new HashMap<Player, BukkitTask>();
	
	@EventHandler
	public void stopBleed(PlayerQuitEvent e)
	{
		if (this.bleeding.containsKey(e.getPlayer()))
		{
			this.bleeding.get(e.getPlayer()).cancel();
			this.bleeding.remove(e.getPlayer());
		}
	}
	
	@Override
	public void startTasks()
	{
		this.axegain = new BukkitRunnable() {
			
			@Override
			public void run()
			{
				int size = 0;
				for (ItemStack is : getPlayer().getInventory().getContents())
				{
					if ((is != null) && (is.getType() != Material.AIR))
					{
						size++;
					}
				}
				if (size < 15)
				{
					UtilPlayer.addItem(getPlayer(), NMSUtils.setUnbreakable(new ItemStack(Material.IRON_AXE)));
				}
			}
		}.runTaskTimer(main, 300L, 300L);
	}
	
	@Override
	public void stopTasks()
	{
		if (axegain == null)
		{
			return;
		}
		this.axegain.cancel();
	}

}
