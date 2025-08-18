package com.astuteflamez.mandomc.database.data;

import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import io.lumine.mythic.bukkit.utils.shadows.nbt.NBTTagCompound;
import io.lumine.mythic.core.utils.jnbt.CompoundTag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.UUID;

public class RewardItem extends QuestReward{

    private final int poolId;
    private final int id;
    private final byte[] itemStack;

    public RewardItem(int poolId, int id, ItemStack item) {
        this.poolId = poolId;
        this.id = id;

        this.itemStack = convertToByteArray(item);
    }

    public RewardItem(int poolId, ItemStack item) {this(poolId, -1, item);}

    public RewardItem(int poolId, int id, byte[] itemBytes) {
        this.poolId = poolId;
        this.id = id;
        this.itemStack = itemBytes;
    }

    public int getPoolId() {
        return poolId;
    }

    public int getId() {
        return id;
    }

    public byte[] getItemStackBytes(){
        return itemStack;
    }

    public static byte[] convertToByteArray(ItemStack item) {
        if (item == null) return null;


        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try
        {
            BukkitObjectOutputStream objectOutput = new BukkitObjectOutputStream(output);
            objectOutput.writeObject(item);
            objectOutput.close();

            return output.toByteArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public ItemStack getItemStack() {
        if (itemStack == null) return null;

        ByteArrayInputStream input = new ByteArrayInputStream(itemStack);

        try {
            BukkitObjectInputStream objectInput = new BukkitObjectInputStream(input);
            return (ItemStack) objectInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toString(){
        return "id: " + id + "; item:" + getItemStack();
    }

    @Override
    public void givePlayer(UUID uuid) {
        PlayerInventory inventory = Bukkit.getPlayer(uuid).getInventory();
        inventory.addItem(getItemStack());
    }
}
