package com.anngtv.kho.commands;

import com.anngtv.kho.MineralWarehouse;
import com.anngtv.kho.gui.WarehouseGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KhoCommand implements CommandExecutor, TabCompleter {

    private final MineralWarehouse plugin;

    public KhoCommand(MineralWarehouse plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Chỉ người chơi mới có thể dùng lệnh này!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            new WarehouseGUI(plugin).open(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "deposit":
            case "cat":
                handleDeposit(player);
                break;
            case "depositall":
            case "cathet":
                handleDepositAll(player);
                break;
            case "sell":
            case "ban":
                handleSell(player, args);
                break;
            case "sellall":
            case "banhet":
                handleSellAll(player);
                break;
            case "reload":
                handleReload(player);
                break;
            case "add":
            case "set":
            case "take":
                handleAdminModify(sender, args);
                break;
            case "see":
                handleSee(player, args);
                break;
            case "autocat":
                handleAutoDeposit(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Sử dụng: /kho [deposit|cathet|autocat|ban|banhet|see|reload]");
                if (sender.hasPermission("mineralwarehouse.admin")) {
                    sender.sendMessage(ChatColor.RED + "Admin: /kho [add|set|take] <player> <mineral> <amount>");
                }
                break;
        }

        return true;
    }

    private void handleAutoDeposit(Player player) {
        boolean current = plugin.getDatabaseManager().isAutoDeposit(player.getUniqueId());
        plugin.getDatabaseManager().setAutoDeposit(player.getUniqueId(), !current);
        String msg = !current ? plugin.getConfig().getString("messages.auto-deposit-on") : 
                plugin.getConfig().getString("messages.auto-deposit-off");
        player.sendMessage(format(plugin.getConfig().getString("messages.prefix") + msg));
    }

    private void handleSell(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Sử dụng: /kho ban <tên_khoáng_sản> [số_lượng]");
            return;
        }

        String type = args[1].toUpperCase();
        if (!plugin.getConfig().getConfigurationSection("minerals").contains(type)) {
            player.sendMessage(ChatColor.RED + "Khoáng sản không hợp lệ!");
            return;
        }

        long amount = 0;
        long current = plugin.getDatabaseManager().getAmount(player.getUniqueId(), type);

        if (args.length >= 3) {
            try {
                amount = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Số lượng không hợp lệ!");
                return;
            }
        } else {
            amount = current; // Sell all of this type if amount not specified
        }

        if (amount <= 0) return;

        if (current < amount) {
            player.sendMessage(format(plugin.getConfig().getString("messages.prefix") + 
                    plugin.getConfig().getString("messages.not-enough-in-kho")
                    .replace("{mineral}", plugin.getConfig().getString("minerals." + type + ".name"))));
            return;
        }

        plugin.getWarehouseManager().sell(player, type, amount);
    }

    private void handleSellAll(Player player) {
        plugin.getWarehouseManager().sellAll(player);
    }

    private void handleDeposit(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(format(plugin.getConfig().getString("messages.prefix") + "&cCầm khoáng sản trên tay để cất!"));
            return;
        }

        String type = item.getType().name();
        if (!plugin.getConfig().getConfigurationSection("minerals").contains(type)) {
            player.sendMessage(format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.not-mineral")));
            return;
        }

        int amount = item.getAmount();
        String mineralName = plugin.getConfig().getString("minerals." + type + ".name");
        
        plugin.getWarehouseManager().deposit(player, item);
        
        player.sendMessage(format(plugin.getConfig().getString("messages.prefix") + 
                plugin.getConfig().getString("messages.deposit-success")
                .replace("{amount}", String.valueOf(amount))
                .replace("{mineral}", mineralName)));
    }

    private void handleDepositAll(Player player) {
        plugin.getWarehouseManager().depositAll(player);
        player.sendMessage(format(plugin.getConfig().getString("messages.prefix") + 
                plugin.getConfig().getString("messages.deposit-all-success")));
    }

    private void handleSee(Player player, String[] args) {
        if (args.length > 1) {
            if (!player.hasPermission("mineralwarehouse.admin")) {
                player.sendMessage(format(plugin.getConfig().getString("messages.no-permission")));
                return;
            }
            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(format(plugin.getConfig().getString("messages.prefix") + 
                        plugin.getConfig().getString("messages.not-online")));
                return;
            }
            new WarehouseGUI(plugin).open(target, player); // Open target's warehouse for viewer
        } else {
            new WarehouseGUI(plugin).open(player);
        }
    }

    private void handleAdminModify(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mineralwarehouse.admin")) {
            sender.sendMessage(format(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Sử dụng: /kho " + args[0] + " <player> <mineral> <amount>");
            return;
        }

        org.bukkit.OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[1]);
        String type = args[2].toUpperCase();
        if (!plugin.getConfig().getConfigurationSection("minerals").contains(type)) {
            sender.sendMessage(ChatColor.RED + "Khoáng sản không hợp lệ!");
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Số lượng không hợp lệ!");
            return;
        }

        long current = plugin.getDatabaseManager().getAmount(target.getUniqueId(), type);
        long newValue = current;

        switch (args[0].toLowerCase()) {
            case "add":
                newValue += amount;
                break;
            case "set":
                newValue = amount;
                break;
            case "take":
                newValue = Math.max(0, current - amount);
                break;
        }

        plugin.getDatabaseManager().setAmount(target.getUniqueId(), type, newValue);
        sender.sendMessage(format(plugin.getConfig().getString("messages.prefix") + 
                plugin.getConfig().getString("messages.admin-modify-success")
                .replace("{mineral}", plugin.getConfig().getString("minerals." + type + ".name"))
                .replace("{player}", target.getName())
                .replace("{amount}", String.valueOf(newValue))));
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("mineralwarehouse.admin")) {
            player.sendMessage(format(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        plugin.reloadConfig();
        player.sendMessage(format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.reload")));
    }

    private String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> sub = new ArrayList<>(Arrays.asList("cat", "cathet", "autocat", "ban", "banhet", "see", "reload"));
            if (sender.hasPermission("mineralwarehouse.admin")) {
                sub.addAll(Arrays.asList("add", "set", "take"));
            }
            return sub.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("see") || args[0].equalsIgnoreCase("add") || 
                args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("take")) {
                return null; // Return null to show online players
            }
            if (args[0].equalsIgnoreCase("ban")) {
                return plugin.getConfig().getConfigurationSection("minerals").getKeys(false).stream()
                        .filter(s -> s.startsWith(args[1].toUpperCase())).collect(Collectors.toList());
            }
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("add") || 
            args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("take"))) {
            return plugin.getConfig().getConfigurationSection("minerals").getKeys(false).stream()
                    .filter(s -> s.startsWith(args[2].toUpperCase())).collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}
