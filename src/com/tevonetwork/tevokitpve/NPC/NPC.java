package com.tevonetwork.tevokitpve.NPC;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftZombie;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.metadata.FixedMetadataValue;

import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.Core.Rank;
import com.tevonetwork.tevokitpve.TevoKitPVE;
import com.tevonetwork.tevokitpve.Kit.Kit;

public class NPC {

	private TevoKitPVE main = TevoKitPVE.getInstance();
	private Zombie zombie;
	private ArmorStand holo;
	private int holo_id;
	private Kit kit;
	private String name;
	private Location loc;
	
	public NPC(Location loc, Kit kit)
	{
		this.loc = loc;
		this.kit = kit;
	}
	
	public void spawn()
	{
		Chunk chunk = this.loc.getWorld().getChunkAt(this.loc);
		this.loc.getWorld().loadChunk(chunk);
		this.zombie = (Zombie)this.loc.getWorld().spawnEntity(this.loc, EntityType.ZOMBIE);
		this.zombie.setBaby(false);
		this.zombie.setVillager(false);
		Entity nmsen = ((CraftEntity) this.zombie).getHandle();
		NBTTagCompound compound = new NBTTagCompound();
		nmsen.c(compound);
		compound.setByte("NoAI", (byte) 1);
		compound.setByte("Silent", (byte) 1);
		nmsen.f(compound);
		this.holo = (ArmorStand)this.loc.getWorld().spawnEntity(this.loc, EntityType.ARMOR_STAND);
		this.holo.setMetadata("NPC", new FixedMetadataValue(main, this.kit.getName()));
		this.zombie.setPassenger(this.holo);
		this.zombie.setMetadata("NPC", new FixedMetadataValue(main, this.kit.getName()));
		
		EntityArmorStand nms_holo = ((CraftArmorStand)this.holo).getHandle();
		nms_holo.setInvisible(true);
		nms_holo.setSmall(true);
		nms_holo.setGravity(true);
		nms_holo.setBasePlate(false);
		String rank = "";
		if (this.kit.getRankRequired() != null)
		{
			rank = CC.tnUse + " (" + Rank.getRankPrefix(this.kit.getRankRequired()) + CC.tnUse + ")";
		}
		nms_holo.setCustomName(CC.tnAbility + CC.fBold + this.kit.getName() + CC.tnInfo + CC.fBold + " Kit" + rank + CC.tnUse + " (Right Click)");
		nms_holo.setCustomNameVisible(true);
		this.holo_id = nms_holo.getId();
		equipKitStuff();
	}
	
	public void despawn()
	{
		if (this.holo != null)
		{
			((CraftArmorStand)this.holo).getHandle().die();
		}
		if (this.zombie != null)
		{
			((CraftZombie)this.zombie).getHandle().die();
		}
	}
	
	public void reattach()
	{
		if (this.kit.getItems().size() > 0)
		{
			this.zombie.getEquipment().setItemInHand(this.kit.getItems().get(0));
		}
		this.zombie.getEquipment().setHelmet(this.kit.getHelmet());
		this.zombie.getEquipment().setChestplate(this.kit.getChestplate());
		this.zombie.getEquipment().setLeggings(this.kit.getLeggings());
		this.zombie.getEquipment().setBoots(this.kit.getBoots());
	}
	
	private void equipKitStuff()
	{
		if (this.kit.getItems().size() > 0)
		{
			this.zombie.getEquipment().setItemInHand(this.kit.getItems().get(0));
		}
		this.zombie.getEquipment().setHelmet(this.kit.getHelmet());
		this.zombie.getEquipment().setChestplate(this.kit.getChestplate());
		this.zombie.getEquipment().setLeggings(this.kit.getLeggings());
		this.zombie.getEquipment().setBoots(this.kit.getBoots());
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public Kit getKit()
	{
		return this.kit;
	}
	
	public int getHolo_ID()
	{
		return this.holo_id;
	}
	
}
