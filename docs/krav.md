# Krav — rimfrost-service-kund-prenumeration

Denna service tillhandahåller en generell subscribe/unsubscribe-mekanism mot identitetsdata.
När en användare kompletterar en uppgift på Mina sidor uppdateras identitetsdata i
rimfrost-extern. Regler som behöver reagera på sådana förändringar — primärt kompletteringsregler
— kan via denna service prenumerera på ändringar för en eller flera identiteter. När en
förändring sker levereras ett `IdentitetUppdateradEvent` över Kafka, så att regeln kan påverka
sitt utfall, avsluta OUL-uppgiften, uppdatera Mimer och skicka svar tillbaka till processen.

## 1. Funktionella krav

### KUNDPREN-FR-01 — Prenumerationshantering

- **KUNDPREN-FR-01.1** Servicen ska exponera `POST /prenumerationer` för att registrera en
  prenumeration på identitetsförändringar. Anropet ska acceptera ett `CreatePrenumerationRequest`
  med fälten `prenumerationId` (UUID, klient-valt), `identiteter` (lista med minst ett `Idtyp`
  med `typId` och `varde`) samt `replyTo` (Kafka-topic dit `IdentitetUppdateradEvent` levereras).
  Returnerar HTTP 201 vid framgång och HTTP 400 vid ogiltig förfrågan.
- **KUNDPREN-FR-01.2** Servicen ska exponera `DELETE /prenumerationer/{prenumerationId}` för att
  avregistrera en befintlig prenumeration. Returnerar HTTP 204 vid framgång.
- **KUNDPREN-FR-01.3** En prenumeration ska knytas till prenumerantens `replyTo`-topic, som
  angavs vid registreringen. Servicen ska inte ha hårdkodad kännedom om sina konsumenters
  Kafka-topics.
- **KUNDPREN-FR-01.4** Servicen ska vara regeltyps-agnostisk — den ska kunna användas av
  godtyckliga regler.
- **KUNDPREN-FR-01.5** Om `POST /prenumerationer` anropas med ett `prenumerationId` som redan
  finns ska prenumerationen ersättas i sin helhet (full replace av `identiteter` och `replyTo`)
  och HTTP 201 returneras.
- **KUNDPREN-FR-01.6** Om `DELETE /prenumerationer/{prenumerationId}` anropas med ett
  `prenumerationId` som inte finns ska HTTP 404 returneras.

### KUNDPREN-FR-02 — Eventleverans vid förändring

- **KUNDPREN-FR-02.1** När identitetsdata förändras för en prenumererad identitet ska servicen
  publicera ett `IdentitetUppdateradEvent` på varje matchande prenumerants `replyTo`-topic —
  flera prenumeranter kan bevaka samma identitet och ska alla notifieras.
- **KUNDPREN-FR-02.2** `IdentitetUppdateradEvent` ska innehålla `prenumerationId` (UUID) som
  kopplar leveransen till den ursprungliga prenumerationen. Eventet ska inte innehålla det
  uppdaterade datat — mottagaren ansvarar för att hämta aktuell information via lämpligt
  gränssnitt.

### KUNDPREN-FR-03 — Testendpoint för manuell eventtrigger

- **KUNDPREN-FR-03.1** Servicen ska exponera `POST /test/identitet-uppdaterad` för manuell
  testning och demos i Kubernetes-miljö. Endpointen definieras i
  `rimfrost-service-identitet-prenumeration-test-openapi` och accepterar ett `TriggerRequest`
  med fälten `idtyp` (string) och `idvarde` (string).
- **KUNDPREN-FR-03.2** Vid anrop ska servicen söka upp alla aktiva prenumerationer där någon av
  `identiteter` matchar den givna kombinationen (`typId` == `idtyp` och `varde` == `idvarde`)
  och publicera ett `IdentitetUppdateradEvent` på respektive prenumerants `replyTo`-topic.
  Returnerar HTTP 204 oavsett om några prenumerationer matchades.
- **KUNDPREN-FR-03.3** Om `idtyp` eller `idvarde` saknas i anropet ska HTTP 400 returneras.


---

## 2. Icke-funktionella krav

### KUNDPREN-NFR-02 — Observerbarhet

- **KUNDPREN-NFR-02.1** Servicen ska logga subscribe- och unsubscribe-anrop med `prenumerationId`
  och `replyTo`-topic.
- **KUNDPREN-NFR-02.2** Servicen ska logga när ett event levereras till en prenumerant.
