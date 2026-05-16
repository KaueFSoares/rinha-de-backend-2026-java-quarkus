package br.kauesoares.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/ready")
public class ReadyResource {

    @Inject
    FraudEngine engine;

    @GET
    public String ready() {
        engine.vectorSearch.top5(new float[14], new byte[5]);
        return "ok";
    }

}
