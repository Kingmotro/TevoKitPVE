package com.tevonetwork.tevokitpve.Kit.kits;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.tevonetwork.tevoapi.API.Cooldown.Cooldown;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.ItemStackFactory;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.API.Util.ItemStackFactory.Armor;
import com.tevonetwork.tevoapi.API.Util.UtilEnt;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.Items;
import com.tevonetwork.tevokitpve.Kit.Kit;
import com.tevonetwork.tevokitpve.Kit.KitManager;
import com.tevonetwork.tevokitpve.Kit.NMSUtils;
import com.tevonetwork.tevokitpve.PVE.PVEManager;

public class Snowman extends Kit{

	public Snowman(Player owner)
	{
		super("Snowman", 6500, owner, new String[] {
				CC.tnInfo + "Knock health off your enemies with the " + CC.tnAbility + "Snowbomb " + CC.tnInfo + "ability!",
				"",
				CC.tnUse + "Right Click " + CC.tnValue + "Snowball" + CC.tnInfo + " to use " + CC.tnAbility + "Snowbomb" + CC.tnInfo + " (" + CC.tnValue + "5 second " + CC.tnInfo + "Cooldown, Deals " + CC.tnValue + "6 damage " + CC.tnInfo + "to players within a " + CC.tnValue + "2" + CC.tnInfo + " block radius)",
				CC.tnInfo + "You take no damage from " + CC.tnAbility + "Snowbomb",
				"",
				CC.tnInfo + "Weapons and Armor:",
				CC.tnValue + "Wooden Sword (Sharpness I)" + CC.comma_ + CC.tnValue + "Orange Leather Helmet" + CC.comma_ + CC.tnValue + "White Leather Chestplate, Leggings and Boots"
		});
		ItemStackFactory isf = new ItemStackFactory();
		ItemStack sword = new ItemStack(Material.WOOD_SWORD);
		sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		addItem(NMSUtils.setUnbreakable(sword));
		addItem(NMSUtils.setUnbreakable(isf.createItemStack(Items.SNOWBALL, CC.tnAbility + "Snowbomb")));
		setHelmet(NMSUtils.setUnbreakable(isf.createLeatherArmor(Armor.HELMET, Color.ORANGE)));
		setChestplate(NMSUtils.setUnbreakable(isf.createLeatherArmor(Armor.CHESTPLATE, Color.WHITE)));
		setLeggings(NMSUtils.setUnbreakable(isf.createLeatherArmor(Armor.LEGGINGS, Color.WHITE)));
		setBoots(NMSUtils.setUnbreakable(isf.createLeatherArmor(Armor.BOOTS, Color.WHITE)));
	}
	
	@EventHandler
	public void useSnowbomb(PlayerInteractEvent e)
	{
		if ((e.getAction() != Action.RIGHT_CLICK_AIR) && (e.getAction() != Action.RIGHT_CLICK_BLOCK))
		{
			return;
		}
		Player p = e.getPlayer();
		if (!isPlayer(p))
		{
			return;
		}
		if (!eventAllowed(p))
		{
			e.setCancelled(true);
			p.updateInventory();
			return;
		}
		if ((p.getItemInHand() != null) && (p.getItemInHand().getType() == Material.SNOW_BALL))
		{
			if (Cooldown.isPlayeronCooldown(p, "Snowbomb"))
			{
				UtilPlayer.onc(p, "Snowbomb");
				e.setCancelled(true);
				p.updateInventory();
				return;
			}
			e.setCancelled(true);
			p.updateInventory();
			Snowball bomb = p.launchProjectile(Snowball.class);
			bomb.setMetadata("Ability", new FixedMetadataValue(main, "Snowbomb"));
			bomb.setMetadata("AbilityOwner", new FixedMetadataValue(main, p.getName()));
			UtilPlayer.message(Category.ABILITY, p, CC.tnInfo + "You used " + CC.tnAbility + "Snowbomb" + CC.end);
			Cooldown.addCooldown(p, "Snowbomb", 5);
		}
	}
	
	@EventHandler
	public void kaboom(ProjectileHitEvent e)
	{
		if (!(e.getEntity() instanceof Snowball))
		{
			return;
		}
		if (!(e.getEntity().getShooter() instanceof Player))
		{
			return;
		}
		Snowball bomb = (Snowball)e.getEntity();
		if (!bomb.hasMetadata("Ability"))
		{
			return;
		}
		if (!bomb.getMetadata("Ability").get(0).asString().equalsIgnoreCase("Snowbomb"))
		{
			return;
		}
		if (!eventAllowed(bomb.getLocation().getBlock()))
		{
			return;
		}
		Player shooter = (Player)bomb.getShooter();
		bomb.getWorld().playSound(bomb.getLocation(), Sound.FIREWORK_BLAST, 2.0F, 0.8F);
		bomb.getWorld().spigot().playEffect(bomb.getLocation(), Effect.SNOWBALL_BREAK, 0, 0, 2.0F, 0.5F, 2.0F, 0.0F, 30, 20);
		for (Player dmg : UtilEnt.getinRadius(bomb, 3))
		{
			if ((KitManager.getKit(dmg) != null) && (!(KitManager.getKit(dmg) instanceof Snowman)) && (PVEManager.isPVPEnabled(dmg)))
			{
				customDamage("Snowbomb", dmg, shooter, 6.0);
			}
		}
	}

}
