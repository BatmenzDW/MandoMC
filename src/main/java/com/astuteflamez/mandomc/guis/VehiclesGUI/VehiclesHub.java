package com.astuteflamez.mandomc.guis.VehiclesGUI;

import com.astuteflamez.mandomc.features.events.rhydonium.RhydoniumCanisterUtil;
import com.astuteflamez.mandomc.features.vehicles.Vehicle;
import com.astuteflamez.mandomc.features.vehicles.VehicleManager;
import com.astuteflamez.mandomc.guis.GUIManager;
import com.astuteflamez.mandomc.guis.InventoryButton;
import com.astuteflamez.mandomc.guis.InventoryGUI;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class VehiclesHub extends InventoryGUI {

    private final GUIManager guiManager;

    public VehiclesHub(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(
                null,
                5 * 9,
                LegacyComponentSerializer.legacyAmpersand().deserialize("&6&lVehicles")
        );
    }

    @Override
    public void decorate(Player player) {
        Vehicle vehicle = VehicleManager.get(player);
        if (vehicle == null) {
            // No vehicle registered
            this.addButton(22, new InventoryButton()
                    .creator(p -> {
                        ItemStack info = new ItemStack(Material.BARRIER);
                        ItemMeta m = info.getItemMeta();
                        m.setDisplayName("§cNo Vehicle Found");
                        List<String> lore = new ArrayList<>();
                        lore.add("§7You don't have a vehicle registered right now.");
                        lore.add("§7Spawn one first, then reopen this menu.");
                        m.setLore(lore);
                        info.setItemMeta(m);
                        return info;
                    })
                    .consumer(e -> e.getWhoClicked().closeInventory()));
            super.decorate(player);
            return;
        }

        // Main actions
        this.addButton(13, createFlyButton());        // moved left by 1
        this.addButton(31, createFuelButton());

        super.decorate(player);
    }

    // ========= Buttons =========

    private InventoryButton createFlyButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack icon = new ItemStack(Material.STONE_BUTTON);
                    ItemMeta meta = icon.getItemMeta();

                    Vehicle v = VehicleManager.get(player);
                    int max = (v != null) ? getMaxFuel(v) : 1000;
                    int fuel = (v != null) ? clamp(v.getFuel(), 0, max) : 0;
                    int minRequired = (int) Math.ceil(max * 0.10); // 10%

                    meta.setDisplayName("§aFly");
                    List<String> lore = new ArrayList<>();
                    lore.add("§7Mount your vehicle and enable drive.");
                    lore.add(" ");
                    lore.add("§7Fuel: §f" + fuel + "§7/§f" + max);
                    lore.add("§7Required: §e≥ " + minRequired + "§7 (10%)");
                    icon.setItemMeta(meta);
                    return icon;
                })
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    Vehicle vehicle = VehicleManager.get(clicker);
                    if (vehicle == null) {
                        clicker.sendMessage("§cNo vehicle available.");
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
                        clicker.closeInventory();
                        return;
                    }

                    int max = getMaxFuel(vehicle);
                    int fuel = clamp(vehicle.getFuel(), 0, max);
                    int minRequired = (int) Math.ceil(max * 0.10); // 10%

                    if (fuel < minRequired) {
                        int shortfall = minRequired - fuel;
                        clicker.sendMessage("§cNot enough fuel to fly. You need §e" + shortfall + "§c more fuel (minimum §e" + minRequired + "§c).");
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
                        clicker.closeInventory();
                        return;
                    }

                    Entity entity = vehicle.getEntity();
                    if (entity == null || entity.isDead()) {
                        clicker.sendMessage("§cYour vehicle isn't spawned.");
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
                        clicker.closeInventory();
                        return;
                    }

                    entity.addPassenger(clicker);
                    vehicle.setInDrive(true);
                    clicker.playSound(clicker.getLocation(), vehicle.getSound(), SoundCategory.MASTER, 1f, 1.25f);
                    clicker.closeInventory();
                });
    }

    private InventoryButton createFuelButton() {
        return new InventoryButton()
                // Build display each time in case fuel changed
                .creator(player -> {
                    Vehicle v = VehicleManager.get(player);
                    if (v == null) {
                        ItemStack fail = new ItemStack(Material.BARRIER);
                        ItemMeta m = fail.getItemMeta();
                        m.setDisplayName("§cRefuel");
                        m.setLore(List.of("§7No vehicle found."));
                        fail.setItemMeta(m);
                        return fail;
                    }
                    return makeFuelItemFromVehicle(v);
                })
                .consumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    Vehicle vehicle = VehicleManager.get(player);
                    if (vehicle == null) {
                        player.sendMessage("§cNo vehicle available.");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
                        player.closeInventory();
                        return;
                    }

                    int max = getMaxFuel(vehicle);
                    int fuel = clamp(vehicle.getFuel(), 0, max);
                    int needed = Math.max(0, max - fuel);

                    int transferred = transferFuelFromPlayerToVehicle(player, vehicle, needed);
                    if (transferred <= 0) {
                        if (fuel >= max) {
                            player.sendMessage("§cYour vehicle is already full.");
                        } else {
                            player.sendMessage("§cNo fuel found in your canisters.");
                        }
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
                    } else {
                        vehicle.setFuel(Math.min(max, fuel + transferred));
                        player.sendMessage("§aTransferred §e" + transferred + "§a fuel into your vehicle.");
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.2f);
                    }

                    player.closeInventory();
                });
    }

    // ========= Display builders =========

    private ItemStack makeFuelItemFromVehicle(Vehicle v) {
        int max = getMaxFuel(v);
        int fuel = clamp(v.getFuel(), 0, max);
        int pct = (int) Math.round((fuel * 100.0) / Math.max(1, max));

        // Pick the correct CMD per range
        int cmd;
        if (pct <= 0) cmd = 6;
        else if (pct <= 33) cmd = 7;
        else if (pct <= 66) cmd = 8;
        else if (pct < 100) cmd = 9;
        else cmd = 10;

        ItemStack stack = new ItemStack(Material.SADDLE);
        ItemMeta meta = stack.getItemMeta();
        meta.setCustomModelData(cmd);
        meta.setDisplayName(colorForPercent(pct) + "Vehicle Fuel §7[" + fuel + "/" + max + "]");

        List<String> lore = new ArrayList<>();
        lore.add("§7" + fuelBar10(pct) + " §8(" + pct + "%)");
        lore.add(" ");
        lore.add("§eClick§7 to drain all Rhydonium canisters");
        lore.add("§7in your inventory into this vehicle.");
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    // ========= Transfer logic (from canisters to Vehicle) =========

    private int transferFuelFromPlayerToVehicle(Player player, Vehicle vehicle, int needed) {
        if (needed <= 0) return 0;

        int transferred = 0;
        PlayerInventory inv = player.getInventory();

        // All slots, including hotbar
        for (int slot = 0; slot < inv.getSize(); slot++) {
            if (needed <= 0) break;
            ItemStack it = inv.getItem(slot);
            int moved = drainCanisterInto(it, needed);
            if (moved > 0) {
                transferred += moved;
                needed -= moved;
                inv.setItem(slot, it);
            }
        }

        // Offhand
        if (needed > 0) {
            ItemStack off = inv.getItemInOffHand();
            int moved = drainCanisterInto(off, needed);
            if (moved > 0) {
                transferred += moved;
                needed -= moved;
                inv.setItemInOffHand(off);
            }
        }

        return transferred;
    }

    /** Drain up to "needed" from a single canister item; returns the amount drained. */
    private int drainCanisterInto(ItemStack item, int needed) {
        if (needed <= 0) return 0;
        if (item == null || item.getType().isAir()) return 0;
        if (!RhydoniumCanisterUtil.isValidCanister(item)) return 0;

        int fuel = RhydoniumCanisterUtil.getFuel(item);
        if (fuel <= 0) return 0;

        int take = Math.min(needed, fuel);
        RhydoniumCanisterUtil.setFuel(item, fuel - take); // updates PDC/CMD/name per your util
        return take;
    }

    // ========= UI helpers =========

    private String fuelBar10(int pct) {
        int filled = (int) Math.round(pct / 10.0); // 0..10 segments
        StringBuilder sb = new StringBuilder();
        sb.append("§7["); // left bracket gray

        String segColor = colorForPercent(pct);
        for (int i = 0; i < 10; i++) {
            if (i < filled) sb.append(segColor).append("|");
            else sb.append("§8").append("|"); // empty = dark gray
        }

        sb.append("§7]"); // right bracket gray
        return sb.toString();
    }

    /** Vehicle % color bands tuned for a big tank. */
    private String colorForPercent(int pct) {
        if (pct >= 95) return "§2"; // dark green
        if (pct >= 75) return "§a"; // green
        if (pct >= 50) return "§6"; // orange/gold
        if (pct >= 30) return "§e"; // yellow
        if (pct >= 10) return "§c"; // red
        if (pct > 0)  return "§4";  // dark red
        return "§4";                // empty
    }

    private int getMaxFuel(Vehicle v) {
        try {
            int max = v.getMaxFuel();
            if (max > 0) return max;
        } catch (Throwable ignored) {}
        return 1000; // default if not implemented
    }

    @SuppressWarnings("SameParameterValue")
    private int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
