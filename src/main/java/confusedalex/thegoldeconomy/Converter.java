package confusedalex.thegoldeconomy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.ResourceBundle;

public class Converter {

    EconomyImplementer eco;
    ResourceBundle bundle;

    public Converter(EconomyImplementer economyImplementer, ResourceBundle bundle) {
        this.eco = economyImplementer;
        this.bundle = bundle;
    }

    public int getValue(Material material) {
        if (material.equals(Material.getMaterial("THE_VAULT_VAULT_BRONZE"))) return 1;
        if (material.equals(Material.getMaterial("THE_VAULT_VAULT_SILVER"))) return 9;
        if (material.equals(Material.getMaterial("THE_VAULT_VAULT_GOLD"))) return 81;
        if (material.equals(Material.getMaterial("THE_VAULT_VAULT_PLATINUM"))) return 729;

        return 0;
    }

    public boolean isNotGold(Material material) {
        if (material.equals(Material.getMaterial("THE_VAULT_VAULT_BRONZE"))) return false;
        if (material.equals(Material.getMaterial("THE_VAULT_VAULT_SILVER"))) return false;
        if (material.equals(Material.getMaterial("THE_VAULT_VAULT_GOLD"))) return false;
        if (material.equals(Material.getMaterial("THE_VAULT_VAULT_PLATINUM"))) return false;

        else return true;
    }

    public int getInventoryValue(Player player){
        int value = 0;

        // calculating the value of all the gold in the inventory to nuggets
        for (ItemStack item : player.getInventory()) {
            if (item == null) continue;
            Material material = item.getType();

            if (isNotGold(material)) continue;

            value += (getValue(material) * item.getAmount());

        }
        return value;
    }

    public void remove(Player player, int amount){
        int value = 0;

        // calculating the value of all the gold in the inventory to nuggets
        for (ItemStack item : player.getInventory()) {
            if (item == null) continue;
            Material material = item.getType();

            if (isNotGold(material)) continue;

            value += (getValue(material) * item.getAmount());
        }

        // Checks if the Value of the items is greater than the amount to deposit
        if (value < amount) return;

        // Deletes all gold items
        for (ItemStack item : player.getInventory()) {
            if (item == null) continue;
            if (isNotGold(item.getType())) continue;

            item.setAmount(0);
            item.setType(Material.AIR);
        }

        int newBalance = value - amount;
        give(player, newBalance);
    }

    public void give(Player player, int value){
        boolean warning = false;

        HashMap<Integer, ItemStack> plat = player.getInventory().addItem(new ItemStack(Material.getMaterial("THE_VAULT_VAULT_PLATINUM"), value/729));
        for (ItemStack item : plat.values()) {
            if (item != null && item.getType() == Material.getMaterial("THE_VAULT_VAULT_PLATINUM") && item.getAmount() > 0) {
                player.getWorld().dropItem(player.getLocation(), item);
                warning = true;
            }
        }

        value -= (value/729)*729;

        HashMap<Integer, ItemStack> gold = player.getInventory().addItem(new ItemStack(Material.getMaterial("THE_VAULT_VAULT_GOLD"), value/81));
        for (ItemStack item : gold.values()) {
            if (item != null && item.getType() == Material.getMaterial("THE_VAULT_VAULT_GOLD") && item.getAmount() > 0) {
                player.getWorld().dropItem(player.getLocation(), item);
                warning = true;
            }
        }

        value -= (value/81)*81;

        HashMap<Integer, ItemStack> silver = player.getInventory().addItem(new ItemStack(Material.getMaterial("THE_VAULT_VAULT_SILVER"), value/9));
        for (ItemStack item : silver.values()) {
            if (item != null && item.getType() == Material.getMaterial("THE_VAULT_VAULT_SILVER") && item.getAmount() > 0) {
                player.getWorld().dropItem(player.getLocation(), item);
                warning = true;
            }
        }

        value -= (value/9)*9;

        HashMap<Integer, ItemStack> bronze = player.getInventory().addItem(new ItemStack(Material.getMaterial("THE_VAULT_VAULT_BRONZE"), value));
        for (ItemStack item : bronze.values()) {
            if (item != null && item.getType() == Material.getMaterial("THE_VAULT_VAULT_BRONZE") && item.getAmount() > 0) {
                player.getWorld().dropItem(player.getLocation(), item);
                warning = true;
            }
        }

        if (warning) Util.sendMessageToPlayer(String.format(bundle.getString("warning.drops")), player);
    }


    public void withdrawAll(Player player){
        String uuid = player.getUniqueId().toString();

        // searches in the Hashmap for the balance, so that a player can't withdraw gold from his Inventory
        int value = eco.bank.getAccountBalance(player.getUniqueId().toString());
        eco.bank.setBalance(uuid, (0));

        give(player, value);
    }

    public void withdraw(Player player, int nuggets){
        String uuid = player.getUniqueId().toString();
        int oldbalance = eco.bank.getAccountBalance(player.getUniqueId().toString());

        // Checks balance in HashMap
        if (nuggets > eco.bank.getPlayerBank().get(player.getUniqueId().toString())) {
            Util.sendMessageToPlayer(bundle.getString("error.notenoughmoneywithdraw"), player);
            return;
        }
        eco.bank.setBalance(uuid, (oldbalance - nuggets));

        give(player, nuggets);

    }

    public void depositAll(Player player){
        OfflinePlayer op = Bukkit.getOfflinePlayer(player.getUniqueId());
        int value = 0;

        for (ItemStack item : player.getInventory()) {
            if (item == null) continue;
            Material material = item.getType();

            if (isNotGold(material)) continue;

            value = value + (getValue(material) * item.getAmount());
            item.setAmount(0);
            item.setType(Material.AIR);
        }

        eco.depositPlayer(op, value);

    }

    public void deposit(Player player, int nuggets){
        OfflinePlayer op = Bukkit.getOfflinePlayer(player.getUniqueId());

        remove(player, nuggets);
        eco.depositPlayer(op, nuggets);
    }
}
