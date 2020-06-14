package com.guilhermemussi.statistics;

import com.guilhermemussi.statistics.service.StatisticsService;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import java.util.logging.Logger;

@Path("statistics")
public class StatisticsResource {
    public static final Logger LOGGER = Logger.getLogger(StatisticsResource.class.getName());

    @Inject
    @Channel("stats-change")
    Publisher<JsonObject> count;

    @GET
    @Path("stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.APPLICATION_JSON)
    public Publisher<JsonObject> stream(@Context Sse sse, @Context SseEventSink sink) {
        LOGGER.info("New subscription on SSE event Sink");
        sink.send(sse.newEvent(StatisticsService.getPlayerCount().toString()));
        return count;
    }
}
