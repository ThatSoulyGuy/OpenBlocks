package com.openblocks.utility;

import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/**
 * A trapdoor that doubles as a ladder when open.
 * Solves the annoying problem of jumping out at the top of a ladder.
 * The block is added to the #minecraft:climbable tag so players can
 * climb it when the trapdoor is in the open (vertical) position.
 */
public class JadedLadderBlock extends TrapDoorBlock {

    public JadedLadderBlock(Properties properties) {
        super(BlockSetType.OAK, properties);
    }
}
