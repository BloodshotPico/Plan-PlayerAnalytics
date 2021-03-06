/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.bungee;


import com.djrapitops.plugin.api.Check;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.systems.info.BungeeInformationManager;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;

import java.util.Map;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class PostNetworkPageContentWebAPI extends WebAPI {
    @Override
    public Response onRequest(IPlan plugin, Map<String, String> variables) {
        if (Check.isBukkitAvailable()) {
            return badRequest("Called a Bukkit server.");
        }

        UUID serverUUID = UUID.fromString(variables.get("sender"));
        String html = variables.get("html");
        if (html == null) {
            return badRequest("html not present");
        }

        ((BungeeInformationManager) plugin.getInfoManager()).cacheNetworkPageContent(serverUUID, html);
        return success();
    }

    @Override
    public void sendRequest(String address) throws WebAPIException {
        throw new IllegalStateException("Wrong method call for this WebAPI, call sendRequest(String, UUID, UUID) instead.");
    }

    public void sendNetworkContent(String address, String html) throws WebAPIException {
        addVariable("html", html);
        super.sendRequest(address);
    }
}