package ru.practicum.moviehub.model;

import static ru.practicum.moviehub.http.MoviesServer.newId;

public class Movie {
    private long id;
    private String title;
    private int year;


    public Movie(String title, int year) {
        this.id = newId.incrementAndGet();
        this.title = title;
        this.year = year;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

}