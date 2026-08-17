package com.crosshairtweaks.integration;

import com.crosshairtweaks.CrosshairTweaksClient;
import com.crosshairtweaks.gui.CrosshairConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * This class is only ever loaded if Mod Menu is installed - fabric.mod.json
 * declares it under the "modmenu" entrypoint, and Mod Menu is the only thing
 * that ever calls that entrypoint. If Mod Menu isn't present, this class is
 * simply never touched, so it's safe for it to reference Mod Menu's API
 * classes without Mod Menu being a hard dependency of the mod.
 */
public class CrosshairTweaksModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> new CrosshairConfigScreen(parent, CrosshairTweaksClient.CONFIG);
	}
}
