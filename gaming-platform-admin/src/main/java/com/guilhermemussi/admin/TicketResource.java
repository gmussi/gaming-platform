package com.guilhermemussi.admin;

import com.guilhermemussi.admin.config.TokenUtils;
import com.guilhermemussi.admin.model.SessionTicket;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

@Path("/ticket")
public class TicketResource {
    @GET
    @RolesAllowed(TokenUtils.ROLE_PLAYER)
    @Produces(MediaType.TEXT_PLAIN)
    public String getTicket(@Context SecurityContext context) {
        // retrieve the username of the user logged in
        String username = context.getUserPrincipal().getName();

        // create a ticket and return the ticket id
        return SessionTicket.getSessionTicket(username).ticketId;
    }
}
