/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.settings.locale.Locale;
import main.java.com.djrapitops.plan.settings.locale.Msg;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit.InspectWebAPI;
import main.java.com.djrapitops.plan.utilities.Condition;

import java.util.Optional;
import java.util.UUID;

/**
 * Command used for testing functions that are too difficult to unit test.
 *
 * @author Rsl1122
 */
public class DevCommand extends SubCommand {

    private final Plan plugin;

    public DevCommand(Plan plugin) {
        super("dev", CommandType.PLAYER_OR_ARGS, "plan.*", "Test Plugin functions not testable with unit tests.", "<feature to test>");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String cmd, String[] args) {
        if (!Condition.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString(), sender)) {
            return true;
        }
        String feature = args[0];
        switch (feature) {
            case "webapi":
                if (!Condition.isTrue(args.length >= 2, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString(), sender)) {
                    break;
                }
                if (!webapi(args[1] + "webapi", args.length >= 3)) {
                    sender.sendMessage("[Plan] No such API / Exception occurred.");
                }
                break;
            case "web":
                Optional<String> bungeeConnectionAddress = plugin.getServerInfoManager().getBungeeConnectionAddress();
                String accessAddress = plugin.getWebServer().getAccessAddress();
                sender.sendMessage((plugin.getInfoManager().isUsingAnotherWebServer() && bungeeConnectionAddress.isPresent())
                        ? "Bungee: " + bungeeConnectionAddress.get() : "Local: " + accessAddress);
                break;
            default:
                break;
        }
        return true;
    }

    private boolean webapi(String method, boolean connectToBungee) {
        WebAPI api = plugin.getWebServer().getWebAPI().getAPI(method);
        if (api == null) {
            return false;
        }
        try {
            String address = plugin.getWebServer().getAccessAddress();
            if (connectToBungee) {
                Optional<String> bungeeConnectionAddress = plugin.getServerInfoManager().getBungeeConnectionAddress();
                if (bungeeConnectionAddress.isPresent()) {
                    address = bungeeConnectionAddress.get();
                }
            }
            if (api instanceof InspectWebAPI) {
                ((InspectWebAPI) api).sendRequest(address, UUID.randomUUID());
            } else {
                api.sendRequest(address);
            }
            return true;
        } catch (WebAPIException e) {
            e.printStackTrace();
        }
        return false;
    }
}