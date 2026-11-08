package com.anglehud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AngleHudClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("anglehud");
    private static boolean openMenu = false;

    @Override
    public void onInitializeClient() {
        AngleConfig.load();

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("anglehud", "angle_display"), (graphics, tickCounter) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) return;
            if (mc.screen instanceof AngleScreens.Base) return;
            AngleRenderer.draw(graphics, mc, AngleRenderer.text(mc.player.getXRot()));
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            for (String name : new String[]{"winkel", "angle"}) {
                dispatcher.register(ClientCommandManager.literal(name).executes(ctx -> {
                    openMenu = true;
                    return 1;
                }));
            }
        });

        // Menü erst im nächsten Tick öffnen, sonst schließt der Chat es wieder.
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (openMenu) {
                openMenu = false;
                mc.setScreen(new AngleScreens.MainMenu(null));
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> AngleConfig.save());
    }
}
