package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiInventorySearcher;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.GuiSearcher;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetItemFilter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.IOException;
import java.util.Arrays;

public class GuiProgWidgetItemFilter extends GuiProgWidgetOptionBase {
    private GuiSearcher searchGui;
    private GuiInventorySearcher invSearchGui;
    private GuiCheckBox checkBoxUseDamage;
    private GuiCheckBox checkBoxUseNBT;
    private GuiCheckBox checkBoxUseOreDict;
    private GuiCheckBox checkBoxUseModSimilarity;
    private GuiCheckBox checkBoxMatchBlock;
    private final ProgWidgetItemFilter filterWidget;
    private GuiButton incButton, decButton;
    private WidgetComboBox variableField;

    public GuiProgWidgetItemFilter(IProgWidget widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
        filterWidget = (ProgWidgetItemFilter) widget;
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.add(new GuiButton(0, guiLeft + 4, guiTop + 20, 70, 20, "Search item..."));
        buttonList.add(new GuiButton(1, guiLeft + 78, guiTop + 20, 100, 20, "Search inventory..."));
        decButton = new GuiButton(2, guiLeft + 140, guiTop + 87, 10, 12, "-");
        incButton = new GuiButton(3, guiLeft + 167, guiTop + 87, 10, 12, "+");
        buttonList.add(decButton);
        buttonList.add(incButton);
        checkBoxUseDamage = new GuiCheckBox(0, guiLeft + 4, guiTop + 72, 0xFF404040, "Use metadata / damage values");
        checkBoxUseDamage.setTooltip(Arrays.asList("Check to handle differently damaged tools", "or different colors of Wool as different."));
        checkBoxUseDamage.checked = filterWidget.useMetadata;
        addWidget(checkBoxUseDamage);
        checkBoxUseNBT = new GuiCheckBox(2, guiLeft + 4, guiTop + 108, 0xFF404040, "Use NBT");
        checkBoxUseNBT.setTooltip(Arrays.asList("Check to handle items like Enchanted Books", "or Firework as different."));
        checkBoxUseNBT.checked = filterWidget.useNBT;
        addWidget(checkBoxUseNBT);
        checkBoxUseOreDict = new GuiCheckBox(3, guiLeft + 4, guiTop + 120, 0xFF404040, "Use Ore Dictionary");
        checkBoxUseOreDict.setTooltip(Arrays.asList("Check to handle items registered in the", "Ore Dictionary as the same."));
        checkBoxUseOreDict.checked = filterWidget.useOreDict;
        addWidget(checkBoxUseOreDict);
        checkBoxUseModSimilarity = new GuiCheckBox(4, guiLeft + 4, guiTop + 132, 0xFF404040, "Use Mod similarity");
        checkBoxUseModSimilarity.setTooltip(Arrays.asList("Check to handle items from the", "same mod as the same."));
        checkBoxUseModSimilarity.checked = filterWidget.useModSimilarity;
        addWidget(checkBoxUseModSimilarity);
        checkBoxMatchBlock = new GuiCheckBox(5, guiLeft + 4, guiTop + 144, 0xFF404040, "Match by Block");
        checkBoxMatchBlock.setTooltip(Arrays.asList("Check to match by block instead of", "dropped item. Useful for blocks", "which don't drop an item.", TextFormatting.GRAY.toString() + TextFormatting.ITALIC + "Only used by the 'Dig' programming piece."));
        checkBoxMatchBlock.checked = filterWidget.matchBlock;
        addWidget(checkBoxMatchBlock);

        variableField = new WidgetComboBox(fontRenderer, guiLeft + 90, guiTop + 56, 80, fontRenderer.FONT_HEIGHT + 1);
        variableField.setElements(guiProgrammer.te.getAllVariables());
        variableField.setText(filterWidget.getVariable());

        if (ConfigHandler.getProgrammerDifficulty() == 2) {
            addWidget(variableField);
        }

        checkBoxUseDamage.enabled = !checkBoxUseOreDict.checked && !checkBoxUseModSimilarity.checked;
        incButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
        decButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
        checkBoxUseNBT.enabled = !checkBoxUseOreDict.checked && !checkBoxUseModSimilarity.checked && !checkBoxMatchBlock.checked;
        checkBoxUseOreDict.enabled = !checkBoxUseModSimilarity.checked && !checkBoxMatchBlock.checked;
        checkBoxUseModSimilarity.enabled = !checkBoxUseOreDict.checked && !checkBoxMatchBlock.checked;
        checkBoxMatchBlock.enabled = !checkBoxUseNBT.checked && !checkBoxUseModSimilarity.checked && !checkBoxUseOreDict.checked;

        if (searchGui != null) filterWidget.setFilter(searchGui.getSearchStack());
        if (invSearchGui != null) filterWidget.setFilter(invSearchGui.getSearchStack());
    }

