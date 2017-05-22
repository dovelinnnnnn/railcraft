/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.modules;

import mods.railcraft.api.core.RailcraftModule;
import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.carts.RailcraftCarts;
import mods.railcraft.common.core.Railcraft;
import mods.railcraft.common.util.misc.ChunkManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
@RailcraftModule(value = "railcraft:chunk_loading", description = "world anchors, world anchor carts")
public class ModuleChunkLoading extends RailcraftModulePayload {

    public ModuleChunkLoading() {
        setEnabledEventHandler(new ModuleEventHandler() {
            @Override
            public void construction() {
                add(
                        RailcraftBlocks.ANCHOR,
                        RailcraftBlocks.ANCHOR_SENTINEL,
                        RailcraftCarts.ANCHOR_WORLD,
                        RailcraftCarts.ANCHOR_ADMIN,
                        RailcraftCarts.ANCHOR_PERSONAL
                );
            }

            @Override
            public void preInit() {
                ForgeChunkManager.setForcedChunkLoadingCallback(Railcraft.getMod(), ChunkManager.getInstance());
                MinecraftForge.EVENT_BUS.register(ChunkManager.getInstance());
            }
        });
    }
}
