/*
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.multi;

import static mods.railcraft.common.blocks.multi.TileTankSteelWall.STEEL_TANK;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public final class TileTankSteelValve<M extends TileTankBase<M, M>> extends TileTankIronValve<TileTankSteelValve<M>, M> {

    @Override
    @SuppressWarnings("unchecked")
    protected Class<TileTankSteelValve<M>> defineSelfClass() {
        return (Class<TileTankSteelValve<M>>) (Class<?>) TileTankSteelValve.class;
    }

    @Override
    public MetalTank getTankType() {
        return STEEL_TANK;
    }

    @Override
    public int getCapacityPerBlock() {
        return CAPACITY_PER_BLOCK_STEEL;
    }
}
