package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/sensors")
public class SensorResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Sensor> getAllSensors(@QueryParam("type") @DefaultValue("") String type) {
        List<Sensor> all = new ArrayList<>(DataStore.SENSORS.values());
        if (!type.isEmpty()) {
            return all.stream()
                    .filter(sensor -> type.equalsIgnoreCase(sensor.getType()))
                   .collect(Collectors.toList());
        }
        return all;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor.getRoomId() == null || !DataStore.ROOMS.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "Cannot create sensor: room '" + sensor.getRoomId() + "' does not exist."
            );
        }
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            sensor.setId("S" + (DataStore.SENSORS.size() + 1));
        }
        if (sensor.getStatus() == null || sensor.getStatus().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        DataStore.SENSORS.put(sensor.getId(), sensor);
        DataStore.SENSOR_READINGS.put(sensor.getId(), new ArrayList<>());
        DataStore.ROOMS.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