    @Override
    public void keyTyped(char key, int keyCode) throws IOException {
        if (keyCode == 1) {
            filterWidget.setVariable(variableField.getText());
        }
        super.keyTyped(key, keyCode);
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            searchGui = new GuiSearcher(FMLClientHandler.instance().getClient().player);
            searchGui.setSearchStack(filterWidget.getFilter());
            FMLClientHandler.instance().showGuiScreen(searchGui);
        } else if (button.id == 1) {
            invSearchGui = new GuiInventorySearcher(FMLClientHandler.instance().getClient().player);
            invSearchGui.setSearchStack(filterWidget.getFilter());
            FMLClientHandler.instance().showGuiScreen(invSearchGui);
        } else if (button.id == 2) {
            if (--filterWidget.specificMeta < 0) filterWidget.specificMeta = 15;
        } else if (button.id == 3) {
            if (++filterWidget.specificMeta > 15) filterWidget.specificMeta = 0;
        }
        super.actionPerformed(button);
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget) {
        if (guiWidget instanceof GuiCheckBox) {
            GuiCheckBox checkBox = (GuiCheckBox) guiWidget;
            switch (checkBox.getID()) {
                case 0:
                    filterWidget.useMetadata = checkBox.checked;
                    incButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
                    decButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
                    break;
                case 2:
                    filterWidget.useNBT = checkBox.checked;
                    checkBoxMatchBlock.enabled = !checkBox.checked;
                    break;
                case 3:
                    filterWidget.useOreDict = checkBox.checked;
                    checkBoxUseDamage.enabled = !checkBox.checked;
                    checkBoxUseNBT.enabled = !checkBox.checked;
                    checkBoxUseModSimilarity.enabled = !checkBox.checked;
                    checkBoxMatchBlock.enabled = !checkBox.checked;
                    incButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
                    decButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
                    break;
                case 4:
                    filterWidget.useModSimilarity = checkBox.checked;
                    checkBoxUseDamage.enabled = !checkBox.checked;
                    checkBoxUseNBT.enabled = !checkBox.checked;
                    checkBoxUseOreDict.enabled = !checkBox.checked;
                    checkBoxMatchBlock.enabled = !checkBox.checked;
                    incButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
                    decButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
                    break;
                case 5:
                    filterWidget.matchBlock = checkBox.checked;
                    checkBoxUseModSimilarity.enabled = !checkBox.checked;
                    checkBoxUseNBT.enabled = !checkBox.checked;
                    checkBoxUseOreDict.enabled = !checkBox.checked;
                    break;
            }
        }
        super.actionPerformed(guiWidget);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        String value = String.valueOf(filterWidget.specificMeta);
        fontRenderer.drawString(value, guiLeft + 158 - fontRenderer.getStringWidth(value) / 2, guiTop + 90, checkBoxUseDamage.enabled && checkBoxUseDamage.checked ? 0xFF404040 : 0xFF888888);
        fontRenderer.drawString("Specific block metadata:", guiLeft + 14, guiTop + 90, checkBoxUseDamage.enabled && checkBoxUseDamage.checked ? 0xFF404040 : 0xFF888888);
        if (ConfigHandler.getProgrammerDifficulty() == 2)
            fontRenderer.drawString("Variable:", guiLeft + 90, guiTop + 45, 0xFF404040);
        fontRenderer.drawString("Filter:", guiLeft + 10, guiTop + 53, 0xFF404040);

        String oldVarName = filterWidget.getVariable();
        filterWidget.setVariable("");
        if (!filterWidget.getFilter().isEmpty())
            ProgWidgetItemFilter.drawItemStack(filterWidget.getFilter(), guiLeft + 50, guiTop + 48, "");
        filterWidget.setVariable(oldVarName);
    }
}
