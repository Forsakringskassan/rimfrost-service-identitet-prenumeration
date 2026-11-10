package se.fk.github.rimfrost.identitetprenumeration.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.identitetprenumeration.integration.kafka.IdentitetUppdateradEventPublisher;

/**
 * Orchestrates event delivery when identitetsdata changes (FR-02).
 * Looks up active prenumerationer for the given identity and publishes an
 * {@link se.fk.rimfrost.identitet.uppdaterad.IdentitetUppdateradEvent} to each replyTo-topic.
 */
@ApplicationScoped
public class IdentitetUppdateradService
{

   private static final Logger log = LoggerFactory.getLogger(IdentitetUppdateradService.class);

   @Inject
   PrenumerationStore prenumerationStore;

   @Inject
   IdentitetUppdateradEventPublisher publisher;

   /**
    * Finds all active prenumerationer for the given identity and publishes an
    * IdentitetUppdateradEvent to each subscriber's replyTo-topic (FR-02.1).
    *
    * @param typId  identifier type
    * @param varde  identifier value
    */
   public void triggerIdentitetUppdaterad(String typId, String varde)
   {
      var prenumerationer = prenumerationStore.findByIdentitet(typId, varde);
      log.info("Triggering IdentitetUppdateradEvent for typId={} varde={} — {} subscriber(s)", typId, varde,
            prenumerationer.size());
      for (Prenumeration p : prenumerationer)
      {
         publisher.publish(p.prenumerationId(), p.replyTo());
      }
   }
}
