package com.ethanicuss.merrymusic;

import com.ethanicuss.merrymusic.register.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MerryMusic.MOD_ID)
public class MerryMusic
{
    public static final String MOD_ID = "merrymusic";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MerryMusic()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();


        ModSounds.registerSounds();

        modEventBus.addListener(this::commonSetup);


        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Merry Music is active!");
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            //ModSounds.registerSounds();
        }
    }
}