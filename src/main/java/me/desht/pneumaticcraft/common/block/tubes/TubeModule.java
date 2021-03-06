package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.client.model.module.ModelModuleBase;
import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketOpenTubeModuleGui;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.lib.BBConstants.PRESSURE_PIPE_MAX_POS;
import static me.desht.pneumaticcraft.lib.BBConstants.PRESSURE_PIPE_MIN_POS;

public abstract class TubeModule implements ISidedPart {
    public static final float MAX_VALUE = 30;

    protected IPneumaticPosProvider pressureTube;
    protected EnumFacing dir = EnumFacing.UP;
    public final AxisAlignedBB[] boundingBoxes = new AxisAlignedBB[6];
    protected boolean upgraded;
    public float lowerBound = 7.5F, higherBound = 0;
    private boolean fake;
    public boolean advancedConfig;
    public boolean shouldDrop;
    @SideOnly(Side.CLIENT)
    private ModelModuleBase model;

    public TubeModule() {
        double width = getWidth() / 2;
        double height = getHeight();

        // 0..6 = D,U,N,S,W,E
        boundingBoxes[0] = new AxisAlignedBB(0.5 - width, PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 + width, PRESSURE_PIPE_MIN_POS, 0.5 + width);
        boundingBoxes[1] = new AxisAlignedBB(0.5 - width, PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 + width, PRESSURE_PIPE_MAX_POS + height, 0.5 + width);
        boundingBoxes[2] = new AxisAlignedBB(0.5 - width, 0.5 - width, PRESSURE_PIPE_MIN_POS - height, 0.5 + width, 0.5 + width, PRESSURE_PIPE_MIN_POS);
        boundingBoxes[3] = new AxisAlignedBB(0.5 - width, 0.5 - width, PRESSURE_PIPE_MAX_POS, 0.5 + width, 0.5 + width, PRESSURE_PIPE_MAX_POS + height);
        boundingBoxes[4] = new AxisAlignedBB(PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 - width, PRESSURE_PIPE_MIN_POS, 0.5 + width, 0.5 + width);
        boundingBoxes[5] = new AxisAlignedBB(PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 - width, PRESSURE_PIPE_MAX_POS + height, 0.5 + width, 0.5 + width);
    }

    public void markFake() {
        fake = true;
    }

    public boolean isFake() {
        return fake;
    }

    public void setTube(IPneumaticPosProvider pressureTube) {
        this.pressureTube = pressureTube;
    }

    public IPneumaticPosProvider getTube() {
        return pressureTube;
    }

    public double getWidth() {
        return PRESSURE_PIPE_MAX_POS - PRESSURE_PIPE_MIN_POS;
    }

    protected double getHeight() {
        return PRESSURE_PIPE_MIN_POS;
    }

    public float getThreshold(int redstone) {
        double slope = (higherBound - lowerBound) / 15;
        double threshold = lowerBound + slope * redstone;
        return (float) threshold;
    }

    /**
     * Returns the item(s) that this part drops.
     *
     * @return the module item and possibly an Advanced PCB too
     */
    public NonNullList<ItemStack> getDrops() {
        NonNullList<ItemStack> drops = NonNullList.create();
        if (shouldDrop) {
            drops.add(new ItemStack(ModuleRegistrator.getModuleItem(getType())));
            if (upgraded) drops.add(new ItemStack(Itemss.ADVANCED_PCB));
        }
        return drops;
    }

    @Override
    public void setDirection(EnumFacing dir) {
        this.dir = dir;
    }

    public EnumFacing getDirection() {
        return dir;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        dir = EnumFacing.byIndex(nbt.getInteger("dir"));
        upgraded = nbt.getBoolean("upgraded");
        lowerBound = nbt.getFloat("lowerBound");
        higherBound = nbt.getFloat("higherBound");
        advancedConfig = !nbt.hasKey("advancedConfig") || nbt.getBoolean("advancedConfig");
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("dir", dir.ordinal());
        nbt.setBoolean("upgraded", upgraded);
        nbt.setFloat("lowerBound", lowerBound);
        nbt.setFloat("higherBound", higherBound);
        nbt.setBoolean("advancedConfig", advancedConfig);
    }

    public void update() {
    }

    public void onNeighborTileUpdate() {
    }

    public void onNeighborBlockUpdate() {
    }

    /**
     * Get a unique string identifier for this module type.
     *
     * @return the module ID
     */
    public abstract String getType();

    public int getRedstoneLevel() {
        return 0;
    }

    void updateNeighbors() {
        pressureTube.world().notifyNeighborsOfStateChange(pressureTube.pos(), pressureTube.world().getBlockState(pressureTube.pos()).getBlock(), true);
    }

    public boolean isInline() {
        return false;
    }

    public void sendDescriptionPacket() {
        if (pressureTube instanceof TileEntityPressureTube) ((TileEntityPressureTube) pressureTube).sendDescriptionPacket();
    }

    public void addInfo(List<String> curInfo) {
        if (upgraded) {
            ItemStack stack = new ItemStack(Itemss.ADVANCED_PCB);
            curInfo.add(TextFormatting.GREEN + stack.getDisplayName() + " installed");
        }
        if (this instanceof INetworkedModule) {
            int colorChannel = ((INetworkedModule) this).getColorChannel();
            curInfo.add(PneumaticCraftUtils.xlate("waila.logisticsModule.channel") + " "
                    + TextFormatting.YELLOW
                    + PneumaticCraftUtils.xlate("item.fireworksCharge." + EnumDyeColor.byDyeDamage(colorChannel).getTranslationKey()));
        }
    }

    public boolean canUpgrade() {
        return true;
    }

    public void upgrade() {
        upgraded = true;
    }

    public boolean isUpgraded() {
        return upgraded;
    }

    public boolean onActivated(EntityPlayer player, EnumHand hand) {
        if (!player.world.isRemote && upgraded && getGuiId() != null && !player.isSneaking()) {
            NetworkHandler.sendTo(new PacketOpenTubeModuleGui(getGuiId().ordinal(), pressureTube.pos()), (EntityPlayerMP) player);
            return true;
        }
        return false;
    }

    protected abstract EnumGuiId getGuiId();

    @SideOnly(Side.CLIENT)
    public abstract Class<? extends ModelModuleBase> getModelClass();

    @SideOnly(Side.CLIENT)
    public final ModelModuleBase getModel() {
        if (model == null) {
            try {
                Constructor<? extends ModelModuleBase> ctor = getModelClass().getDeclaredConstructor(this.getClass());
                model = ctor.newInstance(this);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                model = new ModelModuleBase.MissingModel();
            }
        }
        return model;
    }

    @SideOnly(Side.CLIENT)
    public void doExtraRendering() {
    }

    public AxisAlignedBB getRenderBoundingBox() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TubeModule)) return false;
        TubeModule that = (TubeModule) o;
        return Objects.equals(pressureTube.pos(), that.pressureTube.pos()) && dir == that.dir;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pressureTube.pos(), dir);
    }

}
