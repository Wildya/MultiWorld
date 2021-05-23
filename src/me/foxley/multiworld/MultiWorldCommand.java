package me.foxley.multiworld;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MultiWorldCommand implements TabExecutor {

    private static final String WORLD_NOT_FOUND = ChatColor.RED + "Le monde n'a pas été trouvé";
    private static final String WORLD_NOT_LOADED = ChatColor.RED + "Le monde n'est pas chargé";
    private static final String WORLD_ALREADY_LOADED = ChatColor.RED + "Le monde est déjà chargé";
    private static final String WORLD_IS_LOADING = ChatColor.GREEN + "Le monde est en train de charger";

    final MultiWorld multiWorld;

    MultiWorldCommand(final MultiWorld multiWorld_) {
        this.multiWorld = multiWorld_;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        if (args.length > 0 && sender instanceof Player) {
            final Player player = (Player) sender;

            try {
                final MultiWorldSubCommand subCommand = MultiWorldSubCommand.valueOf(args[0].toUpperCase());
                subCommand.performCommand(multiWorld, player, args);
            } catch (final IllegalArgumentException exception) {
                MultiWorldSubCommand.sendHelpCommand(sender);
            }
        } else {
            MultiWorldSubCommand.sendHelpCommand(sender);
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        final List<String> suggestionList;
        if (args.length == 1) {
            suggestionList = Arrays.stream(MultiWorldSubCommand.values())
                    .map(multiWorldSubCommand -> multiWorldSubCommand.name().toLowerCase())
                    .filter(worldName -> worldName.startsWith(args[0]))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            suggestionList = Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(worldName -> worldName.startsWith(args[1]))
                    .collect(Collectors.toList());
        } else {
            suggestionList = null;
        }

        return suggestionList;
    }

    enum MultiWorldSubCommand {
        LIST((byte) 0, "Liste tous les mondes existants") {
            @Override
            void performCommandSafely(MultiWorld multiWorld, Player player, String[] args) {
                final StringBuilder stringBuilderMemory = new StringBuilder("Liste des mondes chargés en mémoire :");
                final List<World> worldList = Bukkit.getWorlds();
                for (final World world : worldList) {
                    stringBuilderMemory.append(" ").append(world.getName());
                }
                player.sendMessage(stringBuilderMemory.toString());


                final StringBuilder stringBuilderDisk = new StringBuilder("Liste des mondes sur le disque :");
                final File[] worldListFolder = Bukkit.getWorldContainer().listFiles();
                if (worldListFolder != null) {
                    for (final File worldFolder : worldListFolder) {
                        if (worldFolder.exists() && worldFolder.isDirectory()) {
                            final File levelDatFile = new File(worldFolder, "level.dat");
                            if (levelDatFile.exists() && levelDatFile.isFile()) {
                                stringBuilderDisk.append(" ").append(worldFolder.getName());
                            }
                        }
                    }
                    player.sendMessage(stringBuilderDisk.toString());
                } else {
                    player.sendMessage(ChatColor.RED + "Impossible de récupérer le dossier des mondes:");
                }
            }
        },
        TP((byte) 1, "Téléporte à un monde") {
            @Override
            void performCommandSafely(MultiWorld multiWorld, Player player, String[] args) {
                final World world = Bukkit.getWorld(args[1]);
                if (world != null) {
                    player.teleport(world.getSpawnLocation());
                } else {
                    player.sendMessage(WORLD_NOT_LOADED);
                }
            }
        },
        LOAD((byte) 1, "Charge un monde en mémoire") {
            @Override
            void performCommandSafely(MultiWorld multiWorld, Player player, String[] args) {
                final World world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    if (checkIfWorldFolderExists(args[1])) {
                        final WorldCreator worldCreator = new WorldCreator(args[1]);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(multiWorld, new CreateWorldRunnable(worldCreator));
                        player.sendMessage(WORLD_IS_LOADING);
                    } else {
                        player.sendMessage(WORLD_NOT_FOUND);
                    }
                } else {
                    player.sendMessage(WORLD_ALREADY_LOADED);
                }
            }
        },
        CREATE((byte) 1, "Crée un nouveau monde vide") {
            @Override
            void performCommandSafely(MultiWorld multiWorld, Player player, String[] args) {
                final World world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    if (!checkIfWorldFolderExists(args[1])) {
                        final WorldCreator worldCreator = new WorldCreator(args[1]);
                        worldCreator.type(WorldType.FLAT);
                        worldCreator.generatorSettings("2;0;1;");
                        Bukkit.getScheduler().scheduleSyncDelayedTask(multiWorld, new CreateWorldRunnable(worldCreator));
                        player.sendMessage(WORLD_IS_LOADING);
                    } else {
                        player.sendMessage(ChatColor.RED + "Un monde avec le même monde existe déjà");
                    }
                } else {
                    player.sendMessage(WORLD_ALREADY_LOADED);
                }
            }
        };

        private final byte minimalArgsSize;
        private final String description;

        MultiWorldSubCommand(final byte minimalArgsSize_, final String description_) {
            this.minimalArgsSize = minimalArgsSize_;
            description = description_;
        }

        public final void performCommand(final MultiWorld multiWorld, final Player player, final String[] args) {
            if (minimalArgsSize <= args.length) {
                performCommandSafely(multiWorld, player, args);
            } else {
                player.sendMessage(ChatColor.RED + "Il faut " + minimalArgsSize + " paramètre");
            }
        }

        abstract void performCommandSafely(final MultiWorld multiWorld, final Player player, final String[] args);

        public static void sendHelpCommand(final CommandSender player) {
            player.sendMessage("--- Menu d'aide MultiWorld ---");
            for (final MultiWorldSubCommand subCommand : MultiWorldSubCommand.values()) {
                player.sendMessage(" - /mw " + subCommand.name().toLowerCase() + " : " + subCommand.description);
            }
        }

        protected static boolean checkIfWorldFolderExists(final String worldName) {
            final File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            final boolean levelDatFileExists;

            if (worldFolder.exists() && worldFolder.isDirectory()) {
                final File levelDatFile = new File(worldFolder, "level.dat");
                levelDatFileExists = levelDatFile.exists() && levelDatFile.isFile();
            } else {
                levelDatFileExists = false;
            }

            return levelDatFileExists;
        }
    }
}
