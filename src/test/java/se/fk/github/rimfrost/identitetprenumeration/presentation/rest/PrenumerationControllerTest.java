package se.fk.github.rimfrost.identitetprenumeration.presentation.rest;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.github.rimfrost.identitetprenumeration.application.PrenumerationStore;

/** Integration tests for {@link PrenumerationController}. */
@QuarkusTest
class PrenumerationControllerTest
{

   @InjectMock
   PrenumerationStore prenumerationStore;

   private static final String VALID_REQUEST = """
         {
           "prenumerationId": "00000000-0000-0000-0000-000000000001",
           "identiteter": [{"typId": "TYP", "varde": "123"}],
           "replyTo": "reply-topic"
         }
         """;

   @Test
   @DisplayName("KUNDPREN-FR-01.1: POST /prenumerationer returns 201 and stores prenumeration")
   void createPrenumeration_returns_201()
   {
      given()
            .contentType(ContentType.JSON)
            .body(VALID_REQUEST)
            .when()
            .post("/prenumerationer")
            .then()
            .statusCode(201);

      verify(prenumerationStore).upsert(any());
   }

   @Test
   @DisplayName("KUNDPREN-FR-01.5: POST /prenumerationer with duplicate prenumerationId returns 201 (upsert)")
   void createPrenumeration_upserts_on_duplicate_id()
   {
      given()
            .contentType(ContentType.JSON)
            .body(VALID_REQUEST)
            .when()
            .post("/prenumerationer")
            .then()
            .statusCode(201);

      given()
            .contentType(ContentType.JSON)
            .body(VALID_REQUEST)
            .when()
            .post("/prenumerationer")
            .then()
            .statusCode(201);
   }

   @Test
   @DisplayName("KUNDPREN-FR-01.1: POST /prenumerationer returns 400 when identiteter is missing")
   void createPrenumeration_returns_400_when_identiteter_missing()
   {
      given()
            .contentType(ContentType.JSON)
            .body("""
                  {
                    "prenumerationId": "00000000-0000-0000-0000-000000000001",
                    "replyTo": "reply-topic"
                  }
                  """)
            .when()
            .post("/prenumerationer")
            .then()
            .statusCode(400);

      verify(prenumerationStore, never()).upsert(any());
   }

   @Test
   @DisplayName("KUNDPREN-FR-01.2: DELETE /prenumerationer/{id} returns 204 when prenumeration exists")
   void deletePrenumeration_returns_204()
   {
      when(prenumerationStore.deleteByPrenumerationId("00000000-0000-0000-0000-000000000001"))
            .thenReturn(true);

      given()
            .when()
            .delete("/prenumerationer/00000000-0000-0000-0000-000000000001")
            .then()
            .statusCode(204);
   }

   @Test
   @DisplayName("KUNDPREN-FR-01.6: DELETE /prenumerationer/{id} returns 404 when prenumeration not found")
   void deletePrenumeration_returns_404_when_not_found()
   {
      when(prenumerationStore.deleteByPrenumerationId("00000000-0000-0000-0000-000000000099"))
            .thenReturn(false);

      given()
            .when()
            .delete("/prenumerationer/00000000-0000-0000-0000-000000000099")
            .then()
            .statusCode(404);
   }
}
