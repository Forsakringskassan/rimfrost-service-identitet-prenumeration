package se.fk.github.rimfrost.identitetprenumeration.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.github.rimfrost.identitetprenumeration.integration.kafka.IdentitetUppdateradEventPublisher;

/** Unit tests for {@link IdentitetUppdateradService}. */
class IdentitetUppdateradServiceTest
{

   private PrenumerationStore prenumerationStore;
   private IdentitetUppdateradEventPublisher publisher;
   private IdentitetUppdateradService service;

   @BeforeEach
   void setUp()
   {
      prenumerationStore = mock(PrenumerationStore.class);
      publisher = mock(IdentitetUppdateradEventPublisher.class);
      service = new IdentitetUppdateradService();
      service.prenumerationStore = prenumerationStore;
      service.publisher = publisher;
   }

   @Test
   @DisplayName("KUNDPREN-FR-02.1: publishes IdentitetUppdateradEvent to each matching subscriber's replyTo")
   void triggerIdentitetUppdaterad_publishes_event_to_each_subscriber()
   {
      when(prenumerationStore.findByIdentitet("TYP", "123")).thenReturn(List.of(
            new Prenumeration("id-1", List.of(new Identitet("TYP", "123")), "reply-a"),
            new Prenumeration("id-2", List.of(new Identitet("TYP", "123")), "reply-b")));

      service.triggerIdentitetUppdaterad("TYP", "123");

      verify(publisher).publish("id-1", "reply-a");
      verify(publisher).publish("id-2", "reply-b");
   }

   @Test
   @DisplayName("KUNDPREN-FR-02.1: no event published when no subscribers match")
   void triggerIdentitetUppdaterad_does_nothing_when_no_subscribers()
   {
      when(prenumerationStore.findByIdentitet("TYP", "999")).thenReturn(List.of());

      service.triggerIdentitetUppdaterad("TYP", "999");

      verify(publisher, never()).publish(any(), any());
   }
}
