package me.desht.pneumaticcraft.proxy;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.AreaShowManager;
import me.desht.pneumaticcraft.client.ClientEventHandler;
import me.desht.pneumaticcraft.client.ClientTickHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiHelmetMainScreen;
import me.desht.pneumaticcraft.client.model.TintedOBJLoader;
import me.desht.pneumaticcraft.client.model.item.ModelProgrammingPuzzle.LoaderProgrammingPuzzle;
import me.desht.pneumaticcraft.client.model.pressureglass.PressureGlassModelLoader;
import me.desht.pneumaticcraft.client.particle.CustomParticleFactory;
import me.desht.pneumaticcraft.client.render.RenderItemMinigun;
import me.desht.pneumaticcraft.client.render.entity.*;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.CoordTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.render.tileentity.*;
import me.desht.pneumaticcraft.client.semiblock.ClientSemiBlockManager;
import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.entity.EntityRing;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityHarvestingDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityLogisticsDrone;
import me.desht.pneumaticcraft.common.entity.projectile.EntityMicromissile;
import me.desht.pneumaticcraft.common.entity.projectile.EntityTumblingBlock;
import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
import me.desht.pneumaticcraft.common.event.HackTickHandler;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.tileentity.*;
import me.desht.pneumaticcraft.common.util.DramaSplash;
import me.desht.pneumaticcraft.lib.EnumCustomParticleType;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ClientProxy implements IProxy {

    private final HackTickHandler clientHackTickHandler = new HackTickHandler();
    public final Map<String, Pair<Integer,KeyModifier>> keybindToKeyCodes = new HashMap<>();

    @Override
    public void preInit() {
        TintedOBJLoader.INSTANCE.addDomain(Names.MOD_ID);
        ModelLoaderRegistry.registerLoader(TintedOBJLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(LoaderProgrammingPuzzle.INSTANCE);
        ModelLoaderRegistry.registerLoader(PressureGlassModelLoader.INSTANCE);

        initTESRs();

        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(HUDHandler.instance());
        MinecraftForge.EVENT_BUS.register(ClientTickHandler.instance());
        MinecraftForge.EVENT_BUS.register(getHackTickHandler());
        MinecraftForge.EVENT_BUS.register(new ClientSemiBlockManager());
        MinecraftForge.EVENT_BUS.register(HUDHandler.instance().getSpecificRenderer(CoordTrackUpgradeHandler.class));
        MinecraftForge.EVENT_BUS.register(AreaShowManager.getInstance());
        MinecraftForge.EVENT_BUS.register(KeyHandler.getInstance());

        ThirdPartyManager.instance().clientPreInit();

        EntityTrackHandler.registerDefaultEntries();

        getAllKeybindsFromOptionsFile();

        RenderingRegistry.registerEntityRenderingHandler(EntityVortex.class, RenderEntityVortex.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityDrone.class, RenderDrone.REGULAR_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityLogisticsDrone.class, RenderDrone.LOGISTICS_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityHarvestingDrone.class, RenderDrone.HARVESTING_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityProgrammableController.class, RenderDrone.REGULAR_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityRing.class, RenderEntityRing.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMicromissile.class, RenderMicromissile.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityTumblingBlock.class, RenderTumblingBlock.FACTORY);
    }

    private void initTESRs() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPressureTube.class, new RenderPressureTubeModule());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAphorismTile.class, new RenderAphorismTile());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAirCannon.class, new RenderAirCannon());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPneumaticDoor.class, new RenderPneumaticDoor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPneumaticDoorBase.class, new RenderPneumaticDoorBase());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssemblyController.class, new RenderAssemblyController());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssemblyIOUnit.class, new RenderAssemblyIOUnit());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssemblyPlatform.class, new RenderAssemblyPlatform());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssemblyLaser.class, new RenderAssemblyLaser());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssemblyDrill.class, new RenderAssemblyDrill());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChargingStation.class, new RenderChargingStation());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityElevatorBase.class, new RenderElevatorBase());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityElevatorCaller.class, new RenderElevatorCaller());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityUniversalSensor.class, new RenderUniversalSensor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityVacuumPump.class, new RenderVacuumPump());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRefinery.class, new RenderRefinery());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLiquidHopper.class, new RenderLiquidHopper());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityKeroseneLamp.class, new RenderKeroseneLamp());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPlasticMixer.class, new RenderPlasticMixer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThermopneumaticProcessingPlant.class, new RenderThermopneumaticProcessingPlant());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySentryTurret.class, new RenderSentryTurret());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySecurityStation.class, new RenderSecurityStation());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPressureChamberValve.class, new RenderPressureChamber());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPressureChamberInterface.class, new RenderPressureChamberInterface());
    }

    @Override
    public void init() {
        for (Fluid fluid : Fluids.FLUIDS) {
            ModelLoader.setBucketModelDefinition(Fluids.getBucket(fluid));
        }

        ThirdPartyManager.instance().clientInit();

        Itemss.MINIGUN.setTileEntityItemStackRenderer(new RenderItemMinigun());
    }

    @Override
    public void postInit() {
        EntityTrackHandler.init();
        GuiHelmetMainScreen.init();
        DramaSplash.getInstance();
    }

    @Override
    public boolean isSneakingInGui() {
        return GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak);
    }

    @Override
    public void initConfig() {
        for (EntityEquipmentSlot slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            for (IUpgradeRenderHandler renderHandler : UpgradeRenderHandlerList.instance().getHandlersForSlot(slot)) {
                renderHandler.initConfig();
            }
        }
    }

    @Override
    public void playCustomParticle(EnumCustomParticleType particleType, World w, double x, double y, double z, double dx, double dy, double dz) {
        Minecraft mc = Minecraft.getMinecraft();
        int part = mc.gameSettings.particleSetting;
        if (part == 2 && mc.world.rand.nextInt(10) > 0) return;
        if (part == 1 && mc.world.rand.nextInt(3) > 0) return;
        Minecraft.getMinecraft().effectRenderer.addEffect(CustomParticleFactory.createParticle(particleType, w, x, y, z, dx, dy, dz));
    }

    @Override
    public String xlate(String key) {
        return I18n.format(key);
    }

    @Override
    public void suppressItemEquipAnimation() {
        ItemRenderer renderer = Minecraft.getMinecraft().entityRenderer.itemRenderer;
        renderer.equippedProgressMainHand = 1;
        renderer.prevEquippedProgressMainHand = 1;
    }

    @Override
    public World getClientWorld() {
        return FMLClientHandler.instance().getClient().world;
    }

    @Override
    public EntityPlayer getClientPlayer() {
        return FMLClientHandler.instance().getClient().player;
    }

    @Override
    public int getArmorRenderID(String armorName) {
        return 0;
    }

    @Override
    public HackTickHandler getHackTickHandler() {
        return clientHackTickHandler;
    }

    @Override
    public void addScheduledTask(Runnable runnable, boolean serverSide) {
        if (serverSide) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnable);
        } else {
            Minecraft.getMinecraft().addScheduledTask(runnable);
        }
    }

    private void getAllKeybindsFromOptionsFile() {
        File optionsFile = new File(FMLClientHandler.instance().getClient().gameDir, "options.txt");
        if (optionsFile.exists()) {
            try (BufferedReader bufferedreader = new BufferedReader(new FileReader(optionsFile))) {
                String s = "";
                while ((s = bufferedreader.readLine()) != null) {
                    String[] str = s.split(":");
                    if (str[0].startsWith("key_")) {
                        KeyModifier mod = str.length > 2 ? KeyModifier.valueFromString(str[2]) : KeyModifier.NONE;
                        keybindToKeyCodes.put(str[0].substring(4), Pair.of(Integer.parseInt(str[1]), mod));
                    }
                }
            } catch (Exception exception1) {
                Log.error("Failed to process options.txt:");
                exception1.printStackTrace();
            }
        }
    }

    @Override
    public int particleLevel() {
        return Minecraft.getMinecraft().gameSettings.particleSetting;
    }

    @Override
    public Pair<Integer, Integer> getScaledScreenSize() {
        return ClientEventHandler.getScaledScreenSize();
    }
}
