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
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.*;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

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
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private Notifier notifier;

	@Inject
	private ElementalCollisionDetector collisionDetector;

	@Inject
	private SummerGardenOverlay overlay;

	@Inject
	private SummerGardenConfig config;

	public static final String CONFIG_GROUP = "oneclicksummergarden";
	public static final String CONFIG_KEY_GATE_START = "useGateStartPoint";
	public static final String CONFIG_KEY_COUNTDOWN_TIMER_INFOBOX = "showCountdownTimer";
	public static final String CONFIG_KEY_RACE_STYLE_COUNTDOWN = "raceStyleCountdown";
	public static final String CONFIG_KEY_RACE_STYLE_VOLUME = "raceStyleVolume";
	private static final WorldPoint GARDEN = new WorldPoint(2915, 5490, 0);
	private static final String STAMINA_MESSAGE = "[One Click Summer Garden] Low Stamina Warning";
	private static final String CYCLE_MESSAGE = "[One Click Summer Garden] Cycle Ready";
	private static final int SUMMER_SQUIRK_ITEM_ID = 10845;
	private static final int RACE_STYLE_SOUND_LOW = 3817;
	private static final int RACE_STYLE_SOUND_HIGH = 3818;

	private InfoBox countdownTimerInfoBox;
	private boolean sentStaminaNotification = false;

	@Override
	protected void startUp() throws Exception
	{
		enableOverlay();
		if (config.showCountdownTimer())
		{
			enableCountdownTimerInfoBox();
		}
		collisionDetector.setGateStart(config.useGateStartPoint());
	}

	@Override
	protected void shutDown() throws Exception
	{
		disableOverlay();
		disableCountdownTimerInfoBox();
	}

	private boolean overlayEnabled = false;

	private void enableOverlay()
	{
		if (overlayEnabled)
		{
			return;
		}

		overlayEnabled = true;
		overlayManager.add(overlay);
	}

	private void disableOverlay()
	{
		if (overlayEnabled)
		{
			overlayManager.remove(overlay);
		}
		overlayEnabled = false;
	}

	private void enableCountdownTimerInfoBox()
	{
		if (countdownTimerInfoBox == null)
		{
			countdownTimerInfoBox = new InfoBox(itemManager.getImage(SUMMER_SQUIRK_ITEM_ID), this)
			{
				@Override
				public String getText()
				{
					return "" + collisionDetector.getTicksUntilStart();
				}

				@Override
				public Color getTextColor()
				{
					return null;
				}
			};
			infoBoxManager.addInfoBox(countdownTimerInfoBox);
		}
	}

	private void disableCountdownTimerInfoBox()
	{
		infoBoxManager.removeInfoBox(countdownTimerInfoBox);
		countdownTimerInfoBox = null;
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		Player p = client.getLocalPlayer();
		if (p == null)
		{
			return;
		}

		if (p.getWorldLocation().distanceTo2D(GARDEN) >= 50)
		{
			disableCountdownTimerInfoBox();
			disableOverlay();
			return;
		}

		if (config.showCountdownTimer())
		{
			enableCountdownTimerInfoBox();
		}
		enableOverlay();
		client.getNpcs()
			.stream()
			.filter(ElementalCollisionDetector::isSummerElemental)
			.forEach(npc -> collisionDetector.updatePosition(npc, client.getTickCount()));
		collisionDetector.updateCountdownTimer(client.getTickCount());

		// cycle notification
		if (config.cycleNotification() && collisionDetector.getTicksUntilStart() == config.notifyTicksBeforeStart())
		{
			notifier.notify(CYCLE_MESSAGE, TrayIcon.MessageType.INFO);
		}

		playCountdownSounds();

		checkStamina();
	}
	
	private void playCountdownSounds()
	{
		// Race-style countdown  -Green Donut
		if (config.raceStyleCountdown() && collisionDetector.getTicksUntilStart() <= 3 && config.raceStyleVolume() > 0)
		{
			// As playSoundEffect only uses the volume argument when the in-game volume isn't muted, sound effect volume
			// needs to be set to the value desired for race sounds and afterwards reset to the previous value.
			Preferences preferences = client.getPreferences();
			int previousVolume = preferences.getSoundEffectVolume();
			preferences.setSoundEffectVolume(config.raceStyleVolume());

			if (collisionDetector.getTicksUntilStart() == 0)
			{
				// high sound for countdown 0
				client.playSoundEffect(RACE_STYLE_SOUND_HIGH, config.raceStyleVolume());
			}
			else
			{
				// low sound for countdown 3,2,1
				client.playSoundEffect(RACE_STYLE_SOUND_LOW, config.raceStyleVolume());
			}
			preferences.setSoundEffectVolume(previousVolume);
		}
	}
	
	private void checkStamina()
	{
		// check for stamina usage
		int stamThreshold = config.staminaThreshold();
		if (stamThreshold != 0)
		{
			boolean stamActive = client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0;
			if (client.getEnergy() <= stamThreshold && !stamActive && !sentStaminaNotification)
			{
				notifier.notify(STAMINA_MESSAGE, TrayIcon.MessageType.INFO);
				sentStaminaNotification = true;
			}
			else if (client.getEnergy() > stamThreshold)
			{
				sentStaminaNotification = false;
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (!configChanged.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}

		if (configChanged.getKey().equals(CONFIG_KEY_GATE_START))
		{
			collisionDetector.setGateStart(config.useGateStartPoint());
		}
		else if (configChanged.getKey().equals(CONFIG_KEY_COUNTDOWN_TIMER_INFOBOX))
		{
			if (config.showCountdownTimer())
			{
				enableCountdownTimerInfoBox();
			}
			else
			{
				disableCountdownTimerInfoBox();
			}
		}
	}

	@Provides
	SummerGardenConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SummerGardenConfig.class);
	}
}
