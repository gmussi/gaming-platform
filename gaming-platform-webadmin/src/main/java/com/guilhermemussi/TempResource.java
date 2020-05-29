package com.guilhermemussi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("temp")
public class TempResource {
    @GET
    public String temp() {
        return "temp";
    }
}
