package com.anngtv.kho.listeners;

import com.anngtv.kho.MineralWarehouse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private final MineralWarehouse plugin;

    public InventoryListener(MineralWarehouse plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String baseTitle = ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("gui.title"));
        String title = event.getView().getTitle();
        
        // Check if the title starts with the fixed part of our GUI title
        String titlePrefix = baseTitle.split("\\{player\\}")[0];
        if (!title.startsWith(titlePrefix)) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        Player viewer = (Player) event.getWhoClicked();
        
        // Try to identify the owner from the title
        String ownerName = title.substring(titlePrefix.length());
        Player owner = plugin.getServer().getPlayer(ownerName);
        
        // If owner is not online, we might need a different approach for offline owners
        // but for now let's assume online for GUI interaction
        if (owner == null) return;

        // Permission check for viewing/interacting with others' warehouses
        if (!viewer.getUniqueId().equals(owner.getUniqueId()) && !viewer.hasPermission("mineralwarehouse.admin")) {
            viewer.closeInventory();
            viewer.sendMessage(ChatColor.RED + "Bạn không có quyền thao tác trên kho của người khác!");
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        Material material = clickedItem.getType();
        String type = material.name();

        if (material == Material.CHEST && event.getSlot() == event.getInventory().getSize() - 2) {
            plugin.getWarehouseManager().depositAll(viewer);
            new com.anngtv.kho.gui.WarehouseGUI(plugin).open(owner, viewer);
            return;
        }

        if (material == Material.valueOf("INK_SACK") && event.getSlot() == event.getInventory().getSize() - 3) {
            boolean current = plugin.getDatabaseManager().isAutoDeposit(owner.getUniqueId());
            plugin.getDatabaseManager().setAutoDeposit(owner.getUniqueId(), !current);
            new com.anngtv.kho.gui.WarehouseGUI(plugin).open(owner, viewer);
            return;
        }

        if (material == Material.GOLD_BLOCK && event.getSlot() == event.getInventory().getSize() - 4) {
            plugin.getWarehouseManager().sellAll(viewer);
            new com.anngtv.kho.gui.WarehouseGUI(plugin).open(owner, viewer);
            return;
        }

        if (!plugin.getConfig().getConfigurationSection("minerals").contains(type)) {
            return;
        }

        long currentAmount = plugin.getDatabaseManager().getAmount(owner.getUniqueId(), type);

        if (event.getClick() == ClickType.DROP) {
            plugin.getWarehouseManager().sell(viewer, type, currentAmount);
            new com.anngtv.kho.gui.WarehouseGUI(plugin).open(owner, viewer);
            return;
        }

        int withdrawAmount = 0;

        if (event.isShiftClick()) {
            withdrawAmount = (int) Math.min(currentAmount, 2304); // Withdraw max 36 stacks at once
        } else if (event.isLeftClick()) {
            withdrawAmount = (int) Math.min(currentAmount, 64);
        } else if (event.isRightClick()) {
            withdrawAmount = (int) Math.min(currentAmount, 1);
        }

        if (withdrawAmount > 0) {
            // Withdraw to viewer's inventory
            plugin.getWarehouseManager().withdrawToViewer(owner, viewer, type, withdrawAmount);
            // Refresh GUI for viewer
            new com.anngtv.kho.gui.WarehouseGUI(plugin).open(owner, viewer);
        }
    }
}
