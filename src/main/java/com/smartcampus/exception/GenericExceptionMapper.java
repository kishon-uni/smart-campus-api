package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        LOGGER.severe("Unhandled exception: " + exception.getMessage());
        exception.printStackTrace();

        ErrorMessage error = new ErrorMessage(
                "An unexpected internal server error occurred.",
                500,
                "https://smartcampus.edu/api/docs/errors#internal-error"
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
