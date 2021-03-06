package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.AirConUpgradeHandler;
import me.desht.pneumaticcraft.common.config.ArmorHUDLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuiAirConditionerOptions extends IOptionPage.SimpleToggleableOptions {

    public GuiAirConditionerOptions(AirConUpgradeHandler airConUpgradeHandler) {
        super(airConUpgradeHandler);
    }

    @Override
    public void initGui(IGuiScreen gui) {
        super.initGui(gui);

        gui.getButtonList().add(new GuiButton(10, 30, 128, 150, 20, "Move Stat Screen..."));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 10) {
            Minecraft.getMinecraft().player.closeScreen();
            Minecraft.getMinecraft().displayGuiScreen(new GuiMoveStat(getRenderHandler(), ArmorHUDLayout.LayoutTypes.AIR_CON));
        }
    }

}
