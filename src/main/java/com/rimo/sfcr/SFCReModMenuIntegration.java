package com.rimo.sfcr;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class SFCReModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        //return parent -> AutoConfig.getConfigScreen(SFCReConfig.class, parent).get();
    	return parent -> new SFCReConfigScreen().buildScreen();
    }
}
