# Article model

This document defines the minimal Article model for the Scholarly Mail project and explains its fields, basic validation, example JSON, and suggested endpoints that operate on the model. Add this file to your repository root as `README_ArticleModel.md` or incorporate the content into your main README.

---

## Model: `Article`

**Purpose:** store metadata for scholarly articles that are retrieved by the system or emailed to users. The model is intentionally minimal and optimized for searching and marking read/unread status.

### Fields (minimal set)

- `id` (string, required)
  - Unique identifier for the article document. Use UUIDs or the database-generated ID.

- `title` (string, required)
  - Full title of the article.

- `authors` (array of strings, optional)
  - Ordered list of author names. Keep as simple strings (e.g., "Jane Doe") for indexing.

- `url` (string, required)
  - Canonical URL or DOI link to the article (used for opening the original source).

- `tags` (array of strings, optional)
  - User-defined or auto-generated tags / keywords. Useful for filtering and searches.

- `notes` (string, optional)
  - Free-text notes the user may add after reading (or after receiving via email).

- `read` (boolean, required, default: false)
  - Whether the user has marked the article as read. Defaults to `false` when created from scraping/email.

- `createdAt` (ISO 8601 timestamp string, required)
  - UTC timestamp when the article record was created in the system.

---

## Example JSON representation

Use the following structure as the canonical wire format and persisted JSON document schema for MongoDB or Couchbase. (Place example JSON in your implementation files or API docs for quick reference.)

---

## Suggested API endpoints (high level)

- `GET /articles` — list articles, support query params for `tag`, `author`, `q` (full-text), and paging (`page`, `size`).
- `GET /articles/{id}` — retrieve a single article by id.
- `POST /articles` — create a new article record (used by scrapers or manual save).
- `PATCH /articles/{id}` — update partial fields (e.g., mark `read: true`, add `notes`).
- `DELETE /articles/{id}` — remove a saved article.

Note: authentication and ownership checks should be handled by your user service (only return articles for the requesting user if multi-tenant).

---

## Indexing & query tips

- Index `title` and `tags` for fast filtering. Consider a text index for `title` + `notes` for full-text search.
- Store `authors` as an array to support queries like `authors: "Jane Doe"`.
- Use `createdAt` for sorting (newest first) and retention policies.

---

## Implementation notes

- For Couchbase: store as a document with a type field (e.g., `"type":"article"`) and include metadata bucket/indexing as needed.
- For MongoDB: store in a collection named `articles`. Use `_id` for `id` (or a separate `id` field if you prefer UUIDs).
- Validate `url` format and ensure `createdAt` is always stored in UTC.

---

## Next steps

1. Add this README file to your repo.
2. Use it as the source of truth when scaffolding your Spring Data entity/repository and API contracts.
3. When you are ready, I can produce a one-page API contract (OpenAPI/Swagger-style) based on this model (no code).

