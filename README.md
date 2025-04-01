## Task 1 – Web Service for Multimedia Database

### ✅ A RESTful web service has been implemented using Spring Boot
- The project is a Spring Boot application.
- It exposes a REST API via the controller `MediaController.java`.

### ✅ The service exposes the following endpoints:

```xml
<endpoint method="GET" path="/test_connection">
    Returns a confirmation string indicating the server is running.
</endpoint>

<endpoint method="POST" path="/upload_sprite">
    Accepts a file (sprite image) using multipart/form-data.
    Returns a confirmation message with the file name.
</endpoint>

<endpoint method="POST" path="/upload_audio">
    Accepts a file (audio) using multipart/form-data.
    Returns a confirmation message with the file name.
</endpoint>

<endpoint method="POST" path="/player_score">
    Accepts a JSON object with the following structure:
    {
        "player_name": "String",
        "score": Integer
    }
    Inserts this document into the MongoDB database.
    Returns the inserted document ID.
</endpoint>
```

### ✅ MongoDB is used for persistent storage
- A cloud-based MongoDB cluster is used.
- Data is inserted into the `scores` collection in the `multimedia_db` database.

### ✅ Reactive MongoDB driver is used
- Reactive Streams are handled using Project Reactor (`Mono<T>`).

### ✅ Application is well-structured
- `MediaController` handles routes.
- `ServerService` handles data logic and database connections.
- `PlayerScore` is the data model used for mapping score submissions.

### ✅ Example request for /player_score:

```
json
POST /player_score
Content-Type: application/json

{
    "player_name": "Alice",
    "score": 3000
}
```
