package se.fk.github.rimfrost.identitetprenumeration.application;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for active prenumerationer.
 * This is a test/demo service — subscriptions are not persisted across restarts.
 */
@ApplicationScoped
public class PrenumerationStore
{

   private final ConcurrentHashMap<String, Prenumeration> store = new ConcurrentHashMap<>();

   /**
    * Adds or replaces the prenumeration identified by its prenumerationId (FR-01.5).
    * Full replace — all fields are overwritten if the ID already exists.
    *
    * @param prenumeration the prenumeration to store
    */
   public void upsert(Prenumeration prenumeration)
   {
      store.put(prenumeration.prenumerationId(), prenumeration);
   }

   /**
    * Removes the prenumeration with the given ID (FR-01.6).
    *
    * @param prenumerationId the client-chosen ID to remove
    * @return true if a prenumeration was removed, false if no match was found
    */
   public boolean deleteByPrenumerationId(String prenumerationId)
   {
      return store.remove(prenumerationId) != null;
   }

   /**
    * Returns all prenumerationer where any of the stored identiteter matches the given typId and varde.
    *
    * @param typId identifier type
    * @param varde identifier value
    * @return matching prenumerationer, possibly empty
    */
   public List<Prenumeration> findByIdentitet(String typId, String varde)
   {
      return store.values().stream()
            .filter(p -> p.identiteter().stream()
                  .anyMatch(id -> typId.equals(id.typId()) && varde.equals(id.varde())))
            .toList();
   }
}
