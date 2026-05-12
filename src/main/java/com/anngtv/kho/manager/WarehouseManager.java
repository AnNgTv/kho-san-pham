package com.anngtv.kho.manager;

import com.anngtv.kho.MineralWarehouse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class WarehouseManager {

    private final MineralWarehouse plugin;

    public WarehouseManager(MineralWarehouse plugin) {
        this.plugin = plugin;
    }

    public void deposit(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        String type = item.getType().name();
        if (!plugin.getConfig().getConfigurationSection("minerals").contains(type)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        int amount = item.getAmount();
        long current = plugin.getDatabaseManager().getAmount(uuid, type);
        
        plugin.getDatabaseManager().setAmount(uuid, type, current + amount);
        item.setAmount(0);
    }

    public void depositAll(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            
            String type = item.getType().name();
            if (plugin.getConfig().getConfigurationSection("minerals").contains(type)) {
                deposit(player, item);
            }
        }
    }

    public void sell(Player player, String type, long amount) {
        UUID uuid = player.getUniqueId();
        long current = plugin.getDatabaseManager().getAmount(uuid, type);

        if (current < amount) return;

        double pricePerUnit = plugin.getConfig().getDouble("minerals." + type + ".price", 0);
        double totalSellPrice = pricePerUnit * amount;

        if (totalSellPrice > 0) {
            plugin.getEconomy().depositPlayer(player, totalSellPrice);
            plugin.getDatabaseManager().setAmount(uuid, type, current - amount);

            String mineralName = plugin.getConfig().getString("minerals." + type + ".name");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + 
                    plugin.getConfig().getString("messages.sell-success")
                    .replace("{amount}", String.valueOf(amount))
                    .replace("{mineral}", mineralName)
                    .replace("{price}", String.format("%.2f", totalSellPrice))));
        }
    }

    public void sellAll(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, Long> warehouseData = plugin.getDatabaseManager().getWarehouse(uuid);
        double totalGain = 0;

        for (Map.Entry<String, Long> entry : warehouseData.entrySet()) {
            String type = entry.getKey();
            long amount = entry.getValue();
            double pricePerUnit = plugin.getConfig().getDouble("minerals." + type + ".price", 0);
            
            if (pricePerUnit > 0 && amount > 0) {
                totalGain += pricePerUnit * amount;
                plugin.getDatabaseManager().setAmount(uuid, type, 0);
            }
        }

        if (totalGain > 0) {
            plugin.getEconomy().depositPlayer(player, totalGain);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + 
                    plugin.getConfig().getString("messages.sell-all-success")
                    .replace("{price}", String.format("%.2f", totalGain))));
        }
    }

    public void withdraw(Player player, String type, int amount) {
        withdrawToViewer(player, player, type, amount);
    }

    public void withdrawToViewer(Player owner, Player viewer, String type, int amount) {
        UUID ownerUuid = owner.getUniqueId();
        long current = plugin.getDatabaseManager().getAmount(ownerUuid, type);

        if (current < amount) {
            return;
        }

        Material material = Material.valueOf(type);
        ItemStack item = new ItemStack(material, amount);

        if (viewer.getInventory().firstEmpty() == -1) {
            viewer.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.inventory-full")));
            return;
        }

        viewer.getInventory().addItem(item);
        plugin.getDatabaseManager().setAmount(ownerUuid, type, current - amount);
    }
}
