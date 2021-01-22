package com.duckblade.runelite.summergarden;

import javax.inject.Singleton;

import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

@Singleton
public class ElementalCollisionDetector {

    private static final WorldPoint[] HOMES = {
            new WorldPoint(2907, 5488, 0),
            new WorldPoint(2907, 5490, 0),
            new WorldPoint(2910, 5487, 0),
            new WorldPoint(2912, 5485, 0),
            new WorldPoint(2921, 5486, 0),
            new WorldPoint(2921, 5495, 0),
    };
    private static final int[] CYCLE_LENGTHS = {10, 10, 12, 20, 12, 12};

    private final int[] tickBasis = {-1, -1, -1, -1, -1, -1};

    public static boolean isSummerElemental(NPC npc) {
        return npc.getId() >= 1801 && npc.getId() <= 1806;
    }

    public void updatePosition(NPC npc, int tc) {
        if (!isSummerElemental(npc))
            return;

        int eId = npc.getId() - 1801;
        if (npc.getWorldLocation().equals(HOMES[eId]))
            tickBasis[eId] = tc;
    }

    public int getParity(int elementalId) {
        switch (elementalId) {
            case 1801:
                return getParityOne();
            case 1802:
                return getParityTwo();
            case 1803:
                return getParityThree();
            case 1804:
                return getParityFour();
            case 1805:
                return getParityFive();
            case 1806:
                return getParitySix();
            default:
                return 0;
        }
    }

    public boolean isLaunchCycle() {
        return tickBasis[0] == tickBasis[2];
    }

    private int getDiff(int baseEId, int targetEid) {
        int bb = tickBasis[baseEId];
        int tb = tickBasis[targetEid];
        int diff = (bb - tb) % CYCLE_LENGTHS[baseEId];
        return diff < 0 ? CYCLE_LENGTHS[baseEId] + diff : diff;
    }

    private int getParityOne() {
        if (tickBasis[0] == -1)
            return -1;

        return (tickBasis[0] % 2) == 0 ? 0 : 1;
    }

    private int getParityTwo() {
        if (getParityOne() != 0 || tickBasis[1] == -1)
            return -1;

        // equal length to e1, so we can compare bases
        int diff = getDiff(0, 1);
        if (diff == 1 || diff == 6) // works on 1 and 6
            return 0;
        else if (diff == 0) // 0 wraps down to 6
            return 4;
        else if (diff < 6) // distance to 1
            return diff - 1;
        else // distance to 6
            return diff - 6;
    }

    private int getParityThree() {
        if (tickBasis[2] == -1)
            return -1;

        return tickBasis[2] % 2 == 0 ? 0 : 1;
    }

    private int getParityFour() {
        if (getParityOne() != 0 || tickBasis[3] == -1)
            return -1;

        int diff = getDiff(0, 3);
        if ((diff >= 1 && diff <= 4) || diff == 6) // works on 1-4 + 6
            return 0;
        else if (diff == 0) // wraps to 6
            return 4;
        else if (diff == 5) // distance to 4
            return 1;
        else // distance to 6
            return diff - 6;
    }

    private int getParityFive() {
        if (getParityThree() != 0 || tickBasis[4] == -1)
            return -1;

        int diff = getDiff(2, 4);
        if (diff == 1 || diff == 7) // works on 1 + 7
            return 0;
        else if (diff == 0) // 0 wraps to 7
            return 5;
        else if (diff < 7) // distance to 1
            return diff - 1;
        else // distance to 7
            return diff - 7;
    }

    private int getParitySix() {
        if (getParityThree() != 0 || tickBasis[5] == -1)
            return -1;

        return getDiff(2, 5);
    }

}
