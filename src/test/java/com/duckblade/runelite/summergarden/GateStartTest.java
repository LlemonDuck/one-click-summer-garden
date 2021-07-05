package com.duckblade.runelite.summergarden;

import static com.duckblade.runelite.summergarden.GateStartTest.Direction.*;
import java.util.Random;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

public class GateStartTest
{

	@Test
	public void testValidTicks() {
		boolean[][] expectedNormalStart = ElementalCollisionDetector.VALID_TICKS_NORMAL_START;
		boolean[][] expectedGateStart = ElementalCollisionDetector.VALID_TICKS_GATE_START;
		boolean[][] generatedGateStart = generateValidTicks(0);
		boolean[][] generatedNormalStart = generateValidTicks(3);
		System.out.println("expected (normal start):");
		print2darray(expectedNormalStart);
		System.out.println("generated:");
		print2darray(generatedNormalStart);
		System.out.println("expected (gate start):");
		print2darray(expectedGateStart);
		System.out.println("generated:");
		print2darray(generatedGateStart);

		Assert.assertArrayEquals(expectedNormalStart, generatedNormalStart);
		Assert.assertArrayEquals(expectedGateStart, generatedGateStart);
	}

	private void print2darray(boolean[][] expected)
	{
		for (boolean[] arr : expected)
		{
			String s = "";
			for (int j = 0; j < arr.length; j++)
			{
				s += (arr[j] ? j + 1 : 0) + " ";
			}
			System.out.println(s);
		}
	}

	@Ignore
	@Test
	public void gateVsNormalParityComparison() {
		ElementalCollisionDetector ecd = new ElementalCollisionDetector();

		int n = 10000;

		int highestLowestParity_gate = 0;
		int paritySumSum_gate = 0;
		int highestLowestParity_normal = 0;
		int paritySumSum_normal = 0;
		Random random = new Random();
		for (int i = 0; i < n; i++)
		{
			updateNpcs(ecd, 0, random.nextInt(10), random.nextInt(12), random.nextInt(20), random.nextInt(12), random.nextInt(12));

			ecd.setGateStart(true);
			int paritySum = ecd.getParitySum(ecd.getBestStartPointForLowestTotalParityScore());
			paritySumSum_gate += paritySum;
			if (paritySum > highestLowestParity_gate)
			{
				highestLowestParity_gate = paritySum;
			}

			ecd.setGateStart(false);
			paritySum = ecd.getParitySum(ecd.getBestStartPointForLowestTotalParityScore());
			paritySumSum_normal += paritySum;
			if (paritySum > highestLowestParity_normal)
			{
				highestLowestParity_normal = paritySum;
			}
		}
		System.out.println("gate:         highest: " + highestLowestParity_gate + " average: " + (((double) paritySumSum_gate) / n));
		System.out.println("normal:       highest: " + highestLowestParity_normal + " average: " + (((double) paritySumSum_normal) / n));
	}

	private void printParities(ElementalCollisionDetector ecd)
	{
		for (int i = 0; i < 6; i++)
		{
			System.out.println("parity for elemental " + 0 + " is " + ecd.getParity(i + 1801));
		}
	}

	private void updateNpcs(ElementalCollisionDetector ecd, int... npcOffsets)
	{
		assert npcOffsets.length == 6;

		for (int i = 0; i < 6; i++)
		{
			ecd.updatePosition(npc(i), npcOffsets[i]);
		}
	}

	private NPC npc(int index) {
		NPC npc = Mockito.mock(NPC.class);
		Mockito.when(npc.getId()).thenReturn(1801 + index);
		Mockito.when(npc.getWorldLocation()).thenReturn(HOMES[index]);
		return npc;
	}

	private static final WorldPoint[] HOMES = {
		new WorldPoint(2907, 5488, 0),
		new WorldPoint(2907, 5490, 0),
		new WorldPoint(2910, 5487, 0),
		new WorldPoint(2912, 5485, 0),
		new WorldPoint(2921, 5486, 0),
		new WorldPoint(2921, 5495, 0),
	};

	enum Direction {
		NORTH(0, 1), SOUTH(0, -1), EAST(1, 0), WEST(-1, 0),
		NORTHEAST(1, 1), SOUTHEAST(1, -1), SOUTHWEST(-1, -1), NORTHWEST(-1, 1)
		;

		public final int x;
		public final int y;

