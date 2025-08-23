package com.astuteflamez.mandomc.guis.WarpsGUI;

import com.astuteflamez.mandomc.guis.GUIManager;
import com.astuteflamez.mandomc.guis.InventoryButton;
import com.astuteflamez.mandomc.features.warps.WarpConfig;
import com.astuteflamez.mandomc.guis.InventoryGUI;
import com.astuteflamez.mandomc.listeners.triggers.quests.QuestTrigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WarpsHub extends InventoryGUI {

    private final GUIManager guiManager;

    public WarpsHub(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, 6 * 9,
                LegacyComponentSerializer.legacyAmpersand().deserialize("&4&lMandoMC Warps"));
    }

    @Override
    public void decorate(Player player) {
        ConfigurationSection warps = WarpConfig.get().getConfigurationSection("warps");
        if (warps == null) return;

        for (String warpName : warps.getKeys(false)) {
            String path = "warps." + warpName;

            int slot = WarpConfig.get().getInt(path + ".slot", -1);
            if (slot == -1) continue;

            String displayName = WarpConfig.get().getString(path + ".name", "&7Unknown Warp");
            List<String> loreLines = WarpConfig.get().getStringList(path + ".description");

            // ✅ Block item for GUI
            Material material = Material.matchMaterial(WarpConfig.get().getString(path + ".material", "ENDER_PEARL"));
            if (material == null) material = Material.ENDER_PEARL;

            // ✅ Create item
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(displayName));
                int customModelData = WarpConfig.get().getInt(path + ".custommodeldata", 1);

                List<Component> loreComponents = new ArrayList<>();
                for (String line : loreLines) {
                    loreComponents.add(LegacyComponentSerializer.legacyAmpersand()
                            .deserialize(line)
                            .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
                }
                meta.lore(loreComponents);

                meta.setCustomModelData(customModelData);

                item.setItemMeta(meta);
            }

            // ✅ Add clickable button for this warp
            this.addButton(slot, createWarpButton(item, warpName));
        }

        super.decorate(player);
    }

    private InventoryButton createWarpButton(ItemStack itemStack, String warpName) {
        return new InventoryButton()
                .creator(player -> itemStack)
                .consumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    String path = "warps." + warpName;

                    // ✅ Get teleport data
                    double x = WarpConfig.get().getDouble(path + ".x");
                    double y = WarpConfig.get().getDouble(path + ".y");
                    double z = WarpConfig.get().getDouble(path + ".z");
                    float yaw = (float) WarpConfig.get().getDouble(path + ".yaw");
                    float pitch = (float) WarpConfig.get().getDouble(path + ".pitch");

                    // ✅ NEW: get world name from config
                    String worldName = WarpConfig.get().getString(path + ".world", player.getWorld().getName());
                    World world = Bukkit.getWorld(worldName);

                    if (world == null) {
                        player.sendMessage("§cWorld '" + worldName + "' is not loaded!");
                        player.closeInventory();
                        return;
                    }

                    if (worldName.equalsIgnoreCase("world")) {
                        QuestTrigger.checkQuests(player, "phone_home", "quests-commands");
                    }

                    Location loc = new Location(world, x, y, z, yaw, pitch);
                    player.teleport(loc);
                    player.sendMessage("§6§lMandoMC §8» §aWarped to " + warpName + "!");
                    player.playSound(loc, Sound.BLOCK_PORTAL_TRAVEL, 0.7f, 1.3f);

                    player.closeInventory();
                });
    }
}
