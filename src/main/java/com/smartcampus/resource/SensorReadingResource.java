package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.DataNotFoundException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class SensorReadingResource {
    private static final String SENSOR_MAINTENANCE_STATUS = "MAINTENANCE";

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        Sensor sensor = DataStore.SENSORS.get(sensorId);
        if (sensor == null) {
            throw new DataNotFoundException("Sensor not found: " + sensorId);
        }
        List<SensorReading> readings = DataStore.SENSOR_READINGS.getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(readings).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
        Sensor sensor = DataStore.SENSORS.get(sensorId);
        if (sensor == null) {
            throw new DataNotFoundException("Sensor not found: " + sensorId);
        }
        if (SENSOR_MAINTENANCE_STATUS.equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is in " + SENSOR_MAINTENANCE_STATUS + " status and cannot accept new readings."
            );
        }

        reading.setId(DataStore.nextSensorReadingId());
        reading.setTimestamp(System.currentTimeMillis());

        List<SensorReading> readings = DataStore.SENSOR_READINGS.get(sensorId);
        if (readings ==  null) {
            readings = new ArrayList<>();
            DataStore.SENSOR_READINGS.put(sensorId, readings);
        }
        readings.add(reading);
        sensor.setCurrentValue(reading.getValue());

        URI location = uriInfo.getAbsolutePathBuilder().path(reading.getId()).build();
        return Response.created(location).entity(reading).build();
    }
}
