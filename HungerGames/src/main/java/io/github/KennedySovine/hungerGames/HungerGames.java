package io.github.KennedySovine.hungerGames;

import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.combat.CombatManager;
import io.github.KennedySovine.hungerGames.command.SubCommand;
import io.github.KennedySovine.hungerGames.game.GameManager;
import io.github.KennedySovine.hungerGames.storage.YamlStorage;
import io.github.KennedySovine.hungerGames.stats.StatsManager;
import io.github.KennedySovine.hungerGames.command.HgCommand;
import io.github.KennedySovine.hungerGames.command.admin.*;
import io.github.KennedySovine.hungerGames.command.player.*;
import io.github.KennedySovine.hungerGames.listener.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class HungerGames extends JavaPlugin {

    private HgCommand hgCommand;

    // Managers
    private YamlStorage storage;
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private CombatManager combatManager;
    private StatsManager statsManager;
    private io.github.KennedySovine.hungerGames.utils.ParticleManager particleManager;
    private io.github.KennedySovine.hungerGames.utils.BorderManager borderManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.storage = new YamlStorage(this);
        this.arenaManager = new ArenaManager(this);
        this.gameManager = new GameManager(this);
        this.combatManager = new CombatManager(this);
        this.statsManager = new StatsManager(this);
        this.particleManager = new io.github.KennedySovine.hungerGames.utils.ParticleManager(this);
        this.borderManager = new io.github.KennedySovine.hungerGames.utils.BorderManager(this);

        // Load arenas from disk
        arenaManager.loadArenas();

        this.hgCommand = new HgCommand(this);

        // Register subcommand skeletons (these classes are lightweight stubs)
        // Register a single 'arena' parent command that dispatches to child arena subcommands
        hgCommand.registerSubCommand("arena", new ArenaParentCommand());
        hgCommand.registerSubCommand("maxplayers", new MaxPlayersCommand());
        hgCommand.registerSubCommand("minplayers", new MinPlayersCommand());
        hgCommand.registerSubCommand("time", new TimeCommand());
        hgCommand.registerSubCommand("centersize", new CenterSizeCommand());
        hgCommand.registerSubCommand("gui", new GuiCommand());
        hgCommand.registerSubCommand("graceperiod", new GracePeriodCommand());
        hgCommand.registerSubCommand("chestrefill", new ChestRefillCommand());
        hgCommand.registerSubCommand("start", new StartCommand());
        hgCommand.registerSubCommand("stop", new StopCommand());
        hgCommand.registerSubCommand("beacons", new BeaconsCommand());
        hgCommand.registerSubCommand("join", new JoinCommand());
        hgCommand.registerSubCommand("leave", new LeaveCommand());
        hgCommand.registerSubCommand("stats", new StatsCommand());
        // other arena subcommands are handled by ArenaParentCommand

        // Register with Bukkit to handle /hg
        if (getCommand("hg") != null) {
            getCommand("hg").setExecutor(hgCommand);
            getCommand("hg").setTabCompleter(hgCommand);
        } else {
            getLogger().severe("Command 'hg' is not defined in plugin.yml!");
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new CombatListener(combatManager), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(combatManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryGuiListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnStickListener(), this);

        getLogger().info("HungerGames plugin enabled (commands & managers registered). Started in dry-skeleton mode.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        // restore any modified world borders
        if (borderManager != null) {
            borderManager.getActiveArenaId().ifPresent(id -> borderManager.clearBorder(id));
        }
        getLogger().info("HungerGames plugin disabled.");
    }

    // Manager accessors for other components and commands
    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public io.github.KennedySovine.hungerGames.utils.ParticleManager getParticleManager() {
        return particleManager;
    }

    public io.github.KennedySovine.hungerGames.utils.BorderManager getBorderManager() {
        return borderManager;
    }

    public YamlStorage getStorage() {
        return storage;
    }
}
