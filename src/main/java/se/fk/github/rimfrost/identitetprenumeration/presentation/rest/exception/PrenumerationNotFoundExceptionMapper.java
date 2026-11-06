package se.fk.github.rimfrost.identitetprenumeration.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.fk.github.rimfrost.identitetprenumeration.application.PrenumerationNotFoundException;

/**
 * Maps {@link PrenumerationNotFoundException} to HTTP 404 Not Found.
 */
@Provider
public class PrenumerationNotFoundExceptionMapper implements ExceptionMapper<PrenumerationNotFoundException>
{

   @Override
   public Response toResponse(PrenumerationNotFoundException exception)
   {
      return Response.status(Response.Status.NOT_FOUND).build();
   }
}