		Direction(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
	}
	private static final Direction[][] ELEMENTAL_PATHS = {
		{SOUTH, SOUTH, SOUTH, SOUTH, SOUTH, NORTH, NORTH, NORTH, NORTH, NORTH},
		{NORTH, NORTH, NORTH, NORTH, NORTH, SOUTH, SOUTH, SOUTH, SOUTH, SOUTH},
		{NORTH, NORTH, NORTH, NORTH, NORTH, NORTH, SOUTH, SOUTH, SOUTH, SOUTH, SOUTH, SOUTH},
		{EAST, EAST, EAST, SOUTH, SOUTH, EAST, EAST, EAST, NORTH, NORTH, WEST, WEST, WEST, SOUTH, SOUTH, WEST, WEST, WEST, NORTH, NORTH},
		{EAST, EAST, NORTH, NORTH, NORTH, NORTH, SOUTH, SOUTH, SOUTH, SOUTH, WEST, WEST},
		{SOUTH, SOUTH, SOUTH, SOUTH, EAST, EAST, NORTH, NORTH, NORTH, NORTH, WEST, WEST}
	};
	/**
	 * The tile just after the gate.
	 */
	private static final WorldPoint PLAYER_START = new WorldPoint(2910, 5481, 0);
	private static final Direction[] PLAYER_PATH = {
		NORTH, WEST, WEST, WEST, NORTH, NORTH, NORTH, NORTH, NORTH, NORTH, WEST, NORTH, NORTH, NORTH, NORTHEAST, NORTH,
		NORTH, NORTH, EAST, EAST, SOUTH, SOUTH, SOUTH, SOUTH, SOUTHEAST, SOUTH, SOUTH, SOUTHWEST, SOUTH, SOUTH, EAST,
		EAST, EAST, EAST, EAST, EAST, EAST, EAST, EAST, EAST, EAST, EAST, NORTH, EAST, EAST, NORTH, NORTH, NORTH,
		NORTH, NORTH, WEST, WEST, SOUTH, SOUTH, SOUTH, SOUTH, WEST, WEST,
	};

	private static int moduloPositive(int base, int mod) {
		return ((base % mod) + mod) % mod;
	}

	/**
	 * WARNING: In this function, elementals can see through walls. In game, they cannot. This does not make a
	 * difference for the summer garden, but may for other situations.
	 */
	private static boolean[][] generateValidTicks(int playerStartOffset)
	{
		boolean[][] validTicks = new boolean[6][];
		for (int i = 0; i < validTicks.length; i++)
		{
			validTicks[i] = new boolean[ELEMENTAL_PATHS[i].length];
		}

		for (int elementalIndex = 0; elementalIndex < ELEMENTAL_PATHS.length; elementalIndex++)
		{
			Direction[] elementalPath = ELEMENTAL_PATHS[elementalIndex];
			for (int elementalPathIndex = 0; elementalPathIndex < elementalPath.length; elementalPathIndex++)
			{
				// must use the elemental's last direction to tell where it's looking.
				Direction elementalDirection = elementalPath[moduloPositive(elementalPathIndex - 1, elementalPath.length)];
				System.out.println("elemental " + elementalIndex + " " + elementalDirection + " " + elementalPathIndex);

				validTicks[elementalIndex][elementalPathIndex] =
					isValidElementalStartPosition(playerStartOffset, elementalPathIndex, elementalIndex);
			}
		}
		return validTicks;
	}

	private static boolean isValidElementalStartPosition(int playerStartOffset, int elementalPathIndex, int elementalIndex)
	{
		Direction[] elementalPath = ELEMENTAL_PATHS[elementalIndex];

		int playerX = PLAYER_START.getX();
		int playerY = PLAYER_START.getY();
		int elementalX = HOMES[elementalIndex].getX();
		int elementalY = HOMES[elementalIndex].getY();

		for (int i = 0; i < playerStartOffset; i++)
		{
			Direction playerDirection = PLAYER_PATH[i];
			playerX += playerDirection.x;
			playerY += playerDirection.y;
		}

		for (int i = 0; i < elementalPathIndex; i++)
		{
			Direction elementalDirection = elementalPath[moduloPositive(i, elementalPath.length)];
			elementalX += elementalDirection.x;
			elementalY += elementalDirection.y;
		}

		int i = 0;
		while (playerStartOffset + i * 2 + 1 < PLAYER_PATH.length)
		{
			// Elemental looks in the last direction it moved
			Direction elementalLooking = elementalPath[moduloPositive(elementalPathIndex + i - 1, elementalPath.length)];
			Direction elementalDirection = elementalPath[moduloPositive(elementalPathIndex + i, elementalPath.length)];

			Direction playerDirection1 = PLAYER_PATH[playerStartOffset + i * 2];
			Direction playerDirection2 = PLAYER_PATH[playerStartOffset + i * 2 + 1];

			// The tile it's on and the 3 tiles in front of it are always dangerous.
			for (int j = 0; j < 4; j++)
			{
				if (playerX == elementalX + elementalLooking.x * j && playerY == elementalY + elementalLooking.y * j) {
					return false;
				}
			}

			playerX += playerDirection1.x;
			playerY += playerDirection1.y;
			playerX += playerDirection2.x;
			playerY += playerDirection2.y;
			elementalX += elementalDirection.x;
			elementalY += elementalDirection.y;
			i++;
		}
		return true;
	}

}
