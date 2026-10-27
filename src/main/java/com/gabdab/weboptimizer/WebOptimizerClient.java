package com.gabdab.weboptimizer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class WebOptimizerClient implements ClientModInitializer {
    private KeyBinding openHudKey;

    @Override
    public void onInitializeClient() {
        WebConfig.load();

        // Use Minecraft's keybinding system instead of polling GLFW twice per tick.
        // This makes the HUD key reliable and visible in Options > Controls.
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("weboptimizer", "main"));
        openHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.weboptimizer.open_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category
        ));

        ClientTickEvents.START_CLIENT_TICK.register(client -> Core.tick(client));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Core.afterTick(client);
            while (openHudKey.wasPressed()) {
                if (client.currentScreen instanceof WebScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null && client.player != null) {
                    client.setScreen(new WebScreen());
                }
            }
        });

        // remember when the player hits something (0.2 s pause before the next web)
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && player == MinecraftClient.getInstance().player) {
                WebLogic.markAttack();
            }
            return ActionResult.PASS;
        });
    }

}
