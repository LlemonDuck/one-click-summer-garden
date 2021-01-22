package com.duckblade.runelite.summergarden;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "One Click Summer Garden"
)
public class SummerGardenPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ElementalCollisionDetector collisionDetector;

	@Inject
	private SummerGardenOverlay overlay;

	private static final WorldPoint GARDEN = new WorldPoint(2915, 5490, 0);

	@Override
	protected void startUp() throws Exception
	{
		enableOverlay();
	}

	@Override
	protected void shutDown() throws Exception
	{
		disableOverlay();
	}

	private boolean overlayEnabled = false;
	private void enableOverlay() {
		if (overlayEnabled)
			return;

		overlayEnabled = true;
		overlayManager.add(overlay);
	}

	private void disableOverlay() {
		if (overlayEnabled)
			overlayManager.remove(overlay);
		overlayEnabled = false;
	}

	@Subscribe
	public void onGameTick(GameTick e) {
		Player p = client.getLocalPlayer();
		if (p == null)
			return;

		if (p.getWorldLocation().distanceTo2D(GARDEN) >= 50) {
			disableOverlay();
			return;
		}

		enableOverlay();
		client.getNpcs()
			.stream()
			.filter(ElementalCollisionDetector::isSummerElemental)
			.forEach(npc -> collisionDetector.updatePosition(npc, client.getTickCount()));
	}

	@Provides
	SummerGardenConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SummerGardenConfig.class);
	}
}
