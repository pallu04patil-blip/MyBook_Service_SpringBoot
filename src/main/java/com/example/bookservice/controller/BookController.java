package com.example.bookservice.controller;

import com.example.bookservice.model.Book;
import com.example.bookservice.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    

    // POST /api/books -> Add a new book
    @PostMapping
    public ResponseEntity<Book> addBook(@Valid @RequestBody Book book) {
        Book saved = bookService.addBook(book);
        return new ResponseEntity<>(saved, HttpStatus.CREATED); // 201
    }

    // GET /api/books?query=tolkien -> Search catalog or view all books
    @GetMapping
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(bookService.searchBooks(query));
    }

    // GET /api/books/{id} -> View a single book by ID
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }
}