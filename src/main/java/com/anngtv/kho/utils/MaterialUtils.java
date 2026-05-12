package com.anngtv.kho.utils;

import org.bukkit.Material;

public class MaterialUtils {
    public static Material getMaterial(String name) {
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException e) {
            // Fallback for older versions if needed
            if (name.equals("LAPIS_LAZULI")) return Material.matchMaterial("INK_SACK");
            return null;
        }
    }
}
