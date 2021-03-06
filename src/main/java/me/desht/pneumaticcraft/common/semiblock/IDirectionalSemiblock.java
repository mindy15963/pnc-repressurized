package me.desht.pneumaticcraft.common.semiblock;

import net.minecraft.util.EnumFacing;

/**
 * Represents a semiblock which sits on the side of an actual block, rather than occupying the same space.
 * E.g. transfer gadgets are directional, but crop supports are not.
 * Note that although logistics frames have a "facing" they're not directional; the facing is only to tell
 * drones which side of the inventory to access.
 */
public interface IDirectionalSemiblock {
    EnumFacing getFacing();
}
