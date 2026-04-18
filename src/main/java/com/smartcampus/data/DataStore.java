package com.smartcampus.data;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {
    public static final Map<String, Room> ROOMS = new HashMap<>();
    public static final Map<String, Sensor> SENSORS = new HashMap<>();
    public static final Map<String, List<SensorReading>> SENSOR_READINGS = new HashMap<>();

    static {
        Room room1 = new Room();
        room1.setId("LIB-301");
        room1.setName("Library Quiet Study");
        room1.setCapacity(40);
        ROOMS.put(room1.getId(), room1);

        Room room2 = new Room();
        room2.setId("ENG-101");
        room2.setName("Engineering Lab A");
        room2.setCapacity(25);
        ROOMS.put(room2.getId(), room2);

        Sensor sensor1 = new Sensor();
        sensor1.setId("TEMP-001");
        sensor1.setType("Temperature");
        sensor1.setStatus("ACTIVE");
        sensor1.setCurrentValue(21.5);
        sensor1.setRoomId("LIB-301");
        SENSORS.put(sensor1.getId(), sensor1);
        ROOMS.get("LIB-301").getSensorIds().add(sensor1.getId());
        SENSOR_READINGS.put(sensor1.getId(), new ArrayList<>());

        Sensor sensor2 = new Sensor();
        sensor2.setId("CO2-001");
        sensor2.setType("CO2");
        sensor2.setStatus("ACTIVE");
        sensor2.setCurrentValue(450.0);
        sensor2.setRoomId("ENG-101");
        SENSORS.put(sensor2.getId(), sensor2);
        ROOMS.get("ENG-101").getSensorIds().add(sensor2.getId());
        SENSOR_READINGS.put(sensor2.getId(), new ArrayList<>());

        Sensor sensor3 = new Sensor();
        sensor3.setId("OCC-001");
        sensor3.setType("Occupancy");
        sensor3.setStatus("MAINTENANCE");
        sensor3.setCurrentValue(0.0);
        sensor3.setRoomId("LIB-301");
        SENSORS.put(sensor3.getId(), sensor3);
        ROOMS.get("LIB-301").getSensorIds().add(sensor3.getId());
        SENSOR_READINGS.put(sensor3.getId(), new ArrayList<>());
    }
}
