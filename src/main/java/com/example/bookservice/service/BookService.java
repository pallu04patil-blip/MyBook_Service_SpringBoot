package com.example.bookservice.service;

import com.example.bookservice.exception.BookNotFoundException;
import com.example.bookservice.model.Book;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BookService {

    // In-memory "database" — thread-safe map keyed by book id.
    // Good enough for a demo/viva; swap for a JPA repository later
    // without touching the controller.
    private final Map<Long, Book> catalog = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public BookService() {
        // seed a few books so search/browse isn't empty on first run
        addBook(new Book(null, "Clean Code", "Robert C. Martin", "Programming", true));
        addBook(new Book(null, "The Hobbit", "J. R. R. Tolkien", "Fantasy", true));
        addBook(new Book(null, "1984", "George Orwell", "Dystopian", true));
    }

    public Book addBook(Book book) {
        long id = idCounter.getAndIncrement();
        book.setId(id);
        book.setAvailable(true); // every newly added book starts on the shelf
        catalog.put(id, book);
        return book;
    }

    // Search by title OR author, case-insensitive, partial match.
    // No query -> returns the whole catalog.
    public List<Book> searchBooks(String query) {
        if (query == null || query.isBlank()) {
            return List.copyOf(catalog.values());
        }
        String needle = query.toLowerCase();
        return catalog.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(needle)
                        || b.getAuthor().toLowerCase().contains(needle))
                .toList();
    }

    public Book getBookById(Long id) {
        Book book = catalog.get(id);
        if (book == null) {
            throw new BookNotFoundException(id);
        }
        return book;
    }

    // Used by LendingService — marks a book borrowed. Throws if it's already out.
    public Book borrowBook(Long id) {
        Book book = getBookById(id);
        if (!book.isAvailable()) {
            throw new IllegalStateException("Book with id " + id + " is already borrowed");
        }
        book.setAvailable(false);
        return book;
    }

    // Used by LendingService — marks a book returned. Throws if it wasn't out.
    public Book returnBook(Long id) {
        Book book = getBookById(id);
        if (book.isAvailable()) {
            throw new IllegalStateException("Book with id " + id + " was not borrowed");
        }
        book.setAvailable(true);
        return book;
    }
}
