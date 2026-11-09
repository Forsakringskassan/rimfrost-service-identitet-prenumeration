package se.fk.github.rimfrost.identitetprenumeration.application;

/**
 * Thrown when no prenumeration with the given ID exists.
 */
public class PrenumerationNotFoundException extends RuntimeException
{

   private static final long serialVersionUID = 1L;

   /**
    * @param prenumerationId the ID that was not found
    */
   public PrenumerationNotFoundException(String prenumerationId)
   {
      super("Prenumeration not found: " + prenumerationId);
   }
}
