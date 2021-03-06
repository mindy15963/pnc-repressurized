package me.desht.pneumaticcraft.client.model.block;

import me.desht.pneumaticcraft.client.render.tileentity.AbstractModelRenderer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class ModelVacuumPump extends AbstractModelRenderer.BaseModel {
    private final ModelRenderer turbineCase;
    private final ModelRenderer top;
    private final ModelRenderer blade;

    public ModelVacuumPump() {
        textureWidth = 64;
        textureHeight = 64;

        turbineCase = new ModelRenderer(this, 0, 47);
        turbineCase.addBox(0F, 0F, 0F, 1, 4, 1);
        turbineCase.setRotationPoint(-0.5F, 14.1F, 0F);
        turbineCase.setTextureSize(64, 64);
        turbineCase.mirror = true;
        setRotation(turbineCase, 0F, 0F, 0F);
        top = new ModelRenderer(this, 0, 47);
        top.addBox(0F, 0F, 0F, 6, 1, 12);
        top.setRotationPoint(-3F, 13F, -6F);
        top.setTextureSize(64, 64);
        top.mirror = true;
        setRotation(top, 0F, 0F, 0F);
        blade = new ModelRenderer(this, 0, 0);
        blade.addBox(0F, 0F, 0F, 1, 4, 2);
        blade.setRotationPoint(-0.5F, 14F, -3F);
        blade.setTextureSize(64, 64);
        blade.mirror = true;
        setRotation(blade, 0F, 0F, 0F);
    }

    private static final int BLADE_COUNT = 3;
    private static final int CASE_POINTS = 20;

    public void renderModel(float size, float rotation) {
        rotation++;
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 3D / 16D);
        for (int i = 0; i < BLADE_COUNT; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(rotation * 2 + (i + 0.5F) / BLADE_COUNT * 360, 0, 1, 0);
            GlStateManager.translate(0, 0, 1D / 16D);
            blade.render(size);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();

        GlStateManager.rotate(180, 0, 1, 0);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 3D / 16D);
        for (int i = 0; i < BLADE_COUNT; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-rotation * 2 + (float) i / (float) BLADE_COUNT * 360, 0, 1, 0);
            GlStateManager.translate(0, 0, 1D / 16D);
            blade.render(size);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();

        GlStateManager.disableTexture2D();
        GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
        GlStateManager.pushMatrix();
        for (int i = 0; i < CASE_POINTS; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 3F / 16F);
            GlStateManager.rotate((float) i / (float) CASE_POINTS * 275F - 130, 0, 1, 0);
            GlStateManager.translate(0, 0, 2.5F / 16F);
            turbineCase.render(size);
            GlStateManager.popMatrix();
        }
        GlStateManager.rotate(180, 0, 1, 0);
        for (int i = 0; i < CASE_POINTS; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 3F / 16F);
            GlStateManager.rotate((float) i / (float) CASE_POINTS * 275F - 130, 0, 1, 0);
            GlStateManager.translate(0, 0, 2.5F / 16F);
            turbineCase.render(size);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1, 1, 1, 0.4F);
        top.render(size);
        GlStateManager.disableBlend();

        drawPlusAndMinus();

        GlStateManager.enableTexture2D();
    }

    private void drawPlusAndMinus() {
        double scale = 0.05D;
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.26D, 13.95D / 16D, 0);
        GlStateManager.rotate(90, 1, 0, 0);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.color(0, 1, 0, 1);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(-1, 0, 0).endVertex();
        bufferBuilder.pos(1, 0, 0).endVertex();
        bufferBuilder.pos(0, -1, 0).endVertex();
        bufferBuilder.pos(0, 1, 0).endVertex();
        tessellator.draw();
        GlStateManager.translate(-0.52D / scale, 0, 0);
        GlStateManager.color(1, 0, 0, 1);
        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(-1, 0, 0).endVertex();
        bufferBuilder.pos(1, 0, 0).endVertex();
        tessellator.draw();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.popMatrix();
    }
}
