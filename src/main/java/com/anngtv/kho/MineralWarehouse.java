package com.anngtv.kho;

import com.anngtv.kho.commands.KhoCommand;
import com.anngtv.kho.database.DatabaseManager;
import com.anngtv.kho.listeners.InventoryListener;
import com.anngtv.kho.manager.WarehouseManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MineralWarehouse extends JavaPlugin {

    private static MineralWarehouse instance;
    private WarehouseManager warehouseManager;
    private DatabaseManager databaseManager;
    private Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        databaseManager = new DatabaseManager(this);
        databaseManager.init();

        warehouseManager = new WarehouseManager(this);

        getCommand("kho").setExecutor(new KhoCommand(this));
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new com.anngtv.kho.listeners.BlockBreakListener(this), this);

        getLogger().info("MineralWarehouse has been enabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("MineralWarehouse has been disabled!");
    }

    public static MineralWarehouse getInstance() {
        return instance;
    }

    public WarehouseManager getWarehouseManager() {
        return warehouseManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public Economy getEconomy() {
        return econ;
    }
}
