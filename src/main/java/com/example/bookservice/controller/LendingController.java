package com.example.bookservice.controller;

import com.example.bookservice.model.Book;
import com.example.bookservice.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// A third "service" in the REST sense (its own resource path, /api/lending),
// but it has no storage of its own — it composes BookService so the
// availability flag stays in exactly one place instead of getting
// duplicated across two data sources.
@RestController
@RequestMapping("/api/lending")
public class LendingController {

    private final BookService bookService;

    public LendingController(BookService bookService) {
        this.bookService = bookService;
    }

    // PUT /api/lending/{id}/borrow
    // 409 (via IllegalStateException) if the book is already checked out.
    @PutMapping("/{id}/borrow")
    public ResponseEntity<Book> borrowBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.borrowBook(id));
    }

    // PUT /api/lending/{id}/return
    // 409 (via IllegalStateException) if the book wasn't borrowed.
    @PutMapping("/{id}/return")
    public ResponseEntity<Book> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.returnBook(id));
    }

    // GET /api/lending/available -> books currently on the shelf
    @GetMapping("/available")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        return ResponseEntity.ok(
                bookService.searchBooks(null).stream().filter(Book::isAvailable).toList()
        );
    }
}
