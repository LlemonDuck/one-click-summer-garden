package com.duckblade.runelite.summergarden;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.*;

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
	private Notifier notifier;

	@Inject
	private ElementalCollisionDetector collisionDetector;

	@Inject
	private SummerGardenOverlay overlay;

	@Inject
	private SummerGardenConfig config;

	private static final WorldPoint GARDEN = new WorldPoint(2915, 5490, 0);
	private static final String STAMINA_MESSAGE = "[One Click Summer Garden] Low Stamina Warning";
	private static final String CYCLE_MESSAGE = "[One Click Summer Garden] Cycle Ready";

	private boolean sentStaminaNotification = false;
	private boolean sentCycleNotification = false;

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
		
		// cycle notification
		if (config.cycleNotification()) {
			boolean shouldSend = collisionDetector.isLaunchCycle();
			if (shouldSend && !sentCycleNotification)
				notifier.notify(CYCLE_MESSAGE, TrayIcon.MessageType.INFO);
			
			sentCycleNotification = shouldSend;
		}

		// check for stamina usage
		int stamThreshold = config.staminaThreshold();
		if (stamThreshold != 0) {
			boolean stamActive = client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0;
			if (client.getEnergy() <= stamThreshold && !stamActive && !sentStaminaNotification) {
				notifier.notify(STAMINA_MESSAGE, TrayIcon.MessageType.WARNING);
				sentStaminaNotification = true;
			} else if (client.getEnergy() > stamThreshold) {
				sentStaminaNotification = false;
			}
		}
	}

	@Provides
	SummerGardenConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SummerGardenConfig.class);
	}
}
