package com.astuteflamez.mandomc.features.sabers;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

public class SaberManager {

    private final Plugin plugin;

    public SaberManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Adds saber-specific attributes like damage.
     */
    public ItemStack applySaberAttributes(ItemStack item, String saberId, ConfigurationSection sec) {
        double damage = sec.getDouble("damage", 5);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey modKey = new NamespacedKey(plugin, saberId.toLowerCase() + "_damage");
            AttributeModifier modifier = new AttributeModifier(
                    modKey,
                    damage,
                    Operation.ADD_NUMBER,
                    EquipmentSlotGroup.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Generates default saber lore if none is provided.
     */
    public List<String> generateSaberLore(double damage) {
        List<String> lore = new ArrayList<>();
        lore.add(color("&7\"An elegant weapon from a more civilized age.\""));
        lore.add("");
        lore.add(color("&6Ability: Saber Block ➣ &fĘ"));
        lore.add("");
        lore.add(color("&6Ability: Saber Throw ➣ &fė &e&l+ &fę"));
        lore.add("");
        lore.add(color("&7Melee Damage: &c" + damage));
        lore.add(color("&7Saber Throw Damage: &c24"));
        lore.add(color("&7Saber Throw Cooldown: &c10 seconds"));
        return lore;
    }

    private String color(String text) {
        return translateAlternateColorCodes('&', text);
    }
}
