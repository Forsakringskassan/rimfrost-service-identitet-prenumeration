package se.fk.github.rimfrost.identitetprenumeration.presentation.rest;

import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
public class PrenumerationController implements PrenumerationControllerApi
{

   private static final Logger log = LoggerFactory.getLogger(PrenumerationController.class);

   @Inject
   PrenumerationStore prenumerationStore;

   @Inject
   RoutingContext routingContext;

   /**
    * Registers or replaces a prenumeration (FR-01.1, FR-01.5).
    * Returns HTTP 201 Created on success.
    *
    * @param createPrenumerationRequest the subscription details
    */
   @Override
   public void createPrenumeration(CreatePrenumerationRequest createPrenumerationRequest)
   {
      log.info("createPrenumeration: prenumerationId={}", createPrenumerationRequest.getPrenumerationId());
      var prenumeration = map(createPrenumerationRequest);
      prenumerationStore.upsert(prenumeration);
      routingContext.response().setStatusCode(201);
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
