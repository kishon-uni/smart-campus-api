package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        ErrorMessage error = new ErrorMessage(
                exception.getMessage(),
                403,
                "https://smartcampus.edu/api/docs/errors#sensor-unavailable"
        );
        return Response.status(Response.Status.FORBIDDEN)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
