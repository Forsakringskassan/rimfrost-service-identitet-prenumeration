package se.fk.github.rimfrost.identitetprenumeration.integration.kafka;

/**
 * Thrown when publishing an {@link se.fk.rimfrost.identitet.uppdaterad.IdentitetUppdateradEvent} to Kafka fails.
 */
public class IdentitetUppdateradPublishException extends RuntimeException
{

   /**
    * @param prenumerationId the ID of the prenumeration whose event could not be published
    * @param cause           the underlying Kafka or messaging failure
    */
   public IdentitetUppdateradPublishException(String prenumerationId, Throwable cause)
   {
      super("Failed to publish IdentitetUppdateradEvent for prenumerationId: " + prenumerationId, cause);
   }
}
