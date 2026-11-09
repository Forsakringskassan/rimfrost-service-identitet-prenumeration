package se.fk.github.rimfrost.identitetprenumeration.integration.kafka;

import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.identitet.uppdaterad.IdentitetUppdateradEvent;

import java.time.Duration;

/**
 * Publishes {@link IdentitetUppdateradEvent} to the replyTo-topic of each prenumerant (FR-02.1).
 */
@ApplicationScoped
public class IdentitetUppdateradEventPublisher
{

   private static final Logger log = LoggerFactory.getLogger(IdentitetUppdateradEventPublisher.class);

   @Channel("identitet-uppdaterad-out")
   MutinyEmitter<IdentitetUppdateradEvent> emitter;

   @ConfigProperty(name = "identitet-uppdaterad.publish-timeout", defaultValue = "10s")
   Duration publishTimeout;

   /**
    * Publishes an event to the given dynamic replyTo-topic.
    * Blocks until Kafka acknowledges the message or the timeout elapses.
    *
    * @param prenumerationId the client-chosen subscription ID
    * @param replyTo         the Kafka topic to publish to
    * @throws IdentitetUppdateradPublishException if the message could not be delivered within the timeout
    */
   public void publish(String prenumerationId, String replyTo)
   {
      log.info("Publishing IdentitetUppdateradEvent: prenumerationId={} replyTo={}", prenumerationId, replyTo);
      var event = new IdentitetUppdateradEvent();
      event.setPrenumerationId(prenumerationId);
      var metadata = OutgoingKafkaRecordMetadata.builder().withTopic(replyTo).build();
      var message = Message.of(event).addMetadata(metadata);
      try
      {
         emitter.sendMessage(message).await().atMost(publishTimeout);
      }
      catch (RuntimeException e)
      {
         log.error("Failed to publish IdentitetUppdateradEvent: prenumerationId={} replyTo={}", prenumerationId, replyTo, e);
         throw new IdentitetUppdateradPublishException(prenumerationId, e);
      }
   }
}
