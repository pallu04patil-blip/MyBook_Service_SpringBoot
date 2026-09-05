package com.example.bookservice.model;

import jakarta.validation.constraints.NotBlank;

public class Author {

    private Long id;

    @NotBlank(message = "Author name is required")
    private String name;

    @NotBlank(message = "Nationality is required")
    private String nationality;

    public Author() {
        // required by Jackson
    }

    public Author(Long id, String name, String nationality) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }
}
