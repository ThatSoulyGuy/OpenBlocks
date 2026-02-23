package com.openblocks.guide;

import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Static shape generation algorithms for the guide block.
 */
public final class ShapeGenerators {

    private ShapeGenerators() {}

    public static Set<BlockPos> generate(GuideShape shape, int negX, int negY, int negZ,
                                          int posX, int posY, int posZ) {
        Set<BlockPos> result = new HashSet<>();
        Consumer<BlockPos> collector = result::add;

        switch (shape) {
            case SPHERE -> generateSphere(negX, negY, negZ, posX, posY, posZ, collector);
            case CYLINDER -> generateCylinder(negX, negY, negZ, posX, posY, posZ, collector);
            case DOME -> generateDome(negX, negY, negZ, posX, posY, posZ, collector);
            case CUBOID -> generateCuboid(negX, negY, negZ, posX, posY, posZ, collector);
            case DIAMOND -> generateDiamond(negX, negY, negZ, posX, posY, posZ, collector);
        }

        return result;
    }

    /**
     * Sphere shell: (x/rx)^2 + (y/ry)^2 + (z/rz)^2 <= 1, only surface voxels.
     */
    private static void generateSphere(int negX, int negY, int negZ,
                                        int posX, int posY, int posZ,
                                        Consumer<BlockPos> collector) {
        float rx = (negX + posX) / 2.0f;
        float ry = (negY + posY) / 2.0f;
        float rz = (negZ + posZ) / 2.0f;
        if (rx <= 0 || ry <= 0 || rz <= 0) return;

        for (int x = -negX; x <= posX; x++) {
            for (int y = -negY; y <= posY; y++) {
                for (int z = -negZ; z <= posZ; z++) {
                    float dx = x / rx;
                    float dy = y / ry;
                    float dz = z / rz;
                    float dist = dx * dx + dy * dy + dz * dz;
                    if (dist <= 1.0f && dist >= 0.7f) {
                        collector.accept(new BlockPos(x, y, z));
                    }
                }
            }
        }
    }

    /**
     * Cylinder: 2D ellipse (x/rx)^2 + (z/rz)^2 <= 1, extruded along Y.
     */
    private static void generateCylinder(int negX, int negY, int negZ,
                                          int posX, int posY, int posZ,
                                          Consumer<BlockPos> collector) {
        float rx = (negX + posX) / 2.0f;
        float rz = (negZ + posZ) / 2.0f;
        if (rx <= 0 || rz <= 0) return;

        for (int x = -negX; x <= posX; x++) {
            for (int z = -negZ; z <= posZ; z++) {
                float dx = x / rx;
                float dz = z / rz;
                float dist = dx * dx + dz * dz;
                if (dist <= 1.0f && dist >= 0.7f) {
                    for (int y = -negY; y <= posY; y++) {
                        collector.accept(new BlockPos(x, y, z));
                    }
                }
            }
        }
        // Top and bottom caps
        for (int x = -negX; x <= posX; x++) {
            for (int z = -negZ; z <= posZ; z++) {
                float dx = x / rx;
                float dz = z / rz;
                if (dx * dx + dz * dz <= 1.0f) {
                    collector.accept(new BlockPos(x, -negY, z));
                    collector.accept(new BlockPos(x, posY, z));
                }
            }
        }
    }

    /**
     * Dome: top hemisphere of a sphere.
     */
    private static void generateDome(int negX, int negY, int negZ,
                                      int posX, int posY, int posZ,
                                      Consumer<BlockPos> collector) {
        float rx = (negX + posX) / 2.0f;
        float ry = posY; // only positive Y
        float rz = (negZ + posZ) / 2.0f;
        if (rx <= 0 || ry <= 0 || rz <= 0) return;

        for (int x = -negX; x <= posX; x++) {
            for (int y = 0; y <= posY; y++) {
                for (int z = -negZ; z <= posZ; z++) {
                    float dx = x / rx;
                    float dy = y / ry;
                    float dz = z / rz;
                    float dist = dx * dx + dy * dy + dz * dz;
                    if (dist <= 1.0f && dist >= 0.7f) {
                        collector.accept(new BlockPos(x, y, z));
                    }
                }
            }
        }
    }

    /**
     * Cuboid: wireframe edges only.
     */
    private static void generateCuboid(int negX, int negY, int negZ,
                                        int posX, int posY, int posZ,
                                        Consumer<BlockPos> collector) {
        // 4 edges along X
        for (int x = -negX; x <= posX; x++) {
            collector.accept(new BlockPos(x, -negY, -negZ));
            collector.accept(new BlockPos(x, -negY, posZ));
            collector.accept(new BlockPos(x, posY, -negZ));
            collector.accept(new BlockPos(x, posY, posZ));
        }
        // 4 edges along Y
        for (int y = -negY; y <= posY; y++) {
            collector.accept(new BlockPos(-negX, y, -negZ));
            collector.accept(new BlockPos(-negX, y, posZ));
            collector.accept(new BlockPos(posX, y, -negZ));
            collector.accept(new BlockPos(posX, y, posZ));
        }
        // 4 edges along Z
        for (int z = -negZ; z <= posZ; z++) {
            collector.accept(new BlockPos(-negX, -negY, z));
            collector.accept(new BlockPos(-negX, posY, z));
            collector.accept(new BlockPos(posX, -negY, z));
            collector.accept(new BlockPos(posX, posY, z));
        }
    }

    /**
     * Diamond (octahedron): |x/rx| + |y/ry| + |z/rz| <= 1, surface only.
     */
    private static void generateDiamond(int negX, int negY, int negZ,
                                         int posX, int posY, int posZ,
                                         Consumer<BlockPos> collector) {
        float rx = (negX + posX) / 2.0f;
        float ry = (negY + posY) / 2.0f;
        float rz = (negZ + posZ) / 2.0f;
        if (rx <= 0 || ry <= 0 || rz <= 0) return;

        for (int x = -negX; x <= posX; x++) {
            for (int y = -negY; y <= posY; y++) {
                for (int z = -negZ; z <= posZ; z++) {
                    float dx = Math.abs(x / rx);
                    float dy = Math.abs(y / ry);
                    float dz = Math.abs(z / rz);
                    float dist = dx + dy + dz;
                    if (dist <= 1.0f && dist >= 0.7f) {
                        collector.accept(new BlockPos(x, y, z));
                    }
                }
            }
        }
    }
}
