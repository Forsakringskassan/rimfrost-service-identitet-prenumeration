package se.fk.github.rimfrost.identitetprenumeration.application;

/**
 * Application-layer representation of an identity type/value pair.
 *
 * @param typId identifier type (e.g. a UUID or a string code such as PERSONNUMMER)
 * @param varde identifier value
 */
public record Identitet(String typId,String varde){}
