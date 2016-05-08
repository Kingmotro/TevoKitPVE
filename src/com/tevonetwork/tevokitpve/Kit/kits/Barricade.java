package com.tevonetwork.tevokitpve.Kit.kits;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.tevonetwork.tevoapi.API.Cooldown.Cooldown;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.ItemStackFactory;
import com.tevonetwork.tevoapi.API.Util.ItemStackFactory.Armor;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.Items;
import com.tevonetwork.tevokitpve.Kit.BlockChanging;
import com.tevonetwork.tevokitpve.Kit.Kit;
import com.tevonetwork.tevokitpve.Kit.NMSUtils;
import com.tevonetwork.tevokitpve.PVE.PVEManager;

public class Barricade extends Kit implements BlockChanging {

	public Barricade(Player owner)
	{
		super("Barricade", 5000, owner, new String[]
				{
					CC.tnInfo + "Stop people in their tracks using the " + CC.tnAbility + "Barricade" + CC.tnInfo + " ability which places an iron barricade at the target location for " + CC.tnValue + "10 seconds" + CC.end,
					"",
					CC.tnUse + "Right Click " + CC.tnValue + "Shovel " + CC.tnInfo + "to use " + CC.tnAbility + "Barricade" + CC.tnInfo + " (" + CC.tnValue + "10 second" + CC.tnInfo + " Cooldown" + CC.tnInfo + ")",
					"",
					CC.tnInfo + "Weapons and Armor:",
					CC.tnValue + "Iron Sword, Chestplate and Boots" + CC.comma_ + CC.tnValue + "White Leather Helmet and Leggings"
				});
		ItemStackFactory isf = new ItemStackFactory();
		addItem(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_SWORD)));
		addItem(NMSUtils.setUnbreakable(isf.createItemStack(Items.STONESHOVEL, CC.tnAbility + "Barricade")));
		setHelmet(NMSUtils.setUnbreakable(isf.createLeatherArmor(Armor.HELMET, Color.WHITE)));
		setLeggings(NMSUtils.setUnbreakable(isf.createLeatherArmor(Armor.LEGGINGS, Color.WHITE)));
		setChestplate(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_CHESTPLATE)));
		setBoots(NMSUtils.setUnbreakable(new ItemStack(Material.IRON_BOOTS)));
	}
	
	@EventHandler
	public void useBarricade(PlayerInteractEvent e)
	{
		if ((e.getAction() != Action.RIGHT_CLICK_AIR) && (e.getAction() != Action.RIGHT_CLICK_BLOCK))
		{
			return;
		}
		Player p = e.getPlayer();
		if (!p.getName().equals(getPlayer().getName()))
		{
			return;
		}
		if (!PVEManager.isPVPEnabled(p))
		{
			return;
		}
		if ((p.getItemInHand() != null) && (p.getItemInHand().getType() == Material.STONE_SPADE))
		{
			if (Cooldown.isPlayeronCooldown(p, "Barricade"))
			{
				UtilPlayer.onc(p, "Barricade");
				return;
			}
			Set<Material> trans = new HashSet<Material>();
			trans.add(Material.AIR);
			trans.add(Material.LONG_GRASS);
			trans.add(Material.YELLOW_FLOWER);
			trans.add(Material.BARRIER);
			trans.add(Material.CROPS);
			trans.add(Material.DETECTOR_RAIL);
			trans.add(Material.POWERED_RAIL);
			trans.add(Material.RAILS);
			trans.add(Material.ACTIVATOR_RAIL);
			trans.add(Material.TORCH);
			trans.add(Material.TRAP_DOOR);
			trans.add(Material.DEAD_BUSH);
			trans.add(Material.SAPLING);
			Location loc = p.getTargetBlock(trans, 10).getRelative(BlockFace.UP).getLocation();
			if ((PVEManager.getSpawnRegion() != null) && (PVEManager.getSpawnRegion().containsBlock(loc)))
			{
				UtilPlayer.message(Category.ABILITY, p, CC.tnError + "You cannot place a barricade in the spawn area!");
				return;
			}
			spawnBarrier(loc);
			UtilPlayer.message(Category.ABILITY, p, CC.tnInfo + "You used " + CC.tnAbility + "Barricade" + CC.end);
			Cooldown.addCooldown(p, "Barricade", 10);
		}
		
	}
	
	private void spawnBarrier(Location loc)
	{
		ArrayList<Block> preblocks = new ArrayList<Block>();
		ArrayList<Block> blocks = new ArrayList<Block>();
		Block block = loc.getBlock();
		if (block.getType() == Material.AIR)
		{
			blocks.add(block);
		}
		Block iron = null;
		iron = block.getRelative(BlockFace.NORTH);
		preblocks.add(iron);
		iron = block.getRelative(BlockFace.SOUTH);
		preblocks.add(iron);
		iron = block.getRelative(BlockFace.EAST);
		preblocks.add(iron);
		iron = block.getRelative(BlockFace.WEST);
		preblocks.add(iron);
		block = block.getRelative(BlockFace.UP);
		preblocks.add(block);
		iron = block.getRelative(BlockFace.NORTH);
		preblocks.add(iron);
		iron = block.getRelative(BlockFace.SOUTH);
		preblocks.add(iron);
		iron = block.getRelative(BlockFace.EAST);
		preblocks.add(iron);
		iron = block.getRelative(BlockFace.WEST);
		preblocks.add(iron);
		block = block.getRelative(BlockFace.UP);
		preblocks.add(block);
		iron = block.getRelative(BlockFace.NORTH);
		preblocks.add(iron);
		iron = block.getRelative(BlockFace.SOUTH);
		preblocks.add(iron);
		iron = block.getRelative(BlockFace.EAST);
		preblocks.add(iron);
		iron = block.getRelative(BlockFace.WEST);
		preblocks.add(iron);
		
		Iterator<Block> itr = preblocks.iterator();
		while(itr.hasNext())
		{
			Block itrblock = itr.next();
			if (itrblock.getType() == Material.AIR)
			{
				blocks.add(itrblock);
			}
		}
		
		for (Block tochange : blocks)
		{
			states.add(tochange.getState());
		}
		for (Block set : blocks)
		{
			set.setType(Material.IRON_BLOCK);
		}
		block.getWorld().playSound(block.getLocation(), Sound.ANVIL_LAND, 3F, 0.5F);
		block.getWorld().spigot().playEffect(block.getLocation(), Effect.CRIT, 0, 0, 2.0F, 2.0F, 2.0F, 0, 30, 20);
		despawnBarrier();
		
	}
	
	private ArrayList<BlockState> states = new ArrayList<BlockState>();
	
	private void despawnBarrier()
	{
		new BukkitRunnable() {
			
			@Override
			public void run()
			{
				for (BlockState torestore : states)
				{
					torestore.update(true);
				}
				states.clear();
			}
		}.runTaskLater(main, 200L);
	}

	@Override
	public ArrayList<BlockState> getAffectedBlocks()
	{
		return this.states;
	}

}
