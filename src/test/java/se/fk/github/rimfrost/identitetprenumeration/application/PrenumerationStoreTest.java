package se.fk.github.rimfrost.identitetprenumeration.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PrenumerationStore}. */
class PrenumerationStoreTest
{

   private PrenumerationStore store;

   @BeforeEach
   void setUp()
   {
      store = new PrenumerationStore();
   }

   @Test
   @DisplayName("IDPREN-FR-01.1: upsert stores a new prenumeration")
   void upsert_stores_prenumeration()
   {
      store.upsert(new Prenumeration("id-1", List.of(new Identitet("TYP", "123")), "reply-topic"));

      assertThat(store.findByIdentitet("TYP", "123")).hasSize(1);
   }

   @Test
   @DisplayName("IDPREN-FR-01.5: duplicate prenumerationId is overwritten (idempotent upsert)")
   void upsert_overwrites_existing_prenumeration()
   {
      store.upsert(new Prenumeration("id-1", List.of(new Identitet("TYP", "123")), "reply-old"));
      store.upsert(new Prenumeration("id-1", List.of(new Identitet("TYP", "123")), "reply-new"));

      var result = store.findByIdentitet("TYP", "123");
      assertThat(result).hasSize(1);
      assertThat(result.get(0).replyTo()).isEqualTo("reply-new");
   }

   @Test
   @DisplayName("IDPREN-FR-01.2: deleteByPrenumerationId removes an existing prenumeration and returns true")
   void deleteByPrenumerationId_removes_prenumeration()
   {
      store.upsert(new Prenumeration("id-1", List.of(new Identitet("TYP", "123")), "reply-topic"));

      boolean deleted = store.deleteByPrenumerationId("id-1");

      assertThat(deleted).isTrue();
      assertThat(store.findByIdentitet("TYP", "123")).isEmpty();
   }

   @Test
   @DisplayName("IDPREN-FR-01.6: deleteByPrenumerationId returns false for unknown ID")
   void deleteByPrenumerationId_returns_false_for_unknown_id()
   {
      boolean deleted = store.deleteByPrenumerationId("unknown-id");

      assertThat(deleted).isFalse();
   }

   @Test
   @DisplayName("IDPREN-FR-02.1: findByIdentitet returns all prenumerationer matching the given identity")
   void findByIdentitet_returns_matching_prenumerationer()
   {
      store.upsert(new Prenumeration("id-1", List.of(new Identitet("TYP", "123")), "reply-a"));
      store.upsert(new Prenumeration("id-2", List.of(new Identitet("TYP", "123")), "reply-b"));
      store.upsert(new Prenumeration("id-3", List.of(new Identitet("TYP", "999")), "reply-c"));

      assertThat(store.findByIdentitet("TYP", "123")).hasSize(2);
   }

   @Test
   @DisplayName("IDPREN-FR-02.1: findByIdentitet returns empty list when no match")
   void findByIdentitet_returns_empty_when_no_match()
   {
      assertThat(store.findByIdentitet("TYP", "no-match")).isEmpty();
   }

   @Test
   @DisplayName("IDPREN-FR-02.1: findByIdentitet matches across multiple identiteter in a prenumeration")
   void findByIdentitet_matches_second_identity_in_list()
   {
      store.upsert(new Prenumeration("id-1",
            List.of(new Identitet("TYP-A", "aaa"), new Identitet("TYP-B", "bbb")),
            "reply-topic"));

      assertThat(store.findByIdentitet("TYP-B", "bbb")).hasSize(1);
      assertThat(store.findByIdentitet("TYP-A", "aaa")).hasSize(1);
   }
}
