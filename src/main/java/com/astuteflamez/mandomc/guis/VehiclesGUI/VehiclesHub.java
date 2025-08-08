package com.astuteflamez.mandomc.guis.VehiclesGUI;

import com.astuteflamez.mandomc.features.events.rhydonium.RhydoniumCanisterUtil;
import com.astuteflamez.mandomc.guis.GUIManager;
import com.astuteflamez.mandomc.guis.InventoryButton;
import com.astuteflamez.mandomc.guis.InventoryGUI;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
                3 * 9,
                LegacyComponentSerializer.legacyAmpersand().deserialize("&6&lVehicles")
        );
    }

    @Override
    public void decorate(Player player) {
        int xwingSlot = findXwingSlot(player);
        if (xwingSlot == -1) {
            // Optional: show a placeholder explaining they need to hold an X-Wing item
            super.decorate(player);
            return;
        }

        ItemStack xwing = player.getInventory().getItem(xwingSlot);
        this.addButton(13, createFuelButton(makeFuelItemFromLore(xwing), xwingSlot));

        super.decorate(player);
    }

    private InventoryButton createFuelButton(ItemStack displayItem, int xwingSlot) {
        return new InventoryButton()
                // Rebuild display each time in case fuel changed
                .creator(player -> makeFuelItemFromLore(player.getInventory().getItem(xwingSlot)))
                .consumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    PlayerInventory inv = player.getInventory();
                    ItemStack currentXwing = inv.getItem(xwingSlot);

                    if (currentXwing == null || currentXwing.getType().isAir() || !isXwingItem(currentXwing)) {
                        player.sendMessage("§cCould not find your X-Wing item.");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
                        player.closeInventory();
                        return;
                    }

                    int fuel = getXwingFuel(currentXwing);
                    int max = 1000;
                    int needed = Math.max(0, max - fuel);

                    int transferred = transferFuelFromPlayerToXwing(player, currentXwing, needed);
                    if (transferred <= 0) {
                        if (fuel >= max) {
                            player.sendMessage("§cYour vehicle is already full.");
                        } else {
                            player.sendMessage("§cNo fuel found in your canisters.");
                        }
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
                    } else {
                        setXwingFuel(currentXwing, Math.min(max, fuel + transferred));
                        inv.setItem(xwingSlot, currentXwing);
                        player.sendMessage("§aTransferred §e" + transferred + "§a fuel into your X-Wing.");
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.2f);
                    }

                    player.closeInventory();
                });
    }

    // ==== X-Wing item discovery ====

    private int findXwingSlot(Player player) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack it = inv.getItem(i);
            if (isXwingItem(it)) return i;
        }
        return -1;
    }

    private boolean isXwingItem(ItemStack item) {
        if (item == null || item.getType() != Material.ECHO_SHARD) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) return false;
        return item.getItemMeta().getCustomModelData() == 79;
    }

    // ==== Lore parsing/writing (Fuel: x/1000) ====

    private int getXwingFuel(ItemStack xwing) {
        try {
            if (xwing == null || !xwing.hasItemMeta()) return 0;
            ItemMeta meta = xwing.getItemMeta();
            if (!meta.hasLore()) return 0;
            for (String line : meta.getLore()) {
                String stripped = org.bukkit.ChatColor.stripColor(line).trim().toLowerCase();
                if (stripped.startsWith("fuel:")) {
                    String rest = stripped.substring("fuel:".length()).trim(); // e.g., "123/1000"
                    String left = rest.split("/")[0].trim();
                    return Integer.parseInt(left);
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private void setXwingFuel(ItemStack xwing, int newFuel) {
        if (xwing == null || !xwing.hasItemMeta()) return;
        ItemMeta meta = xwing.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        boolean replaced = false;
        for (int i = 0; i < lore.size(); i++) {
            String stripped = org.bukkit.ChatColor.stripColor(lore.get(i)).trim().toLowerCase();
            if (stripped.startsWith("fuel:")) {
                lore.set(i, "§eFuel: §f" + newFuel + "§7/§f1000");
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            lore.add("§eFuel: §f" + newFuel + "§7/§f1000");
        }

        meta.setLore(lore);
        xwing.setItemMeta(meta);
    }

    // ==== GUI item for fuel (built from lore) ====

    private ItemStack makeFuelItemFromLore(ItemStack xwing) {
        int fuel = Math.max(0, getXwingFuel(xwing));
        int max = 1000;
        int pct = (int) Math.round((fuel * 100.0) / max);

        ItemStack stack = new ItemStack(Material.BUCKET); // swap to custom icon if desired
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(colorForPercent(pct) + "Vehicle Fuel §7[" + fuel + "/" + max + "]");

        List<String> lore = new ArrayList<>();
        lore.add("§7" + fuelBar10(pct) + " §8(" + pct + "%)");
        lore.add(" ");
        lore.add("§eClick§7 to drain all Rhydonium canisters in your inventory into this vehicle.");
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    // ==== Transfer logic (from canisters to X-Wing item) ====

    private int transferFuelFromPlayerToXwing(Player player, ItemStack xwing, int needed) {
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

        // Apply to X-Wing
        if (transferred > 0) {
            int current = Math.max(0, getXwingFuel(xwing));
            int max = 1000;
            setXwingFuel(xwing, Math.min(max, current + transferred));
        }
        return transferred;
    }

    /**
     * Drain up to "needed" from a single canister item; returns the amount drained.
     */
    private int drainCanisterInto(ItemStack item, int needed) {
        if (needed <= 0) return 0;
        if (item == null || item.getType().isAir()) return 0;
        if (!RhydoniumCanisterUtil.isValidCanister(item)) return 0;

        int fuel = RhydoniumCanisterUtil.getFuel(item);
        if (fuel <= 0) return 0;

        int take = Math.min(needed, fuel);
        RhydoniumCanisterUtil.setFuel(item, fuel - take); // updates PDC + CMD + display name
        return take;
    }

    // ===== UI helpers =====

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

    /**
     * Vehicle % color bands tuned for a big tank.
     */
    private String colorForPercent(int pct) {
        if (pct >= 95) return "§2"; // dark green
        if (pct >= 75) return "§a"; // green
        if (pct >= 50) return "§6"; // gold / orange
        if (pct >= 30) return "§e"; // yellow
        if (pct >= 10) return "§c"; // red
        if (pct > 0)  return "§4";  // dark red
        return "§4";                // empty
    }

    @SuppressWarnings("SameParameterValue")
    private int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
