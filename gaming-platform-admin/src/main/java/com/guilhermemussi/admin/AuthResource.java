package com.guilhermemussi.admin;

import com.guilhermemussi.admin.model.PlayerCredential;
import com.guilhermemussi.admin.config.TokenUtils;
import org.eclipse.microprofile.jwt.Claims;
import org.jose4j.jwt.JwtClaims;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

@Path("auth")
@RequestScoped
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.TEXT_PLAIN)
public class AuthResource {
    public static Logger LOGGER = Logger.getLogger(AuthResource.class.getName());

    @GET
    @Path("/available/{username}")
    public Boolean usernameExists(@PathParam("username") String username) {
        return PlayerCredential.findByIdOptional(username).isPresent();
    }

    @GET
    @Path("me")
    @RolesAllowed(TokenUtils.ROLE_PLAYER)
    public String getPlayer(@Context SecurityContext ctx) {
        Principal caller =  ctx.getUserPrincipal();
        return caller.getName();
    }

    @POST
    @Path("login")
    @PermitAll
    public String login(@FormParam("username") String username, @FormParam("password") String password) throws Exception {
        String encodedPass = new String(MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8)));

        Optional<PlayerCredential> player = PlayerCredential.findByIdOptional(username);
        PlayerCredential p = player.orElseThrow(() -> new IllegalArgumentException("Username not found"));

        JwtClaims claims = new JwtClaims();
        claims.setIssuer("http://gaming-platform");
        claims.setJwtId("gaming-platform");
        claims.setSubject(username);
        claims.setClaim(Claims.upn.name(), username);
        claims.setClaim(Claims.preferred_username.name(), username);
        claims.setClaim(Claims.groups.name(), Arrays.asList(TokenUtils.ROLE_PLAYER));
        claims.setExpirationTimeMinutesInTheFuture(600);
        claims.setAudience("gaming-platform");

        String token = TokenUtils.generateTokenString(claims);

        LOGGER.info("User " + username + " logged in");

        return token;
    }

    @POST
    @Path("register")
    @PermitAll
    public String register(@FormParam("username") String username, @FormParam("password") String password) throws Exception {
        if (PlayerCredential.findByIdOptional(username).isPresent()) {
            throw new IllegalArgumentException("Username " + username + " already exists");
        }

        PlayerCredential.register(username, password);

        return login(username, password);
    }
 }
