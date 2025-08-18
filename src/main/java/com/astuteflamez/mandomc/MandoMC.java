package com.astuteflamez.mandomc;

import com.astuteflamez.mandomc.commands.*;
import com.astuteflamez.mandomc.database.EventRewardsTable;
import com.astuteflamez.mandomc.database.ItemRewardsTable;
import com.astuteflamez.mandomc.database.PlayerQuestsTable;
import com.astuteflamez.mandomc.database.QuestsTable;
import com.astuteflamez.mandomc.features.blasters.AmmoRecipes;
import com.astuteflamez.mandomc.features.blasters.SpawnPvP;
import com.astuteflamez.mandomc.features.events.EventsConfig;
import com.astuteflamez.mandomc.features.events.RandomEventScheduler;
import com.astuteflamez.mandomc.features.events.beskar.*;
import com.astuteflamez.mandomc.features.events.chesthunt.ChestHuntCommands;
import com.astuteflamez.mandomc.features.events.chesthunt.ChestHuntListener;
import com.astuteflamez.mandomc.features.events.chesthunt.ChestHuntManager;
import com.astuteflamez.mandomc.features.events.chesthunt.ChestHuntWand;
import com.astuteflamez.mandomc.features.events.koth.KothCommand;
import com.astuteflamez.mandomc.features.events.koth.KothManager;
import com.astuteflamez.mandomc.features.events.rhydonium.*;
import com.astuteflamez.mandomc.features.items.*;
import com.astuteflamez.mandomc.features.sabers.*;
import com.astuteflamez.mandomc.features.vehicles.*;
import com.astuteflamez.mandomc.features.vehicles.listeners.*;
import com.astuteflamez.mandomc.features.warps.WarpCommand;
import com.astuteflamez.mandomc.features.warps.WarpConfig;
import com.astuteflamez.mandomc.guis.GUIListener;
import com.astuteflamez.mandomc.guis.GUIManager;
import com.astuteflamez.mandomc.listeners.GlobalRestrictions;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class MandoMC extends JavaPlugin {

    public static MandoMC instance;
    public static String PREFIX;

    private ItemsManager itemsManager;

    // ✅ just initialize it here, no constructor needed
    private final HashMap<UUID, Long> lightsaberCooldown = new HashMap<>();

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
        PacketEvents.getAPI().getEventManager().registerListener(
                new BoostListener(), PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().getEventManager().registerListener(
                new InputListener(), PacketListenerPriority.NORMAL);
    }

    @Override
    public void onEnable() {
        instance = this;
        PacketEvents.getAPI().init();

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        GUIManager guiManager = new GUIManager();
        GUIListener guiListener = new GUIListener(guiManager);
        Bukkit.getPluginManager().registerEvents(guiListener, this);

        // ✅ Setup configs
        LangConfig.setup();
        LangConfig.get().options().copyDefaults(true);
        LangConfig.save();
        PREFIX = color(LangConfig.get().getString("Prefix", "§7[Plugin] ")); // ✅ safe

        WarpConfig.setup();
        WarpConfig.get().options().copyDefaults(true);
        WarpConfig.save();
        getCommand("warps").setExecutor(new WarpCommand(guiManager));

        EventsConfig.setup();
        EventsConfig.get().options().copyDefaults(true);
        EventsConfig.save();
        KothManager.getInstance();

        ItemsConfig.setup();
        ItemsConfig.get().options().copyDefaults(true);
        ItemsConfig.save();
        this.itemsManager = new ItemsManager(this);
        itemsManager.loadItems();
        itemsManager.registerRecipes();
        new BlastingRecipes(this, itemsManager).register();
        getCommand("giveitem").setExecutor(new GiveItemCommand(this));
        getCommand("recipes").setExecutor(new RecipesCommand(guiManager));
        getServer().getPluginManager().registerEvents(new SaberThrowListener(lightsaberCooldown), this);
        getServer().getPluginManager().registerEvents(new SaberHitListener(), this);
        getServer().getPluginManager().registerEvents(new SaberToggleListener(itemsManager), this);

        getCommand("events").setExecutor(new EventsCommand(guiManager));
        Bukkit.getPluginManager().registerEvents(new ChestHuntWand(), this);
        Bukkit.getPluginManager().registerEvents(new ChestHuntListener(), this);
        getCommand("chesthunt").setExecutor(new ChestHuntCommands());
        Bukkit.getPluginManager().registerEvents(new ChestHuntListener(), this);
        getCommand("koth").setExecutor(new KothCommand());
        getCommand("oreevent").setExecutor(new OreEventCommands());
        getServer().getPluginManager().registerEvents(new OreEventWand(), this);
        getServer().getPluginManager().registerEvents(new OreEventListener(), this);
        OreLocationManager.getInstance().load();
        OreEventManager.getInstance();
        getCommand("rhydonium").setExecutor(new RhydoniumCommands());
        getServer().getPluginManager().registerEvents(new RhydoniumWand(), this);
        getServer().getPluginManager().registerEvents(new WorldLoadListener(), this);
        getServer().getPluginManager().registerEvents(new RhydoniumFuelTransferListener(), this);
        RhydoniumManager.init(MandoMC.getInstance());
        RhydoniumLocationManager.getInstance().load();
        RhydoniumManager.getInstance().enable();

        getServer().getPluginManager().registerEvents(new SpawnPvP(), this);
        new AmmoRecipes(this, itemsManager).registerAmmoRecipes();

        getServer().getPluginManager().registerEvents(new EnterListener(guiManager), this);
        getServer().getPluginManager().registerEvents(new ExitListener(), this);
        getServer().getPluginManager().registerEvents(new SafetyListener(), this);
        getServer().getPluginManager().registerEvents(new ShootListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnListener(), this);
        new VehicleRunnable().runTaskTimer(this, 0L, 1L);

        RandomEventScheduler.getInstance().start();

        initializeDatabase();

        // ✅ Register commands
        getCommand("mmcreload").setExecutor(new ReloadCommand(this));
        getCommand("discord").setExecutor(new DiscordCommand());

        // ✅ Register listeners
        getServer().getPluginManager().registerEvents(new GlobalRestrictions(this), this);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Status.Server.SERVER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                WrappedServerPing packet = event.getPacket().getServerPings().read(0);

                packet.setPlayersMaximum(66);

                packet.setVersionName(getConfig().getString("VersionName"));
                String motdRaw = getConfig().getString("MOTD"); // e.g. "&l&4Welcome"
                assert motdRaw != null;
                String motdLegacy = ChatColor.translateAlternateColorCodes('&', motdRaw);

                packet.setMotD(motdLegacy);


                // Create WrappedGameProfile objects for the custom player list
                List<WrappedGameProfile> profiles = new ArrayList<>();
                for (int i = 0; i < getConfig().getStringList("ServerListHoverText").size(); i++) {
                    String hoverText = getConfig().getStringList("ServerListHoverText").get(i);
                    UUID uuid = UUID.randomUUID(); // Generate a random UUID
                    profiles.add(new WrappedGameProfile(uuid.toString(), hoverText));
                }

                packet.setPlayers(profiles);

                event.getPacket().getServerPings().write(0, packet);
            }
        });

        getServer().getConsoleSender().sendMessage("[MandoMC]: Plugin is enabled");
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        getServer().getConsoleSender().sendMessage("[MandoMC]: Plugin is disabled");
        ChestHuntManager.getInstance().stopChestHunt();
        OreEventManager.getInstance().stop();

        for(Player player : Bukkit.getOnlinePlayers()) {
            Vehicle vehicle = VehicleManager.get(player);
            if (vehicle != null) {
                vehicle.getEntity().remove();
                vehicle.getZombie().remove();
                vehicle.returnVehicle();
                VehicleManager.unregister(player);
            }
        }
    }

    public void initializeDatabase(){
        try {
            PlayerQuestsTable.initializePlayerQuestsTable();
            QuestsTable.initializeQuestsTable();
            ItemRewardsTable.initializeItemRewardsTable();
            EventRewardsTable.initializeEventRewardsTable();
        } catch (SQLException e) {
            getServer().getConsoleSender().sendMessage("[MandoMC]: Database initialization failed: " + e.getMessage());
        }
    }

    public ItemsManager getItemsManager() {
        return itemsManager;
    }

    public static MandoMC getInstance() {
        return instance;
    }

    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
