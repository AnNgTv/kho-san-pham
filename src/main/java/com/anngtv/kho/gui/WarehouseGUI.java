package com.anngtv.kho.gui;

import com.anngtv.kho.MineralWarehouse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WarehouseGUI {

    private final MineralWarehouse plugin;

    public WarehouseGUI(MineralWarehouse plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        open(player, player);
    }

    public void open(Player owner, Player viewer) {
        String title = format(plugin.getConfig().getString("gui.title")
                .replace("{player}", owner.getName()));
        int size = plugin.getConfig().getInt("gui.size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        Map<String, Long> warehouseData = plugin.getDatabaseManager().getWarehouse(owner.getUniqueId());
        
        int slot = 0;
        for (String type : plugin.getConfig().getConfigurationSection("minerals").getKeys(false)) {
            if (slot >= size - 1) break;

            Material material = Material.valueOf(type);
            String displayName = format(plugin.getConfig().getString("minerals." + type + ".name"));
            long amount = warehouseData.getOrDefault(type, 0L);
            double price = plugin.getConfig().getDouble("minerals." + type + ".price");

            inv.setItem(slot, createMineralItem(material, displayName, amount, price));
            slot++;
        }

        // Info item at the last slot
        inv.setItem(size - 1, createInfoItem());
        
        // Deposit All item
        inv.setItem(size - 2, createDepositAllItem());
        
        // Auto Deposit item
        inv.setItem(size - 3, createAutoDepositItem(owner));

        // Sell All item
        inv.setItem(size - 4, createSellAllItem());

        viewer.openInventory(inv);
    }

    private ItemStack createSellAllItem() {
        ItemStack item = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "§lBán toàn bộ khoáng sản");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click để bán toàn bộ khoáng sản");
        lore.add(ChatColor.GRAY + "đang có trong kho lấy tiền.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createAutoDepositItem(Player owner) {
        boolean enabled = plugin.getDatabaseManager().isAutoDeposit(owner.getUniqueId());
        ItemStack item = new ItemStack(Material.valueOf("INK_SACK"), 1, (short) (enabled ? 10 : 8));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "§lTự động cất: " + (enabled ? ChatColor.GREEN + "§lBẬT" : ChatColor.RED + "§lTẮT"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click để thay đổi trạng thái tự động");
        lore.add(ChatColor.GRAY + "cất khoáng sản khi đào được.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createDepositAllItem() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "§lCất toàn bộ khoáng sản");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click để cất toàn bộ khoáng sản");
        lore.add(ChatColor.GRAY + "trong túi đồ vào kho.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createMineralItem(Material material, String name, long amount, double price) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name);
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.WHITE + "Số lượng: " + ChatColor.GREEN + amount);
        lore.add(ChatColor.WHITE + "Giá bán: " + ChatColor.GOLD + price + "$/cái");
        lore.add("");
        lore.add(ChatColor.GRAY + "Click chuột trái để lấy 64");
        lore.add(ChatColor.GRAY + "Click chuột phải để lấy 1");
        lore.add(ChatColor.GRAY + "Shift + Click để lấy hết");
        lore.add(ChatColor.AQUA + "Nhấn Q để bán toàn bộ loại này");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfoItem() {
        Material material = Material.valueOf(plugin.getConfig().getString("gui.info-item.material", "BOOK"));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(format(plugin.getConfig().getString("gui.info-item.name")));
        List<String> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("gui.info-item.lore")) {
            lore.add(format(line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
