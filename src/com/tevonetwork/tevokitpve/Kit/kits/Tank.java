package com.tevonetwork.tevokitpve.Kit.kits;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.tevonetwork.tevoapi.API.Cooldown.Cooldown;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.UtilBlock;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevokitpve.Kit.Kit;
import com.tevonetwork.tevokitpve.Kit.NMSUtils;

public class Tank extends Kit{

	public Tank(Player owner)
	{
		super("Tank", 1500, owner, new String[]
				{
					CC.tnInfo + "Wear the strongest armor in the realm and knock other players round!",
					"",
					CC.tnUse + "Right Click " + CC.tnValue + "Sword" + CC.tnInfo + " to use " + CC.tnAbility + "Floor Smash "
					+ CC.tnInfo + "(Deals " + CC.tnValue + "4 damage" + CC.tnInfo + " to nearby players and throws them into "
					+ "the air " + CC.tnValue + "10 second " + CC.tnInfo + "cooldown)",
					"",
					CC.tnInfo + "Weapons and Armor:",
					CC.tnValue + "Stone Sword" + CC.comma_ + CC.tnValue + "Diamond Helmet, Chestplate, Leggings and Boots"
				});
		addItem(NMSUtils.setUnbreakable(new ItemStack(Material.STONE_SWORD)));
		setHelmet(NMSUtils.setUnbreakable(new ItemStack(Material.DIAMOND_HELMET)));
		setChestplate(NMSUtils.setUnbreakable(new ItemStack(Material.DIAMOND_CHESTPLATE)));
		setLeggings(NMSUtils.setUnbreakable(new ItemStack(Material.DIAMOND_LEGGINGS)));
		setBoots(NMSUtils.setUnbreakable(new ItemStack(Material.DIAMOND_BOOTS)));
	}
	
	@Override
	protected void onUnequip() 
	{
		Cooldown.removePlayerCooldown(getPlayer(), "Floor Smash");
	}
	
	@EventHandler
	public void floorSmash(PlayerInteractEvent e)
	{
		if ((e.getAction() != Action.RIGHT_CLICK_AIR) && (e.getAction() != Action.RIGHT_CLICK_BLOCK))
		{
			return;
		}
		final Player p = e.getPlayer();
		if (!eventAllowed(p))
		{
			return;
		}
		if (Cooldown.isPlayeronCooldown(p, "Floor Smash"))
		{
			UtilPlayer.onc(p, "Floor Smash");
			return;
		}
		if (p.getItemInHand().getType() == Material.STONE_SWORD)
		{
			Cooldown.addCooldown(p, "Floor Smash", 10);
			UtilPlayer.message(Category.ABILITY, p, CC.tnInfo + "You used " + CC.tnAbility + "Floor Smash" + CC.end);
			p.setVelocity(new Vector(0, 1.5, 0));
			p.playSound(p.getLocation(), Sound.HORSE_ARMOR, 5F, 1.2F);
			new BukkitRunnable() {
				
				@Override
				public void run()
				{
					p.setVelocity(new Vector(0, -1.5, 0));
				}
			}.runTaskLater(main, 20L);
			
			new BukkitRunnable() {
				
				@SuppressWarnings("deprecation")
				@Override
				public void run()
				{
					if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR)
					{
						cancel();
						p.setFallDistance(0.0F);
						Block center = p.getLocation().getBlock().getRelative(BlockFace.DOWN);
						p.getWorld().playSound(p.getLocation(), Sound.ZOMBIE_WOOD, 5.0F, 0.8F);
						for (Player ent : UtilPlayer.getinRadius(p, 5))
						{
							customDamage("Floor Smash", ent, p, 4.0);
							ent.setVelocity(ent.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(2.0).setY(1.2));
						}
						
						for (Block bl : UtilBlock.getSurrounding(center, 6))
						{
							if (bl.getType() != Material.AIR)
							{
								FallingBlock fb = bl.getWorld().spawnFallingBlock(bl.getLocation().add(0, 1.0, 0), bl.getTypeId(), bl.getData());
								fb.setDropItem(false);
								fb.setVelocity(fb.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.8));
							}
						}
					}
				}
			}.runTaskTimer(main, 20L, 1L);
		}
	}
	
}
