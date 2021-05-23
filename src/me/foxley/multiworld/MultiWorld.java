package me.foxley.multiworld;

import org.bukkit.plugin.java.JavaPlugin;

public class MultiWorld extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getCommand("multiworld").setExecutor(new MultiWorldCommand(this));
    }
}
