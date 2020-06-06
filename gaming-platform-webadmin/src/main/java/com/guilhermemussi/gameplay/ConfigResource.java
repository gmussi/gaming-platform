package com.guilhermemussi.gameplay;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/config.js")
public class ConfigResource {
    @ConfigProperty(name = "gaming.platform.url.admin")
    String adminURL;

    @ConfigProperty(name = "gaming.platform.url.gameplay")
    String gameplayURL;

    @ConfigProperty(name = "gaming.platform.url.statistics")
    String statisticsURL;

    @GET
    @Produces("application/javascript; charset=utf-8")
    public String getConfig() {
        return String.format(
            "const SERVER = {};\n" +
            "SERVER.ADMIN = '%s';\n" +
            "SERVER.GAMEPLAY = '%s';\n" +
            "SERVER.STATISTICS = '%s';\n",
            adminURL, gameplayURL, statisticsURL);
    }
}
