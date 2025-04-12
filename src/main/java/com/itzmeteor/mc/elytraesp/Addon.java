package com.itzmeteor.mc.elytraesp;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("iTzMeteor");

    @Override
    public void onInitialize() {
        Modules.get().add(new ElytraESP());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.itzmeteor.mc.elytraesp";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("therealmeteor", "elytra-esp");
    }
}
