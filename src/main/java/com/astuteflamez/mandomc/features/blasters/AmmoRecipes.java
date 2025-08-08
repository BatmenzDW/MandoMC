package com.astuteflamez.mandomc.features.blasters;

import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.features.items.ItemsManager;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

public class AmmoRecipes {

    private final MandoMC plugin;
    private final ItemsManager itemsManager;

    public AmmoRecipes(MandoMC plugin, ItemsManager itemsManager) {
        this.plugin = plugin;
        this.itemsManager = itemsManager;
    }

    public void registerAmmoRecipes() {
        registerStandardEnergyCell();
        registerHyperEnergyCell();
        registerDenseEnergyCell();
    }

    private void registerStandardEnergyCell() {
        ItemStack ammo = WeaponMechanicsAPI.generateAmmo("Standard_Energy_Cell", false);
        if (ammo == null) return;

        ammo.setAmount(64);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "standard_energy_cell"), ammo);
        recipe.shape("PEP", "EBE", "PEP");
        recipe.setIngredient('B', new RecipeChoice.ExactChoice(itemsManager.getItem("batteries")));
        recipe.setIngredient('P', new RecipeChoice.ExactChoice(itemsManager.getItem("plastoid")));
        recipe.setIngredient('E', new RecipeChoice.ExactChoice(itemsManager.getItem("energy_conduit")));

        Bukkit.addRecipe(recipe);
    }

    private void registerHyperEnergyCell() {
        ItemStack ammo = WeaponMechanicsAPI.generateAmmo("Hyper_Energy_Cell", false);
        if (ammo == null) return;

        ammo.setAmount(64);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "hyper_energy_cell"), ammo);
        recipe.shape("PEP", "EBE", "PEP");
        recipe.setIngredient('B', new RecipeChoice.ExactChoice(itemsManager.getItem("glitterstim_fibers")));
        recipe.setIngredient('P', new RecipeChoice.ExactChoice(itemsManager.getItem("plastoid")));
        recipe.setIngredient('E', new RecipeChoice.ExactChoice(itemsManager.getItem("energy_conduit")));

        Bukkit.addRecipe(recipe);
    }

    private void registerDenseEnergyCell() {
        ItemStack ammo = WeaponMechanicsAPI.generateAmmo("Dense_Energy_Cell", false);
        if (ammo == null) return;

        ammo.setAmount(64);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "dense_energy_cell"), ammo);
        recipe.shape("PEP", "EBE", "PEP");
        recipe.setIngredient('B', new RecipeChoice.ExactChoice(itemsManager.getItem("isotope_5")));
        recipe.setIngredient('P', new RecipeChoice.ExactChoice(itemsManager.getItem("plastoid")));
        recipe.setIngredient('E', new RecipeChoice.ExactChoice(itemsManager.getItem("energy_conduit")));

        Bukkit.addRecipe(recipe);
    }
}
