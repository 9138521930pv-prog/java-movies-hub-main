package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.model.MovieRequest;
import ru.practicum.moviehub.store.MoviesStore;

class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore store;
    private static final Gson gson = new Gson();

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();
        if (query != null && query.startsWith("year=")) {
            handleFilter(ex, query.substring(5));
        } else {
            String[] pathElements = path.split("/");
            boolean hasId = pathElements.length > 2 && !pathElements[2].isBlank();
            String id = hasId ? pathElements[2] : null;
            switch (ex.getRequestMethod()) {
                case "GET":
                    if (hasId) {
                        handleGetById(ex, id);
                    } else {
                        handleGetAll(ex);
                    }
                    break;
                case "POST":
                    handlePost(ex);
                    break;
                case "DELETE":
                    handleDeleteById(ex, id);
                    break;
                default:
                    sendStatusResponse(ex, 405);
            }
        }
    }

    private void handlePost(HttpExchange ex) throws IOException {
        String contentType = ex.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.equals("application/json; charset=UTF-8")) {
            sendStatusResponse(ex, 415);
            return;
        }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        if (body.isBlank()) {
            sendJsonResponse(ex, 400,
                    new ErrorResponse("Ошибка валидации", List.of("Тело запроса не может быть пустым")));
            return;
        }

        MovieRequest req;
        try {
            req = gson.fromJson(body, MovieRequest.class);
        } catch (JsonSyntaxException e) {
            sendJsonResponse(ex, 400,
                    new ErrorResponse("Ошибка валидации", List.of("Неверный формат JSON")));
            return;
        }

        List<String> errors = validate(req);
        if (!errors.isEmpty()) {
            ErrorResponse err = new ErrorResponse("Ошибка валидации", errors);
            sendJsonResponse(ex, 422, gson.toJson(err));
            return;
        }

        Movie movie = new Movie(req.title, req.year);
        store.add(movie);
        sendJsonResponse(ex, 201, gson.toJson(movie));
    }

    private List<String> validate(MovieRequest req) {
        List<String> errors = new ArrayList<>();

        if (req.title == null || req.title.isBlank()) {
            errors.add("название не должно быть пустым");
        } else if (req.title.length() > 100) {
            errors.add("название не должно превышать 100 символов");
        }

        int maxYear = Year.now().getValue() + 1;
        if (req.year < 1888 || req.year > maxYear) {
            errors.add("год должен быть между 1888 и " + maxYear);
        }

        return errors;
    }

    public void handleGetById(HttpExchange ex, String id) throws IOException {
        int movieId;
        try {
            movieId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            sendJsonResponse(ex, 400,
                    new ErrorResponse("Ошибка валидации", List.of("ID фильма должен быть числом")));
            return;
        }

        Movie movie = store.getMovieById(movieId);
        if (movie == null) {
            sendJsonResponse(ex, 404, new ErrorResponse("Ошибка валидации", List.of("Фильм не найден")));
            return;
        }
        sendJsonResponse(ex, 200, movie);
    }

    public void handleGetAll(HttpExchange ex) throws IOException {
        List<Movie> movies = store.getAll();
        sendJsonResponse(ex, 200, movies);
    }

    public void handleDeleteById(HttpExchange ex, String id) throws IOException {
        int movieId;
        try {
            movieId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            sendJsonResponse(ex, 400,
                    new ErrorResponse("Ошибка валидации", List.of("ID фильма должен быть числом")));
            return;
        }

        Movie movie = store.getMovieById(movieId);
        if (movie == null) {
            sendJsonResponse(ex, 404,
                    new ErrorResponse("Ошибка валидации", List.of("Фильм не найден")));
            return;
        }
        store.deleteById(movieId);
        sendNoContentResponse(ex);
    }

    public void handleFilter(HttpExchange ex, String year) throws IOException {
        try {
            int yearMovies = Integer.parseInt(year);
            List<Movie> listMovies = store.getMoviesByYear(yearMovies);

            if (listMovies == null) {
                listMovies = List.of();
            }
            sendJsonResponse(ex, 200, listMovies);
        } catch (NumberFormatException e) {
            sendJsonResponse(ex, 400,
                    new ErrorResponse("Ошибка валидации", List.of("Год фильма должен быть числом")));
        }
    }
}