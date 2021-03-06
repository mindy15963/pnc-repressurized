package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class RenderSearchItemBlock {

    private final BlockPos pos;
    private final World world;
    private long lastCheck = 0;
    private int cachedAmount;

    public RenderSearchItemBlock(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public int getSearchedItemCount() {
        // this gets called every frame from the render methods, so some caching is desirable...
        if (world.getTotalWorldTime() - lastCheck >= 20) {
            TileEntity te = world.getTileEntity(pos);
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                int itemCount = 0;
                ItemStack searchStack = ItemPneumaticArmor.getSearchedStack(ClientUtils.getWornArmor(EntityEquipmentSlot.HEAD));
                if (searchStack.isEmpty()) return 0;
                for (int l = 0; l < handler.getSlots(); l++) {
                    if (!handler.getStackInSlot(l).isEmpty()) {
                        itemCount += getSearchedItemCount(handler.getStackInSlot(l), searchStack);
                    }
                }
                cachedAmount = itemCount;
            } else {
                cachedAmount = 0;
            }
            lastCheck = world.getTotalWorldTime();
        }
        return cachedAmount;
    }

    public static int getSearchedItemCount(ItemStack stack, ItemStack searchStack) {
        int itemCount = 0;
        if (stack.isItemEqual(searchStack)) {
            itemCount += stack.getCount();
        }
        List<ItemStack> inventoryItems = PneumaticCraftUtils.getStacksInItem(stack);
        for (ItemStack s : inventoryItems) {
            itemCount += getSearchedItemCount(s, searchStack);
        }
        return itemCount;
    }

    public void renderSearchBlock(int totalCount, float partialTicks) {
        renderSearch(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, getSearchedItemCount(), totalCount, partialTicks);
    }

    public static void renderSearch(double x, double y, double z, int itemCount, int totalCount, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        float f = ((Minecraft.getMinecraft().world.getTotalWorldTime() & 0x1f) + partialTicks) / 5.092f;  // 0 .. 2*pi every 32 ticks
        GlStateManager.color(0, 1, 0, 0.65F + MathHelper.sin(f) * 0.15f);
        GlStateManager.rotate(180.0F - Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F - Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        double ratio = (double) itemCount / totalCount;
        double diff = (1 - ratio) / 1.5D;
        double size = 1 - diff;
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(-size, size, 0).tex(0, 1).endVertex();
        wr.pos(-size, -size, 0).tex(0, 0).endVertex();
        wr.pos(size, -size, 0).tex(1, 0).endVertex();
        wr.pos(size, size, 0).tex(1, 1).endVertex();

        Tessellator.getInstance().draw();

        GlStateManager.popMatrix();
    }
}
