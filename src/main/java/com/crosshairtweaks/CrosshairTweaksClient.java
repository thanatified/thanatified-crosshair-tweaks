package com.crosshairtweaks;

import com.crosshairtweaks.config.CrosshairConfig;
import com.crosshairtweaks.gui.CrosshairConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class CrosshairTweaksClient implements ClientModInitializer {

    public static CrosshairConfig CONFIG;

    private static KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        CONFIG = CrosshairConfig.load();

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.crosshairtweaks.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_COMMA,
                Category.create(new Identifier("crosshairtweaks:category"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new CrosshairConfigScreen(null, CONFIG));
                }
            }
        });
    }

    public static MinecraftClient client() {
        return MinecraftClient.getInstance();
    }
}
