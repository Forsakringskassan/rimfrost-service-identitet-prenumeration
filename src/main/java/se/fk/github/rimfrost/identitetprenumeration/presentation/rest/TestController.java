package se.fk.github.rimfrost.identitetprenumeration.presentation.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.github.rimfrost.identitetprenumeration.application.IdentitetUppdateradService;
import se.fk.rimfrost.service.identitet.prenumeration.test.jaxrsspec.controllers.generatedsource.TestControllerApi;
import se.fk.rimfrost.service.identitet.prenumeration.test.jaxrsspec.controllers.generatedsource.model.TriggerRequest;

/**
 * Implements the test endpoint for manually triggering an identitet-uppdaterad event.
 * Only intended for use in test and demo environments (FR-03).
 */
@ApplicationScoped
public class TestController implements TestControllerApi
{

   @Inject
   IdentitetUppdateradService identitetUppdateradService;

   /**
    * Triggers an IdentitetUppdateradEvent for the given idtyp/idvarde combination (FR-03).
    *
    * @param triggerRequest the trigger request containing idtyp and idvarde
    */
   @Override
   public void triggerIdentitetDataChanged(TriggerRequest triggerRequest)
   {
      identitetUppdateradService.triggerIdentitetUppdaterad(triggerRequest.getIdtyp(), triggerRequest.getIdvarde());
   }
}
