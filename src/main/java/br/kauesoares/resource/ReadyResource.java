package br.kauesoares.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/ready")
public class ReadyResource {

    @GET
    public String ready() {
        return "ok";
    }

}
