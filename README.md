# Book Service — Spring Boot REST Microservice

One Spring Boot app exposing **three services** on the same server: a book
catalog, an author list, and a lending service that reuses the book data
instead of duplicating it. All in-memory (no DB setup needed) so it runs
anywhere with just a JDK + Maven.

## Project layout

```
book-service/
├── pom.xml
└── src/main/java/com/example/bookservice/
    ├── BookServiceApplication.java    # main() — boots the embedded server
    ├── model/
    │   ├── Book.java                  # book data + validation rules + available flag
    │   └── Author.java                # author data + validation rules
    ├── service/
    │   ├── BookService.java           # book storage + search + borrow/return logic (also backs Lending)
    │   └── AuthorService.java         # author storage + business logic
    ├── controller/
    │   ├── BookController.java        # /api/books
    │   ├── AuthorController.java      # /api/authors
    │   └── LendingController.java     # /api/lending — composes BookService, no storage of its own
    └── exception/
        ├── BookNotFoundException.java
        ├── AuthorNotFoundException.java
        └── GlobalExceptionHandler.java  # turns ALL of the above errors into clean JSON + status codes
└── src/main/resources/application.properties
```

## How to run it

You need JDK 17+ and Maven installed.

```bash
cd book-service
mvn spring-boot:run
```

It starts on **http://localhost:8080**. Three sample books (ids 1–3) and two
sample authors (ids 1–2) are pre-loaded so search/browse isn't empty on first run.

If your laptop is tight on RAM: close other heavy apps before running
`mvn spring-boot:run`, and run it from a plain terminal rather than inside a
heavy IDE — that keeps memory use lowest for the demo.

## Endpoints

**Book service**

| Method | URL                          | Purpose                                              |
|--------|-------------------------------|-------------------------------------------------------|
| POST   | `/api/books`                  | Add a new book                                         |
| GET    | `/api/books`                  | View all books                                         |
| GET    | `/api/books?query=tolkien`    | Search by title or author (partial, case-insensitive)  |
| GET    | `/api/books/{id}`             | View one book by id                                    |

**Author service**

| Method | URL                     | Purpose                 |
|--------|-------------------------|--------------------------|
| POST   | `/api/authors`          | Add a new author          |
| GET    | `/api/authors`          | View all authors          |
| GET    | `/api/authors/{id}`     | View one author by id     |

**Lending service** (built on top of the book catalog — no separate storage)

| Method | URL                          | Purpose                                             |
|--------|-------------------------------|-------------------------------------------------------|
| PUT    | `/api/lending/{id}/borrow`    | Mark a book as borrowed (409 if already out)          |
| PUT    | `/api/lending/{id}/return`    | Mark a book as returned (409 if it wasn't out)        |
| GET    | `/api/lending/available`      | List books currently on the shelf                     |

## Sample request bodies

Add a book:
```json
{ "title": "Dune", "author": "Frank Herbert", "genre": "Sci-Fi" }
```
(`available` is set by the server — always `true` for a newly added book — so
don't send it.)

Add an author:
```json
{ "name": "Frank Herbert", "nationality": "American" }
```

## Testing with Postman

**Book service**
1. **Add a book**: `POST http://localhost:8080/api/books`, Body → raw → JSON,
   the sample body above → Send. Confirm `201`, note the returned `id`, and
   confirm `available: true`.
2. **Search**: `GET http://localhost:8080/api/books?query=tolkien` → Send.
   Confirm only "The Hobbit" comes back.
3. **View all**: `GET http://localhost:8080/api/books` → Send.
4. **View one**: `GET http://localhost:8080/api/books/1` → Send. Confirm `200`.
5. **Invalid input**: repeat step 1 with a blank `title` → Send. Confirm `400`
   with a `details` map naming the failed field.
6. **Not found**: `GET http://localhost:8080/api/books/999` → Send. Confirm `404`.

**Author service**
7. **Add an author**: `POST http://localhost:8080/api/authors` with the sample
   body above → Send. Confirm `201`.
8. **View all**: `GET http://localhost:8080/api/authors` → Send. Confirm the
   two seeded authors plus your new one.
9. **Invalid input**: repeat step 7 with `{ "name": "", "nationality": "" }` →
   Send. Confirm `400`.

**Lending service**
10. **Borrow**: `PUT http://localhost:8080/api/lending/1/borrow` → Send.
    Confirm `200` and `available: false`.
11. **Available list**: `GET http://localhost:8080/api/lending/available` →
    Send. Confirm book 1 no longer appears.
12. **Double-borrow (conflict)**: repeat step 10 → Send. Confirm `409`.
13. **Return**: `PUT http://localhost:8080/api/lending/1/return` → Send.
    Confirm `200` and `available: true`, then re-check step 11.

Screenshot each of these responses (status code + body) — that's your
evidence the APIs actually work, in place of a PPT/video.

## Viva talking points

**What each endpoint does**
- `POST /api/books`: accepts a JSON `Book`, validates it, assigns an id,
  forces `available = true` (a client can't add a book that's pre-borrowed),
  stores it, returns `201`.
- `GET /api/books?query=...`: a single endpoint does both jobs — no `query`
  param returns the whole catalog, a `query` filters by title *or* author,
  case-insensitive, partial match. Point out `@RequestParam(required = false)`
  is what makes the param optional.
- `GET /api/books/{id}`: path variable lookup; throws a custom exception if
  the id doesn't exist.
- `POST /api/authors` / `GET /api/authors` / `GET /api/authors/{id}`: same
  pattern as books, its own `AuthorService` and `AuthorNotFoundException`.
- `PUT /api/lending/{id}/borrow` and `/return`: `PUT` because they mutate the
  state of an existing book, not create anything. Each flips the `available`
  flag and throws `IllegalStateException` if the action doesn't make sense
  given the book's current state (borrowing an already-borrowed book,
  returning one that isn't out).
- `GET /api/lending/available`: reuses `BookService.searchBooks(null)` (the
  "no query" branch) and filters in the controller — no duplicate storage.

**Why three "services" but only two `@Service` classes**
`LendingController` is a full REST service from the client's point of view
(its own base path, its own endpoints) but has **no storage of its own** — it
injects `BookService` and calls `borrowBook`/`returnBook`/`searchBooks` on it.
That's the main design point to explain live: the `available` flag lives in
exactly one place (on the `Book` object inside `BookService`'s map), so
Lending can never drift out of sync with the catalog.

**What happens on invalid input**
`@Valid` runs the `@NotBlank` checks on `Book`/`Author` before the controller
method body executes; a failure throws `MethodArgumentNotValidException`
automatically, caught by `GlobalExceptionHandler` and turned into `400` with
a field-by-field map. A missing id throws a custom `*NotFoundException` → `404`.
A borrow/return that conflicts with the book's current state throws
`IllegalStateException` → `409 Conflict` (not `400`, because the request
itself is well-formed — it just clashes with the resource's current state).

**Likely follow-up questions to prepare for**
- Why `409` for double-borrow instead of `400`? → `400` means "your request
  is malformed"; `409` means "the request is fine, but it conflicts with the
  current state of the resource" — which is exactly what's happening.
- Why is `available` not settable by the client on `POST /api/books`? → it's
  server-controlled state, not client input; exposing it as a writable field
  would let a client add a book that's already "borrowed" with no lender.
- Why constructor injection everywhere instead of `@Autowired` fields? →
  makes the dependency explicit and required, and is what Spring itself
  recommends.
