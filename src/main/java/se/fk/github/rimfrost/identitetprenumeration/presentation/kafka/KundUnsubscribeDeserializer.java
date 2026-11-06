package se.fk.github.rimfrost.identitetprenumeration.presentation.kafka;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import se.fk.rimfrost.kund.prenumeration.KundUnsubscribeRequest;

/** Deserializer for {@link KundUnsubscribeRequest} Kafka messages. */
@SuppressWarnings("unused")
public class KundUnsubscribeDeserializer extends ObjectMapperDeserializer<KundUnsubscribeRequest>
{

   public KundUnsubscribeDeserializer()
   {
      super(KundUnsubscribeRequest.class);
   }
}
