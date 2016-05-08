package com.tevonetwork.tevokitpve.PVE;

import com.tevonetwork.tevoapi.API.Util.CC;

public enum KillStreakType
{
	DOUBLE(CC.cRED + CC.fBold + "Double Kill"), TRIPLE(CC.cD_Purple + CC.fBold + "Triple Kill"), QUADRA(CC.cD_Blue + CC.fBold + "Quadra Kill"), PENTA(CC.cGold + CC.fBold + "Penta Kill");
	
	private final String string;
	
	private KillStreakType(String string)
	{
		this.string = string;
	}
	
	public static String toString(KillStreakType type)
	{
		return type.string;
	}
}
