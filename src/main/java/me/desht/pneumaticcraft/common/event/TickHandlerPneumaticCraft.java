package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.common.ai.DroneClaimManager;
import me.desht.pneumaticcraft.common.config.AmadronOfferPeriodicConfig;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketServerTickTime;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TickHandlerPneumaticCraft {

    @SubscribeEvent
    public void onWorldTickEnd(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isRemote) {
            World world = event.world;
            DroneClaimManager.getInstance(world).update();
            if (event.world.getTotalWorldTime() % 100 == 0) {
                double tickTime = net.minecraft.util.math.MathHelper.average(FMLCommonHandler.instance().getMinecraftServerInstance().tickTimeArray) * 1.0E-6D;//In case world are going to get their own thread: MinecraftServer.getServer().worldTickTimes.get(event.world.provider.getDimension())
                NetworkHandler.sendToDimension(new PacketServerTickTime(tickTime), event.world.provider.getDimension());
            }
        }
    }

    @SubscribeEvent
    public void onServerTickEnd(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            int ticks = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
            if (ticks % (24000 / AmadronOfferPeriodicConfig.timesPerDay) == 1) {
                AmadronOfferManager.getInstance().shufflePeriodicOffers();
            }
            if (ticks % 600 == 0) {
                AmadronOfferManager.getInstance().tryRestockCustomOffers();
            }
        }
    }
}
