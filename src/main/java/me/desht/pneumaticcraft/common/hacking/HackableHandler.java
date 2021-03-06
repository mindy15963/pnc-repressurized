package me.desht.pneumaticcraft.common.hacking;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderBlockTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderEntityTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackUpgradeHandler;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.hacking.block.*;
import me.desht.pneumaticcraft.common.hacking.entity.*;
import me.desht.pneumaticcraft.common.util.WorldAndCoord;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HackableHandler {
    private final Map<Entity, IHackableEntity> trackedHackableEntities = new HashMap<>();
    private final Map<WorldAndCoord, IHackableBlock> trackedHackableBlocks = new HashMap<>();
    private static HackableHandler clientInstance, serverInstance;

    private static HackableHandler getInstance() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            if (clientInstance == null) clientInstance = new HackableHandler();
            return clientInstance;
        } else {
            if (serverInstance == null) serverInstance = new HackableHandler();
            return serverInstance;
        }
    }

    public static void addDefaultEntries() {
        IPneumaticHelmetRegistry manager = PneumaticRegistry.getInstance().getHelmetRegistry();
        manager.addHackable(Blocks.TNT, HackableTNT.class);
        manager.addHackable(Blocks.MOB_SPAWNER, HackableMobSpawner.class);
        manager.addHackable(Blocks.LEVER, HackableLever.class);
        manager.addHackable(Blocks.STONE_BUTTON, HackableButton.class);
        manager.addHackable(Blocks.WOODEN_BUTTON, HackableButton.class);
        manager.addHackable(Blocks.OAK_DOOR, HackableDoor.class);
        manager.addHackable(Blocks.SPRUCE_DOOR, HackableDoor.class);
        manager.addHackable(Blocks.BIRCH_DOOR, HackableDoor.class);
        manager.addHackable(Blocks.JUNGLE_DOOR, HackableDoor.class);
        manager.addHackable(Blocks.ACACIA_DOOR, HackableDoor.class);
        manager.addHackable(Blocks.DARK_OAK_DOOR, HackableDoor.class);
        manager.addHackable(Blocks.TRIPWIRE_HOOK, HackableTripwire.class);
        manager.addHackable(Blocks.DISPENSER, HackableDispenser.class);
        manager.addHackable(Blocks.DROPPER, HackableDispenser.class);
        manager.addHackable(Blockss.SECURITY_STATION, HackableSecurityStation.class);
        manager.addHackable(Blocks.MONSTER_EGG, HackableSilverfish.class);
        manager.addHackable(Blocks.NOTEBLOCK, HackableNoteblock.class);
        manager.addHackable(Blocks.JUKEBOX, HackableJukebox.class);

        manager.addHackable(EntityCreeper.class, HackableCreeper.class);
        manager.addHackable(EntityTameable.class, HackableTameable.class);
        manager.addHackable(EntityCow.class, HackableCow.class);
        manager.addHackable(EntityCaveSpider.class, HackableCaveSpider.class);
        manager.addHackable(EntityBlaze.class, HackableBlaze.class);
        manager.addHackable(EntityGhast.class, HackableGhast.class);
        manager.addHackable(EntityWitch.class, HackableWitch.class);
        manager.addHackable(EntityLiving.class, HackableLivingDisarm.class);
        manager.addHackable(EntityEnderman.class, HackableEnderman.class);
        manager.addHackable(EntityBat.class, HackableBat.class);
        manager.addHackable(EntityHorse.class, HackableHorse.class);
        manager.addHackable(EntityShulker.class, HackableShulker.class);
        manager.addHackable(EntityGuardian.class, HackableGuardian.class);
        manager.addHackable(EntityVillager.class, HackableVillager.class);
        manager.addHackable(EntityPainting.class, HackablePainting.class);
        manager.addHackable(EntityItemFrame.class, HackableItemFrame.class);
    }

    public static IHackableEntity getHackableForEntity(Entity entity, EntityPlayer player) {

        //clean up the map
        getInstance().trackedHackableEntities.entrySet().removeIf(entry -> entry.getKey().isDead || !entry.getValue().canHack(entry.getKey(), player) && !isInDisplayCooldown(entry.getValue(), entry.getKey()));

        if (entity instanceof IHackableEntity && ((IHackableEntity) entity).canHack(entity, player))
            return (IHackableEntity) entity;
        IHackableEntity hackable = getInstance().trackedHackableEntities.get(entity);
        if (hackable == null) {
            for (Map.Entry<Class<? extends Entity>, Class<? extends IHackableEntity>> entry : PneumaticHelmetRegistry.getInstance().hackableEntities.entrySet()) {
                if (entry.getKey().isAssignableFrom(entity.getClass())) {
                    try {
                        hackable = entry.getValue().newInstance();
                        if (hackable.canHack(entity, player)) {
                            getInstance().trackedHackableEntities.put(entity, hackable);
                            break;
                        } else {
                            hackable = null;
                        }
                    } catch (Exception e) {
                        //shouldn't happen, checked earlier.
                        e.printStackTrace();
                    }
                }
            }
        }
        return hackable;
    }

    public static IHackableBlock getHackableForCoord(WorldAndCoord coord, EntityPlayer player) {
        return getHackableForCoord(coord.world, coord.pos, player);
    }

    public static IHackableBlock getHackableForCoord(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        //clean up the map
        Iterator<Map.Entry<WorldAndCoord, IHackableBlock>> iterator = getInstance().trackedHackableBlocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<WorldAndCoord, IHackableBlock> entry = iterator.next();
            Class<? extends IHackableBlock> hackableBlockClazz = PneumaticHelmetRegistry.getInstance().hackableBlocks.get(entry.getKey().getBlock());
            if (hackableBlockClazz != entry.getValue().getClass() || !entry.getValue().canHack(entry.getKey().world, entry.getKey().pos, player) && !isInDisplayCooldown(entry.getValue(), entry.getKey().world, entry.getKey().pos, player))
                iterator.remove();
        }

        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof IHackableBlock && ((IHackableBlock) block).canHack(world, pos, player))
            return (IHackableBlock) block;
        IHackableBlock hackable = getInstance().trackedHackableBlocks.get(new WorldAndCoord(world, pos));
        if (hackable == null) {
            if (!PneumaticHelmetRegistry.getInstance().hackableBlocks.containsKey(block)) return null;
            try {
                hackable = PneumaticHelmetRegistry.getInstance().hackableBlocks.get(block).newInstance();
                if (hackable.canHack(world, pos, player)) {
                    getInstance().trackedHackableBlocks.put(new WorldAndCoord(world, pos), hackable);
                } else {
                    hackable = null;
                }
            } catch (Exception e) {
                //shouldn't happen, checked earlier.
                e.printStackTrace();
            }
        }
        return hackable;
    }

    private static boolean isInDisplayCooldown(IHackableBlock hackableBlock, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        if (player.world.isRemote) {
            RenderBlockTarget target = HUDHandler.instance().getSpecificRenderer(BlockTrackUpgradeHandler.class).getTargetForCoord(pos);
            int requiredHackTime = hackableBlock.getHackTime(world, pos, player);
            return target != null && target.getHackTime() >= requiredHackTime && target.getHackTime() <= requiredHackTime + 20;
        } else {
            return false;
        }
    }

    private static boolean isInDisplayCooldown(IHackableEntity hackableBlock, Entity entity) {
        if (entity.world.isRemote) {
            RenderEntityTarget target = HUDHandler.instance().getSpecificRenderer(EntityTrackUpgradeHandler.class).getTargetForEntity(entity);
            int requiredHackTime = hackableBlock.getHackTime(entity, PneumaticCraftRepressurized.proxy.getClientPlayer());
            return target != null && target.getHackTime() >= requiredHackTime && target.getHackTime() <= requiredHackTime + 20;
        } else {
            return false;
        }
    }
}
