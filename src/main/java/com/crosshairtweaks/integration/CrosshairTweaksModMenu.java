package com.crosshairtweaks.integration;

import com.crosshairtweaks.gui.CrosshairConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class CrosshairTweaksModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new CrosshairConfigScreen(parent);
    }
}
