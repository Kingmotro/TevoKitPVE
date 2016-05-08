package com.tevonetwork.tevokitpve.Kit.kits;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.tevonetwork.tevoapi.API.Cooldown.Cooldown;
import com.tevonetwork.tevoapi.API.Events.PlayerDamageEvent;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevokitpve.Kit.Kit;
import com.tevonetwork.tevokitpve.Kit.NMSUtils;

public class Knight extends Kit{

	public Knight(Player owner)
	{
		super("Knight", 0, owner, new String[] {
				CC.tnInfo + "Oooohh Shiny... The trusty knight kit will never let you down!",
				"",
				CC.tnUse + "Right Click " + CC.tnValue + "Sword " + CC.tnInfo + "to use " + CC.tnAbility + "Slash" + CC.tnInfo + " (Deals " + CC.tnValue + "5 damage " + CC.tnInfo + "to target player, " + CC.tnValue + "5 second " + CC.tnInfo + " Cooldown)",
				CC.tnInfo + "All incoming damage is reduced by " + CC.tnValue + "5%" + CC.end,
				"",
				CC.tnInfo + "Weapons and Armor:",
				CC.tnValue + "Iron Sword, Helmet, Chestplate, Leggings and Boots"
				});
		addItem(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_SWORD)));
		setHelmet(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_HELMET)));
		setChestplate(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_CHESTPLATE)));
		setLeggings(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_LEGGINGS)));
		setBoots(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_BOOTS)));
	}

	@EventHandler
	public void useSlash(PlayerInteractEntityEvent e)
	{
		if (!(e.getRightClicked() instanceof Player))
		{
			return;
		}
		Player p = e.getPlayer();
		if (!eventAllowed(p))
		{
			return;
		}
		if ((p.getItemInHand() == null) || (p.getItemInHand().getType() != Material.IRON_SWORD))
		{
			return;
		}
		if (Cooldown.isPlayeronCooldown(p, "Slash"))
		{
			UtilPlayer.onc(p, "Slash");
			return;
		}
		Player damagee = (Player)e.getRightClicked();
		customDamage("Slash", damagee, p, 5.0);
		damagee.getWorld().spigot().playEffect(damagee.getLocation(), Effect.STEP_SOUND, 152, 0, 1.0F, 1.0F, 1.0F, 0, 7, 20);
		p.getWorld().playSound(p.getLocation(), Sound.SHOOT_ARROW, 2F, 2F);
		UtilPlayer.message(Category.ABILITY, p, CC.tnInfo + "You used " + CC.tnAbility + "Slash" + CC.end);
		Cooldown.addCooldown(p, "Slash", 5);
	}
	
	@EventHandler
	public void damageReduction(PlayerDamageEvent e)
	{
		if (e.getCause() == DamageCause.SUICIDE)
		{
			return;
		}
		double origdmg = e.getDamage();
		double newdmg = origdmg - (0.05 * origdmg);
		e.setDamage(newdmg);
	}
	
}
