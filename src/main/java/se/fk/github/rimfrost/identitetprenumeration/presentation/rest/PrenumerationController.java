package se.fk.github.rimfrost.identitetprenumeration.presentation.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.identitetprenumeration.application.Identitet;
import se.fk.github.rimfrost.identitetprenumeration.application.Prenumeration;
import se.fk.github.rimfrost.identitetprenumeration.application.PrenumerationNotFoundException;
import se.fk.github.rimfrost.identitetprenumeration.application.PrenumerationStore;
import se.fk.rimfrost.service.identitet.prenumeration.jaxrsspec.controllers.generatedsource.PrenumerationControllerApi;
import se.fk.rimfrost.service.identitet.prenumeration.jaxrsspec.controllers.generatedsource.model.CreatePrenumerationRequest;

import java.util.UUID;

/**
 * REST controller for prenumeration registration and cancellation (FR-01).
 */
@ApplicationScoped
@Path("/")
public class PrenumerationController implements PrenumerationControllerApi
{

   private static final Logger log = LoggerFactory.getLogger(PrenumerationController.class);

   @Inject
   PrenumerationStore prenumerationStore;

   /**
    * Registers or replaces a prenumeration (FR-01.1, FR-01.5).
    * Returns HTTP 201 Created on success.
    * Throws WebApplicationException(201) because the generated interface returns void,
    * and JAX-RS defaults void to 204.
    *
    * @param createPrenumerationRequest the subscription details
    */
   @Override
   public void createPrenumeration(CreatePrenumerationRequest createPrenumerationRequest)
   {
      log.info("createPrenumeration: prenumerationId={}", createPrenumerationRequest.getPrenumerationId());
      prenumerationStore.upsert(map(createPrenumerationRequest));
      throw new WebApplicationException(Response.status(Response.Status.CREATED).build());
   }

   /**
    * Removes a prenumeration by ID (FR-01.2, FR-01.6).
    * Returns HTTP 204 No Content on success, 404 if not found.
    *
    * @param prenumerationId the subscription ID to remove
    * @throws PrenumerationNotFoundException if no matching prenumeration exists
    */
   @Override
   public void deletePrenumeration(UUID prenumerationId)
   {
      log.info("deletePrenumeration: prenumerationId={}", prenumerationId);
      boolean deleted = prenumerationStore.deleteByPrenumerationId(prenumerationId.toString());
      if (!deleted)
      {
         throw new PrenumerationNotFoundException(prenumerationId.toString());
      }
   }

   /**
    * Maps the generated request model to the application-layer record.
    * Converts each {@code Idtyp} entry to an {@link Identitet}.
    */
   private Prenumeration map(CreatePrenumerationRequest req)
   {
      var identiteter = req.getIdentiteter().stream()
            .map(idtyp -> new Identitet(idtyp.getTypId(), idtyp.getVarde()))
            .toList();
      return new Prenumeration(req.getPrenumerationId().toString(), identiteter, req.getReplyTo());
   }
}
