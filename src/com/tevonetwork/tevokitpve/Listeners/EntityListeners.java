package com.tevonetwork.tevokitpve.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityListeners implements Listener{

	@EventHandler
	public void onEntityCombust(EntityCombustEvent e)
	{
		if (e.getEntity().hasMetadata("NPC"))
		{
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e)
	{
		if (e.getEntity().hasMetadata("NPC"))
		{
			e.setCancelled(true);
		}
	}
	
}
