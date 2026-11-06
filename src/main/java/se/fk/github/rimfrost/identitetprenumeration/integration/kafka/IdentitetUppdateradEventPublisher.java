package se.fk.github.rimfrost.identitetprenumeration.integration.kafka;

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.identitet.uppdaterad.IdentitetUppdateradEvent;

/**
 * Publishes {@link IdentitetUppdateradEvent} to the replyTo-topic of each prenumerant (FR-02.1).
 */
@ApplicationScoped
public class IdentitetUppdateradEventPublisher
{

   private static final Logger log = LoggerFactory.getLogger(IdentitetUppdateradEventPublisher.class);

   @Inject
   @Channel("identitet-uppdaterad-out")
   @OnOverflow(value = OnOverflow.Strategy.FAIL)
   Emitter<IdentitetUppdateradEvent> emitter;

   /**
    * Publishes an event to the given dynamic replyTo-topic.
    *
    * @param prenumerationId the client-chosen subscription ID
    * @param replyTo         the Kafka topic to publish to
    */
   public void publish(String prenumerationId, String replyTo)
   {
      log.info("Publishing IdentitetUppdateradEvent: prenumerationId={} replyTo={}", prenumerationId, replyTo);
      var event = new IdentitetUppdateradEvent();
      event.setPrenumerationId(prenumerationId);
      var metadata = OutgoingKafkaRecordMetadata.builder().withTopic(replyTo).build();
      emitter.send(Message.of(event).addMetadata(metadata));
   }
}
