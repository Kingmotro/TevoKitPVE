package com.tevonetwork.tevokitpve.Kit;

import com.tevonetwork.tevokitpve.Kit.kits.Archer;
import com.tevonetwork.tevokitpve.Kit.kits.Assassin;
import com.tevonetwork.tevokitpve.Kit.kits.Axeman;
import com.tevonetwork.tevokitpve.Kit.kits.Barricade;
import com.tevonetwork.tevokitpve.Kit.kits.Creeper;
import com.tevonetwork.tevokitpve.Kit.kits.Knight;
import com.tevonetwork.tevokitpve.Kit.kits.Runner;
import com.tevonetwork.tevokitpve.Kit.kits.Snowman;
import com.tevonetwork.tevokitpve.Kit.kits.Sorcerer;
import com.tevonetwork.tevokitpve.Kit.kits.Tank;

/*
 * To add new kit simply add enum and add option to get kit.
 */
public enum Kits {

	ARCHER("Archer", false), 
	ASSASSIN("Assassin", false), 
	AXEMAN("Axeman", false), 
	BARRICADE("Barricade", false), 
	CREEPER("Creeper", false), 
	KNIGHT("Knight", true), 
	RUNNER("Runner", false), 
	SNOWMAN("Snowman", false), 
	SORCERER("Sorcerer", false),
	TANK("Tank", false);
	
	
	private final String name;
	private final boolean unlocked;
	
	private Kits(String name, boolean unlocked)
	{
		this.name = name;
		this.unlocked = unlocked;
	}
	
	public static String getName(Kits kit)
	{
		return kit.name;
	}
	
	public static boolean isUnlockedDefault(Kits kit)
	{
		return kit.unlocked;
	}
	
	public static Kit getKit(Kits kit)
	{
		switch(kit){
			case ARCHER:
				return new Archer(null);
			case ASSASSIN:
				return new Assassin(null);
			case AXEMAN:
				return new Axeman(null);
			case BARRICADE:
				return new Barricade(null);
			case CREEPER:
				return new Creeper(null);
			case KNIGHT:
				return new Knight(null);
			case RUNNER:
				return new Runner(null);
			case SNOWMAN:
				return new Snowman(null);
			case TANK:
				return new Tank(null);
			case SORCERER:
				return new Sorcerer(null);
		}
		return null;
	}
	
	public static Kit getKit(String name)
	{
		for (Kits kit : Kits.values())
		{
			if (kit.name.equalsIgnoreCase(name))
			{
				return getKit(kit);
			}
		}
		return null;
	}
	
}
