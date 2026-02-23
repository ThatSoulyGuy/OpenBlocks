package com.openblocks.imaginary;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Shape variants for imaginary blocks, matching the 1.12.2 original.
 */
public enum ImaginaryShape implements StringRepresentable {
    BLOCK("block"),
    PANEL("panel"),
    HALF_PANEL("half_panel"),
    STAIRS("stairs");

    private static final double PANEL_HEIGHT = 0.1;

    // Symmetric shapes (same for all facings)
    private static final VoxelShape PANEL_SHAPE = Shapes.box(0, 1 - PANEL_HEIGHT, 0, 1, 1, 1);
    private static final VoxelShape HALF_PANEL_SHAPE = Shapes.box(0, 0.5 - PANEL_HEIGHT, 0, 1, 0.5, 1);

    // Stairs shapes pre-computed per horizontal facing.
    // Default orientation (SOUTH): lower step at north (z=0..0.5), upper step at south (z=0.5..1).
    // Player faces SOUTH and walks up the stairs going south.
    private static final VoxelShape STAIRS_SOUTH = Shapes.or(
            Shapes.box(0, 0.5 - PANEL_HEIGHT, 0, 1, 0.5, 0.5),
            Shapes.box(0, 1 - PANEL_HEIGHT, 0.5, 1, 1, 1)
    );
    private static final VoxelShape STAIRS_NORTH = Shapes.or(
            Shapes.box(0, 0.5 - PANEL_HEIGHT, 0.5, 1, 0.5, 1),
            Shapes.box(0, 1 - PANEL_HEIGHT, 0, 1, 1, 0.5)
    );
    private static final VoxelShape STAIRS_EAST = Shapes.or(
            Shapes.box(0, 0.5 - PANEL_HEIGHT, 0, 0.5, 0.5, 1),
            Shapes.box(0.5, 1 - PANEL_HEIGHT, 0, 1, 1, 1)
    );
    private static final VoxelShape STAIRS_WEST = Shapes.or(
            Shapes.box(0.5, 0.5 - PANEL_HEIGHT, 0, 1, 0.5, 1),
            Shapes.box(0, 1 - PANEL_HEIGHT, 0, 0.5, 1, 1)
    );

    private final String name;

    ImaginaryShape(String name) {
        this.name = name;
    }

    /**
     * Returns the VoxelShape for this shape rotated to the given horizontal facing.
     * Symmetric shapes (BLOCK, PANEL, HALF_PANEL) ignore facing.
     */
    public VoxelShape getVoxelShape(Direction facing) {
        return switch (this) {
            case BLOCK -> Shapes.block();
            case PANEL -> PANEL_SHAPE;
            case HALF_PANEL -> HALF_PANEL_SHAPE;
            case STAIRS -> switch (facing) {
                case NORTH -> STAIRS_NORTH;
                case EAST -> STAIRS_EAST;
                case WEST -> STAIRS_WEST;
                default -> STAIRS_SOUTH;
            };
        };
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static ImaginaryShape fromOrdinal(int ordinal) {
        ImaginaryShape[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return BLOCK;
    }
}
