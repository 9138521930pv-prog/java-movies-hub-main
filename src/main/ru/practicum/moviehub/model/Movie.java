package ru.practicum.moviehub.model;

public class Movie {
    private long id;
    private String title;
    private int year;


    public Movie(String title, int year) {
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

    public void setId(int id) {
        this.id = id;
    }
}