package com.smartcampus.resource;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class DiscoveryResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> discover() {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> resources = new HashMap<>();

        response.put("version", "1.0");
        response.put("description", "Smart Campus Sensor & Room Management API");
        response.put("contact", "admin@smartcampus.edu");

        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        response.put("resources", resources);

        return response;
    }
}
