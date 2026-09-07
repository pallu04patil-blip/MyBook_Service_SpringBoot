package com.example.bookservice.service;

import com.example.bookservice.exception.AuthorNotFoundException;
import com.example.bookservice.model.Author;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AuthorService {

    private final Map<Long, Author> authors = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public AuthorService() {
        // seed a couple so GET /api/authors isn't empty on first run
        addAuthor(new Author(null, "Robert C. Martin", "American"));
        addAuthor(new Author(null, "J. R. R. Tolkien", "British"));
    }

    public Author addAuthor(Author author) {
        long id = idCounter.getAndIncrement();
        author.setId(id);
        authors.put(id, author);
        return author;
    }

    public List<Author> getAllAuthors() {
        return List.copyOf(authors.values());
    }

    public Author getAuthorById(Long id) {
        Author author = authors.get(id);
        if (author == null) {
            throw new AuthorNotFoundException(id);
        }
        return author;
    }
}
