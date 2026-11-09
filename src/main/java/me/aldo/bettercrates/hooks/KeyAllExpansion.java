package me.aldo.bettercrates.hooks;

import me.aldo.bettercrates.BetterCrates;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class KeyAllExpansion extends PlaceholderExpansion {
    private final BetterCrates plugin;
    public KeyAllExpansion(BetterCrates plugin) { this.plugin = plugin; }
    @Override public String getIdentifier() { return "bckey"; }
    @Override public String getAuthor() { return "Aldo"; }
    @Override public String getVersion() { return plugin.getDescription().getVersion(); }
    @Override public boolean persist() { return true; }
    @Override public String onRequest(OfflinePlayer player, String params) {
        if (params == null || params.isEmpty()) return plugin.getKeyAllManager().getFormattedRemaining();
        return null;
    }
}
