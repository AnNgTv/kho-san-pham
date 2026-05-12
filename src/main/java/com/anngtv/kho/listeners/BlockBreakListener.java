package com.anngtv.kho.listeners;

import com.anngtv.kho.MineralWarehouse;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class BlockBreakListener implements Listener {

    private final MineralWarehouse plugin;

    public BlockBreakListener(MineralWarehouse plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getDatabaseManager().isAutoDeposit(player.getUniqueId())) {
            return;
        }

        Block block = event.getBlock();
        
        // This is a simple implementation. In a real plugin, you might want to 
        // handle Fortune, Silk Touch, etc. by checking the drops.
        
        Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());
        
        boolean depositedAny = false;
        for (ItemStack drop : drops) {
            String type = drop.getType().name();
            if (plugin.getConfig().getConfigurationSection("minerals").contains(type)) {
                plugin.getWarehouseManager().deposit(player, drop);
                depositedAny = true;
            }
        }
        
        if (depositedAny) {
            // If we deposited everything, we should probably prevent the drops from spawning
            // but block.getDrops doesn't remove them from the world.
            // We can clear the drops if we use a different approach.
            
            // A better way is to cancel the drops if we handled them.
            event.setDropItems(false);
            // Re-spawn items that were NOT minerals if any
            for (ItemStack drop : drops) {
                if (drop.getAmount() > 0) {
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                }
            }
        }
    }
}
