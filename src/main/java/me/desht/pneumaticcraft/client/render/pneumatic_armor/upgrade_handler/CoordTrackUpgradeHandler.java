package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiCoordinateTrackerOptions;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderCoordWireframe;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderNavigator;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.ai.EntityPathNavigateDrone;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketCoordTrackUpdate;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CoordTrackUpgradeHandler implements IUpgradeRenderHandler {
    private RenderCoordWireframe coordTracker;
    private RenderNavigator navigator;

    public boolean isListeningToCoordTrackerSetting = false;

    public boolean pathEnabled;
    public boolean wirePath;
    public boolean xRayEnabled;
    // Timer used to delay the client recalculating a path when it didn't last time. This prevents
    // gigantic lag, as it uses much performance to find a path when it doesn't have anything cached.
    private int pathCalculateCooldown;
    private int noPathCooldown;
    public int pathUpdateSetting;
    public static final int SEARCH_RANGE = 150;

    public enum EnumNavigationResult {
        NO_PATH, EASY_PATH, DRONE_PATH
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getUpgradeName() {
        return "coordinateTracker";
    }

    @Override
    public void initConfig() {
        pathEnabled = ConfigHandler.helmetOptions.pathEnabled;
        wirePath = ConfigHandler.helmetOptions.wirePath;
        xRayEnabled = ConfigHandler.helmetOptions.xRayEnabled;
        pathUpdateSetting = ConfigHandler.helmetOptions.pathUpdateSetting;
    }

    @Override
    public void saveToConfig() {
        ConfigHandler.helmetOptions.pathEnabled = pathEnabled;
        ConfigHandler.helmetOptions.wirePath = wirePath;
        ConfigHandler.helmetOptions.xRayEnabled = xRayEnabled;
        ConfigHandler.helmetOptions.pathUpdateSetting = pathUpdateSetting;
        ConfigHandler.sync();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void update(EntityPlayer player, int rangeUpgrades) {
        if (coordTracker != null) {
            coordTracker.ticksExisted++;
        } else {
            BlockPos pos = ItemPneumaticArmor.getCoordTrackerPos(ClientUtils.getWornArmor(EntityEquipmentSlot.HEAD), player.world);
            if (pos != null) {
                coordTracker = new RenderCoordWireframe(player.world, pos);
                navigator = new RenderNavigator(coordTracker.world, coordTracker.pos);
            }
        }
        if (noPathCooldown > 0) {
            noPathCooldown--;
        }
        if (navigator != null && pathEnabled && noPathCooldown == 0 && --pathCalculateCooldown <= 0) {
            navigator.updatePath();
            if (!navigator.tracedToDestination()) {
                noPathCooldown = 100; // wait 5 seconds before recalculating a path.
            }
            pathCalculateCooldown = pathUpdateSetting == 2 ? 1 : pathUpdateSetting == 1 ? 20 : 100;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render3D(float partialTicks) {
        if (coordTracker != null) {
            if (FMLClientHandler.instance().getClient().player.world.provider.getDimension() != coordTracker.world.provider.getDimension())
                return;
            coordTracker.render(partialTicks);
            if (pathEnabled && navigator != null) {
                navigator.render(wirePath, xRayEnabled, partialTicks);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render2D(float partialTicks, boolean upgradeEnabled) {
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[]{Itemss.upgrades.get(EnumUpgrade.COORDINATE_TRACKER)};
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, EntityPlayer player) {
        return PneumaticValues.USAGE_COORD_TRACKER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void reset() {
        coordTracker = null;
        navigator = null;
    }

    @SubscribeEvent
    public boolean onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote && isListeningToCoordTrackerSetting) {
            isListeningToCoordTrackerSetting = false;
            EnumFacing dir = event.getFace();
            if (dir == null) return false;
            reset();
            ItemStack stack = event.getEntityPlayer().getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if (!stack.isEmpty()) {
                ItemPneumaticArmor.setCoordTrackerPos(stack, event.getWorld().provider.getDimension(), event.getPos().offset(dir));
                NetworkHandler.sendToServer(new PacketCoordTrackUpdate(event.getEntity().world, event.getPos().offset(dir)));
            }
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public EnumNavigationResult navigateToSurface(EntityPlayer player) {
        World world = player.world;
        BlockPos navigatingPos = world.getHeight(new BlockPos(player));
        Path path = PneumaticCraftUtils.getPathFinder().findPath(world, PneumaticCraftUtils.createDummyEntity(player), navigatingPos, (float)SEARCH_RANGE);
        if (path != null) {
            for (int i = 0; i < path.getCurrentPathLength(); i++) {
                PathPoint pathPoint = path.getPathPointFromIndex(i);
                BlockPos pathPos = new BlockPos(pathPoint.x, pathPoint.y, pathPoint.z);
                if (world.canSeeSky(pathPos)) {
                    coordTracker = new RenderCoordWireframe(world, pathPos);
                    navigator = new RenderNavigator(world, pathPos);
                    return EnumNavigationResult.EASY_PATH;
                }
            }
        }
        path = getDronePath(player, navigatingPos);
        if (path != null) {
            for (int i = 0; i < path.getCurrentPathLength(); i++) {
                PathPoint pathPoint = path.getPathPointFromIndex(i);
                BlockPos pathPos = new BlockPos(pathPoint.x, pathPoint.y, pathPoint.z);
                if (world.canSeeSky(pathPos)) {
                    coordTracker = new RenderCoordWireframe(world, pathPos);
                    navigator = new RenderNavigator(world, pathPos);
                    return EnumNavigationResult.DRONE_PATH;
                }
            }
        }
        return EnumNavigationResult.NO_PATH;
    }

    public static Path getDronePath(EntityPlayer player, BlockPos pos) {
        World world = player.world;
        EntityDrone drone = new EntityDrone(world);
        drone.setPosition(player.posX, player.posY, player.posZ);
        return new EntityPathNavigateDrone(drone, world).getPathToPos(pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IOptionPage getGuiOptionsPage() {
        return new GuiCoordinateTrackerOptions();
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot() {
        return EntityEquipmentSlot.HEAD;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiAnimatedStat getAnimatedStat() {
        return null;
    }

}
