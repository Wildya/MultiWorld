package me.foxley.multiworld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class CreateWorldRunnable implements Runnable {

    private final WorldCreator worldCreator;

    public CreateWorldRunnable(final WorldCreator worldCreator_) {
        this.worldCreator = worldCreator_;
    }

    @Override
    public void run() {
        final World world = worldCreator.createWorld();
        if (world != null) {
            Bukkit.broadcastMessage(ChatColor.GREEN + "Le monde " + world.getName() + " a bien été chargé");
        } else {
            Bukkit.broadcastMessage(ChatColor.RED + "Le monde " + worldCreator.name() + " n'a pas pu être chargé");
        }

    }
}
