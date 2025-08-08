package com.astuteflamez.mandomc.features.events.koth;

import com.astuteflamez.mandomc.LangConfig;
import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.features.events.EventsConfig;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class CaptureTracker {

    private static final String PREFIX = "§4§lEvents §8» §7";

    private final KothRegion region;
    private final int captureTimeSeconds;

    private Faction capturingFaction = null;
    private int progressSeconds = 0;

    private Player capturingPlayer = null;

    private BossBar bossBar;
    private BukkitRunnable captureTask;

    private final String contest = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("KothContest"));
    private final String capturing = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("KothCapturing"));
    private final String captured = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("KothCaptured"));

    public CaptureTracker(KothRegion region) {
        this.region = region;
        this.captureTimeSeconds = EventsConfig.get().getInt("events.koth.capture-time-seconds", 60);
        startTracking();
        region.showRegionParticles();
    }

    private void startTracking() {
        captureTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!KothManager.getInstance().isActive() || !KothManager.getInstance().getActiveRegion().equals(region)) {
                    stopTracking();
                    return;
                }

                Faction factionInZone = null;
                Player lastPlayerInZone = null;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!region.isInRegion(player.getLocation())) continue;

                    FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);
                    if (fplayer == null) continue;

                    Faction faction = fplayer.getFaction();
                    if (faction == null || faction.isWilderness()) continue;

                    if (factionInZone == null) {
                        factionInZone = faction;
                        lastPlayerInZone = player;
                    } else if (!factionInZone.equals(faction)) {
                        Bukkit.broadcastMessage(contest.replace("{var}", region.getName()));
                        resetCapture();
                        return;
                    } else {
                        lastPlayerInZone = player;
                    }
                }

                if (factionInZone == null) {
                    resetCapture();
                    return;
                }

                if (!factionInZone.equals(capturingFaction)) {
                    capturingFaction = factionInZone;
                    capturingPlayer = lastPlayerInZone;
                    progressSeconds = 0;
                    createBossBar();
                    Bukkit.broadcastMessage(capturing.replace("{var}", capturingFaction.getTag())
                            .replace("{var1}", region.getName()));
                } else {
                    capturingPlayer = lastPlayerInZone;
                    progressSeconds++;
                }

                double progressPercent = (progressSeconds * 100.0) / captureTimeSeconds;
                updateBossBar(progressPercent, capturingFaction.getTag());

                if (progressSeconds >= captureTimeSeconds) {
                    endCapture();
                    stopTracking();
                }
            }
        };

        captureTask.runTaskTimer(MandoMC.getInstance(), 0L, 20L);
    }

    public void stopTracking() {
        if (captureTask != null) {
            captureTask.cancel();
            captureTask = null;
        }
        removeBossBar();
    }

    private void resetCapture() {
        capturingFaction = null;
        capturingPlayer = null;
        progressSeconds = 0;
        removeBossBar();
    }

    private void createBossBar() {
        removeBossBar();

        String name = "§4§lKOTH §c[{region}]".replace("{region}", capturingFaction.getTag());
        bossBar = BossBar.bossBar(
                Component.text(name),
                0f,
                BossBar.Color.RED,
                BossBar.Overlay.PROGRESS
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showBossBar(bossBar);
        }
    }

    private void updateBossBar(double percent, String factionTag) {
        if (bossBar == null) return;

        String part = "§4§lKOTH §c[{region}]".replace("{region}", factionTag);
        bossBar.name(Component.text(part + " " + String.format("%.1f%%", percent)).color(NamedTextColor.GREEN));
        bossBar.progress((float) Math.min(1.0, percent / 100.0));
    }

    private void removeBossBar() {
        if (bossBar == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.hideBossBar(bossBar);
        }
        bossBar = null;
    }

    private void endCapture() {
        removeBossBar();

        Bukkit.broadcastMessage(captured.replace("{var}", capturingFaction.getTag())
                .replace("{var1}", capturingPlayer != null ? capturingPlayer.getName() : "Unknown")
                .replace("{var2}", region.getName()));

        List<String> commands = EventsConfig.get().getStringList("events.koth.rewards");

        // Reward all online players in the winning faction
        for (FPlayer fPlayer : capturingFaction.getFPlayers()) {
            Player player = fPlayer.getPlayer();
            if (player != null && player.isOnline()) {
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);
                for (String cmd : commands) {
                    String parsed = cmd.replace("%player%", player.getName())
                            .replace("%faction%", capturingFaction.getTag());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
                }
            }
        }

        KothManager.getInstance().endActiveKoth();
    }


    public double getProgressPercent() {
        return progressSeconds * 100.0 / captureTimeSeconds;
    }

    public Faction getCapturingFaction() {
        return capturingFaction;
    }
}
