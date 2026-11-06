# Plan — FKPOC-1328: subscribe/unsubscribe via OpenAPI (REST)

Base package: `se.fk.github.rimfrost.identitetprenumeration`

The existing implementation uses Kafka consumers for subscribe/unsubscribe (`kund.subscribe` /
`kund.unsubscribe` topics). The updated krav replaces this with REST endpoints defined in
`rimfrost-service-identitet-prenumeration-openapi`. This plan migrates the service to that model.

## Steps

- [ ] **1. Rename repo — kund → identitet**
- [ ] **2. Update dependencies**
- [ ] **3. Redesign domain model**
- [ ] **4. Update event model and publisher**
- [ ] **5. Remove Kafka consumers**
- [ ] **6. Add REST controller — PrenumerationController**
- [ ] **7. Update application service**
- [ ] **8. Update test endpoint**
- [ ] **9. Update tests**

---

## Design

### Step 1 — Rename repo — kund → identitet

Layer: **all**

| What | From | To |
|------|------|----|
| GitHub repo name | `rimfrost-service-kund-prenumeration` | `rimfrost-service-identitet-prenumeration` |
| Local directory | `rimfrost-service-kund-prenumeration/` | `rimfrost-service-identitet-prenumeration/` |
| Maven `artifactId` (root + submodules) | `rimfrost-service-kund-prenumeration` | `rimfrost-service-identitet-prenumeration` |
| Java base package | `se.fk.github.rimfrost.kundprenumeration` | `se.fk.github.rimfrost.identitetprenumeration` |
| Class name prefixes | `Kund*` | `Identitet*` (where semantically correct) |
| Config keys | `kund-*`, `kund.*` | `identitet-*`, `identitet.*` |
| Plan/docs internal references | `kund-prenumeration` | `identitet-prenumeration` |

Rename via `gh repo rename rimfrost-service-identitet-prenumeration`, then update local remote URL.
Java package rename via IDE refactor (or sed across `src/`).

### Step 2 — Update dependencies

Layer: **build**

| Change | Artifact | Action |
|--------|----------|--------|
| Add | `rimfrost-service-identitet-prenumeration-openapi-jaxrs-spec` | Provides generated JAX-RS interface + `CreatePrenumerationRequest`, `Idtyp` |
| Replace | `rimfrost-service-kund-prenumeration-test-openapi-jaxrs-spec` → `rimfrost-service-identitet-prenumeration-test-openapi-jaxrs-spec` | Renamed artifact (kund → identitet) |
| Keep | `rimfrost-service-kund-prenumeration-asyncapi` | Still needed for `IdentitetUppdateradEvent` |
| Keep | `quarkus-messaging-kafka` | Still needed for outgoing event publishing |

### Step 3 — Redesign domain model

Layer: **application**
Package: `se.fk.github.rimfrost.identitetprenumeration.application`

`Prenumeration` record: single `typId`/`varde` pair → list of identities:

```
Prenumeration(String prenumerationId, List<Idtyp> identiteter, String replyTo)
```

`PrenumerationStore` changes:

```
void    upsert(Prenumeration)                                    // unchanged — full replace on existing id
boolean deleteByPrenumerationId(String id)                       // returns false if not found (FR-01.6 → 404)
List<Prenumeration> findByIdentitet(String typId, String varde)  // searches across identiteter list
```

`Idtyp` sourced from generated `rimfrost-service-identitet-prenumeration-openapi-jaxrs-spec`
(replaces the asyncapi-generated one).

### Step 4 — Update event model and publisher

Layer: **integration/kafka**
Package: `se.fk.github.rimfrost.identitetprenumeration.integration.kafka`

Replace `KundChangedEvent` + `KundChangedEventPublisher` with:

```
IdentitetUppdateradEventPublisher
  @Channel("identitet-uppdaterad-out")
  Emitter<IdentitetUppdateradEvent> emitter

  void publish(String prenumerationId, String replyTo)
    → emitter.send(Message.of(new IdentitetUppdateradEvent(prenumerationId)).addMetadata(topicMetadata(replyTo)))
```

`application.properties`:
```
mp.messaging.outgoing.identitet-uppdaterad-out.connector=smallrye-kafka
mp.messaging.outgoing.identitet-uppdaterad-out.value.serializer=io.quarkus.kafka.client.serialization.ObjectMapperSerializer
```

Remove `mp.messaging.outgoing.kund-changed-out.*`.

### Step 5 — Remove Kafka consumers

Layer: **presentation/kafka**

Delete:
- `KundSubscribeConsumer.java`
- `KundUnsubscribeConsumer.java`
- `KundSubscribeDeserializer.java`
- `KundUnsubscribeDeserializer.java`

Remove from `application.properties`:
```
mp.messaging.incoming.kund-subscribe.*
mp.messaging.incoming.kund-unsubscribe.*
```

### Step 6 — Add REST controller

Layer: **presentation/rest** — new cross-layer dependency: presentation → application (`PrenumerationStore`)

```
@ApplicationScoped
PrenumerationController implements PrenumerationControllerApi  // generated from openapi jaxrs-spec

  Response createPrenumeration(CreatePrenumerationRequest req)
    → store.upsert(map(req))
    → return 201

  Response deletePrenumeration(UUID prenumerationId)
    → boolean deleted = store.deleteByPrenumerationId(prenumerationId.toString())
    → deleted ? 204 : 404
```

### Step 7 — Update application service

Layer: **application**

```
IdentitetUppdateradService  (renamed from KundChangedService)
  void triggerIdentitetUppdaterad(String typId, String varde)
    → store.findByIdentitet(typId, varde)
    → for each: publisher.publish(p.prenumerationId(), p.replyTo())
```

Only changes: rename, `findByTypIdAndVarde` → `findByIdentitet`, updated publisher call.

### Step 8 — Update test endpoint

Layer: **presentation/rest**

`TestController` implements the renamed JAX-RS interface from
`rimfrost-service-identitet-prenumeration-test-openapi-jaxrs-spec`.

Path changes from `POST /test/kundcentrerat-data-Changed` → `POST /test/identitet-uppdaterad`.
`TriggerRequest` and `TestControllerApi` sourced from new artifact (same field names: `idtyp`, `idvarde`).
Delegate: `IdentitetUppdateradService.triggerIdentitetUppdaterad(req.getIdtyp(), req.getIdvarde())`.

### Step 9 — Update tests

- Remove `KundSubscribeConsumerTest`, `KundUnsubscribeConsumerTest`
- Add `PrenumerationControllerTest` (`@QuarkusTest`):
  - `POST /prenumerationer` → 201, subscription stored
  - `POST /prenumerationer` with same `prenumerationId` → 201, full replace (FR-01.5)
  - `DELETE /prenumerationer/{id}` → 204 (FR-01.2)
  - `DELETE /prenumerationer/{unknown-id}` → 404 (FR-01.6)
- Update `PrenumerationStoreTest`: `delete` returns boolean; `findByIdentitet` searches across list
- Update `IdentitetUppdateradServiceTest`: uses `IdentitetUppdateradEvent`, updated publisher
- Update `TestControllerTest`: new path `/test/identitet-uppdaterad`
