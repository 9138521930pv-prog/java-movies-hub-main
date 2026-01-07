package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MoviesStore {
    private final HashMap<Integer, Movie> movies = new HashMap<>();
    private int seq = 1;

    public List<Movie> getMoviesByYear(int year) {
        return movies.values()
                .stream()
                .filter(movie -> movie.getYear() == year)
                .toList();
    }

    public List<Movie> getAll() {
        return movies.values()
                .stream()
                .toList();
    }

    public void add(Movie movie) {
        movie.setId(seq++);
        movies.put(movie.getId(), movie);
    }

    public void clear() {
        movies.clear();
        seq = 1;
    }

    public void deleteById(int id) {
        movies.remove(id);
    }

    public Movie getMovieById(int id) {
        return movies.get(id);
    }


}