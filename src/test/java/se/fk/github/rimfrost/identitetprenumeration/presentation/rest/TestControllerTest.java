package se.fk.github.rimfrost.identitetprenumeration.presentation.rest;

import static io.restassured.RestAssured.given;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.github.rimfrost.identitetprenumeration.application.IdentitetUppdateradService;

/** Tests for {@link TestController}. */
@QuarkusTest
class TestControllerTest
{

   @InjectMock
   IdentitetUppdateradService identitetUppdateradService;

   @Test
   @DisplayName("KUNDPREN-FR-03.1: POST /test/identitet-data-changed returns 204 on valid request")
   void trigger_returns_204_on_valid_request()
   {
      given()
            .contentType(ContentType.JSON)
            .body("{\"idtyp\":\"TYP\",\"idvarde\":\"123\"}")
            .when()
            .post("/test/identitet-data-changed")
            .then()
            .statusCode(204);

      Mockito.verify(identitetUppdateradService).triggerIdentitetUppdaterad("TYP", "123");
   }

   @Test
   @DisplayName("KUNDPREN-FR-03.3: POST /test/identitet-data-changed returns 400 when idtyp is missing")
   void trigger_returns_400_when_idtyp_missing()
   {
      given()
            .contentType(ContentType.JSON)
            .body("{\"idvarde\":\"123\"}")
            .when()
            .post("/test/identitet-data-changed")
            .then()
            .statusCode(400);
   }

   @Test
   @DisplayName("KUNDPREN-FR-03.3: POST /test/identitet-data-changed returns 400 when idvarde is missing")
   void trigger_returns_400_when_idvarde_missing()
   {
      given()
            .contentType(ContentType.JSON)
            .body("{\"idtyp\":\"TYP\"}")
            .when()
            .post("/test/identitet-data-changed")
            .then()
            .statusCode(400);
   }
}
