package com.duckblade.runelite.summergarden;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SummerGardenPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SummerGardenPlugin.class);
		RuneLite.main(args);
	}
}
