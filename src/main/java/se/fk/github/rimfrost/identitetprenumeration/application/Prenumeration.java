package se.fk.github.rimfrost.identitetprenumeration.application;

import java.util.List;

/**
 * Internal representation of an active subscription.
 *
 * @param prenumerationId client-chosen ID that correlates subscribe/unsubscribe and IdentitetUppdateradEvent
 * @param identiteter     one or more identities whose changes this subscription monitors
 * @param replyTo         Kafka topic to publish IdentitetUppdateradEvent to when any identity changes
 */
public record Prenumeration(String prenumerationId,List<Identitet>identiteter,String replyTo){
/** Makes {@code identiteter} an unmodifiable copy to satisfy SpotBugs EI_EXPOSE_REP. */
public Prenumeration{identiteter=List.copyOf(identiteter);}}
