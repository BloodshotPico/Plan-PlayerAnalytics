package main.java.com.djrapitops.plan.utilities.metrics;


import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.settings.Settings;

import java.util.HashMap;
import java.util.Map;

public class BStats {

    private final Plan plugin;
    private Metrics metrics;

    public BStats(Plan plugin) {
        this.plugin = plugin;
    }

    public void registerMetrics() {
        Log.logDebug("Enable", "Enabling bStats Metrics.");
        if (metrics == null) {
            metrics = new Metrics(plugin);
        }
        registerConfigSettingGraphs();
    }

    private void registerConfigSettingGraphs() {
        // TODO Write a Module bar graph

        String serverType = plugin.getServer().getName();
        if ("CraftBukkit".equals(serverType) && Check.isSpigotAvailable()) {
            serverType = "Spigot";
        } 
        String databaseType = plugin.getDB().getName();
        String analysisRefreshPeriod = Integer.toString(Settings.ANALYSIS_AUTO_REFRESH.getNumber());
        String themeBase = Settings.THEME_BASE.toString();

        addStringSettingPie("server_type", serverType);
        addStringSettingPie("database_type", databaseType);
        addStringSettingPie("analysis_periodic_refresh", analysisRefreshPeriod);
        addStringSettingPie("theme_base", themeBase);
        
        addFeatureBarChart("features");
    }

    private void addStringSettingPie(String id, String setting) {
        metrics.addCustomChart(new Metrics.SimplePie(id, () -> setting));
    }

    private void addFeatureBarChart(String id) {
        metrics.addCustomChart(new Metrics.AdvancedBarChart(id, () -> {
            Map<String, int[]> map = new HashMap<>();

            map.put("HTTPS", isEnabled("HTTPS".equals(plugin.getWebServer().getProtocol().toUpperCase())));
            map.put("HTML Export", isEnabled(Settings.ANALYSIS_EXPORT.isTrue()));
            boolean isConnectedToBungee = plugin.getInfoManager().isUsingAnotherWebServer();
            map.put("BungeeCord Connected", isEnabled(isConnectedToBungee));
            if (isConnectedToBungee) {
                map.put("Copy Bungee Config Values", isEnabled(Settings.BUNGEE_COPY_CONFIG.isTrue()));
                map.put("Standalone Override", isEnabled(Settings.BUNGEE_OVERRIDE_STANDALONE_MODE.isTrue()));
            }            
            map.put("Log Unknown Commands", isEnabled(Settings.LOG_UNKNOWN_COMMANDS.isTrue()));
            map.put("Combine Command Aliases", isEnabled(Settings.COMBINE_COMMAND_ALIASES.isTrue()));
            return map;
        }));
    }

    private int[] isEnabled(boolean t) {
        return t ? new int[]{1, 0} : new int[]{0, 1};
    }
}
