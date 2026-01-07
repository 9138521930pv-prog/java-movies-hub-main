package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MoviesStore {
    HashMap<Integer, List<Movie>> movies = new HashMap<>();
    private int seq = 1;

    public List<Movie> getMoviesByYear(int year) {
        return movies.values()
                .stream()
                .flatMap(List::stream)
                .filter(movie -> movie.getYear() == year)
                .toList();
    }

    public List<Movie> getAll() {
        return movies.values()
                .stream()
                .flatMap(List::stream)
                .toList();
    }

    public void add(Movie movie) {
        movie.setId(seq++);
        int year = movie.getYear();
        List<Movie> copyMovies = Optional
                .ofNullable(movies.get(year))
                .orElseGet(ArrayList::new);
        copyMovies.add(movie);
        movies.put(year, copyMovies);
    }

    public void clear() {
        movies.clear();
        seq = 1;
    }

    public void deleteById(int id) {
        for (Movie movie : getAll()) {
            if (movie.getId() == id) {
                int year = movie.getYear();
                List<Movie> copyMovies = movies.get(year);
                copyMovies.remove(movie);
                movies.put(year, copyMovies);
            }
        }
    }

    public Movie getMovieById(int id) {
        return movies.values()
                .stream()
                .flatMap(List::stream)
                .filter(movie -> movie.getId() == id)
                .findFirst()
                .orElse(null);
    }


}