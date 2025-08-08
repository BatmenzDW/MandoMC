package com.astuteflamez.mandomc.guis.EventsGUI;

import com.astuteflamez.mandomc.features.events.RandomEventScheduler;
import com.astuteflamez.mandomc.features.events.EventType;
import com.astuteflamez.mandomc.features.events.beskar.OreEventManager;
import com.astuteflamez.mandomc.features.events.chesthunt.ChestHuntManager;
import com.astuteflamez.mandomc.features.events.koth.CaptureTracker;
import com.astuteflamez.mandomc.features.events.koth.KothManager;
import com.astuteflamez.mandomc.features.events.koth.KothRegion;
import com.astuteflamez.mandomc.guis.GUIManager;
import com.astuteflamez.mandomc.guis.InventoryButton;
import com.astuteflamez.mandomc.guis.InventoryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EventsHub extends InventoryGUI {

    private final GUIManager guiManager;

    public EventsHub(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    protected org.bukkit.inventory.Inventory createInventory() {
        return Bukkit.createInventory(null, 3 * 9,
                LegacyComponentSerializer.legacyAmpersand().deserialize("&4&lServer Events"));
    }

    @Override
    public void decorate(Player player) {
        addEventStatus();
        super.decorate(player);
    }

    private void addEventStatus() {
        Material mat = Material.BARRIER;
        String title = "&cNo Active Event";
        List<String> lore = new ArrayList<>();
        lore.add("&7Check back later for the next event.");

        // Check real-time state from managers first
        if (KothManager.getInstance().isActive()) {
            var tracker = KothManager.getInstance().getCaptureTracker();
            var region = KothManager.getInstance().getActiveRegion();

            mat = Material.RED_BANNER;
            title = "&a&lKOTH Active!";
            lore.clear();
            lore.add("&fRegion: &e" + (region != null ? region.getName() : "Unknown"));
            lore.add("&fFaction: &e" + (tracker.getCapturingFaction() != null ? tracker.getCapturingFaction().getTag() : "Unknown"));
            lore.add("&fProgress: &e" + String.format("%.1f%%", tracker.getProgressPercent()));
        } else if (ChestHuntManager.getInstance().isActive()) {
            mat = Material.CHEST_MINECART;
            title = "&a&lCrate Rush Active!";
            lore.clear();
            lore.add("&fCrates remaining: &e" + ChestHuntManager.getInstance().getActiveChestCount());
            lore.add("&fLoot them before they disappear!");
        } else if (OreEventManager.getInstance().isEventActive()) {
            mat = Material.NETHERITE_BLOCK;
            title = "&a&lBeskar Mining Active!";
            lore.clear();
            lore.add("&fBeskar remaining: &e" + OreEventManager.getInstance().getRemainingOreCount());
            lore.add("&fMine the beskar hidden in the canyon!");
        } else {
            // No event active — calculate time since last event
            long millisSince = System.currentTimeMillis() - RandomEventScheduler.getInstance().getLastEventEndTime();

            long seconds = (millisSince / 1000) % 60;
            long minutes = (millisSince / (1000 * 60)) % 60;
            long hours = millisSince / (1000 * 60 * 60);

            String timeAgo = String.format("&7Last event ended &b%dh %dm %ds &7ago.", hours, minutes, seconds);
            lore.add(timeAgo);
        }

        ItemStack item = createItem(mat, title, lore);
        this.addButton(13, createEventButton(item));
    }

    private ItemStack createItem(Material mat, String title, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(title));

            List<Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                loreComponents.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
            }
            meta.lore(loreComponents);
            item.setItemMeta(meta);
        }
        return item;
    }

    private InventoryButton createEventButton(ItemStack itemStack) {
        return new InventoryButton()
                .creator(player -> itemStack)
                .consumer(event -> {
                    // Click action (can be added later)
                });
    }
}
