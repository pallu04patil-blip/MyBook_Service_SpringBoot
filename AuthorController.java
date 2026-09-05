package com.example.bookservice.controller;

import com.example.bookservice.model.Author;
import com.example.bookservice.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    // POST /api/authors -> add an author
    @PostMapping
    public ResponseEntity<Author> addAuthor(@Valid @RequestBody Author author) {
        Author saved = authorService.addAuthor(author);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // GET /api/authors -> view all authors
    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors() {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

    // GET /api/authors/{id} -> view one author
    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Long id) {
        return ResponseEntity.ok(authorService.getAuthorById(id));
    }
}
