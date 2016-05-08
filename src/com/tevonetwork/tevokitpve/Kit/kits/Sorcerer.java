package com.tevonetwork.tevokitpve.Kit.kits;

import org.bukkit.entity.Player;

import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.Core.Rank;
import com.tevonetwork.tevokitpve.Kit.Kit;

public class Sorcerer extends Kit{

	public Sorcerer(Player owner) {
		super("Sorcerer", 9000, owner, new String[] {
			CC.tnInfo + "Are you the most powerful in all the realm? We'll find out!",
			"",
			CC.tnUse + "Right Click " + CC.tnValue + "Wand " + CC.tnInfo + "to use " + CC.tnAbility + "Basic Attack Spell" + CC.tnInfo + " (Deals " + CC.tnValue + "5 damage " + CC.tnInfo + "to target player, " + CC.tnValue + "5 second " + CC.tnInfo + " Cooldown)",
			CC.tnInfo + "All incoming damage is reduced by " + CC.tnValue + "5%" + CC.end,
			"",
			CC.tnInfo + "Weapons and Armor:",
			CC.tnValue + "Iron Sword, Helmet, Chestplate, Leggings and Boots"
		});
		setRankRequired(Rank.CRYSTAL);
		
	}

}
