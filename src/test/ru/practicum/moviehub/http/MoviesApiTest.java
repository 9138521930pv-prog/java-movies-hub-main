package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.model.MovieRequest;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {

    private static final String srvAddress = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore store = new MoviesStore();
    private static Charset charset = StandardCharsets.UTF_8;
    private static String contentType = "application/json; charset=UTF-8";
    private static int filmYyear = 2025;
    private static String filmName = "Новый фильм";

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer(store, 8080);
        server.start();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @BeforeEach
    void beforeEach() {
        store.clear();
        server.CiearID();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies"))
                .GET()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(charset));
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        String body = resp.body().trim();
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");
        assertEquals(contentType, contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
        assertEquals("[]", resp.body().trim());
    }

    @Test
    void getMovies_afterMovieIsCreated_ReturnsMovieInArray() throws Exception {
        Gson gson = new Gson();
        MovieRequest body = new MovieRequest(filmName, filmYyear);
        String json = gson.toJson(body);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies"))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(json, charset))
                .build();

        HttpResponse<String> postResponse =
                client.send(postRequest, HttpResponse.BodyHandlers.ofString(charset));

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonArray().get(0).getAsJsonObject();
        String title = jsonObject.get("title").getAsString();
        int year = jsonObject.get("year").getAsInt();
        assertEquals(201, postResponse.statusCode());
        assertTrue(title.equals(body.title));
        assertTrue(year == body.year);
    }

    @Test
    void getMovieById_afterMovieIsCreated_thenReturnsMovieObject() throws Exception {
        Gson gson = new Gson();
        MovieRequest body = new MovieRequest(filmName, filmYyear);

        String json = gson.toJson(body);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies"))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(json, charset))
                .build();

        client.send(postRequest, HttpResponse.BodyHandlers.ofString(charset));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies/1"))
                .GET()
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString(charset));

        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals(contentType, contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        String title = jsonObject.get("title").getAsString();
        int year = jsonObject.get("year").getAsInt();
        int respId = jsonObject.get("id").getAsInt();

        Assertions.assertTrue(title.equals(body.title));
        Assertions.assertTrue(year == body.year);
        Assertions.assertTrue(respId == 1);
        assertEquals(200, response.statusCode(), "GET /movies должен вернуть 200");
    }

    @Test
    void deleteMovieById_afterMovieIsCreated_thenReturns204() throws Exception {
        Gson gson = new Gson();
        MovieRequest body = new MovieRequest(filmName, filmYyear);

        String json = gson.toJson(body);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies"))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(json, charset))
                .build();
        HttpResponse<String> response1 =
        client.send(postRequest, HttpResponse.BodyHandlers.ofString(charset));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies/1"))
                .DELETE()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString(charset));

        assertEquals(204, response.statusCode(), "DELETE /movies должен вернуть 204");
    }

    @Test
    void getFilteredByYear_thenReturnsMoviesWithThatYear() throws Exception {
        Gson gson = new Gson();
        MovieRequest body = new MovieRequest(filmName, filmYyear);

        String json = gson.toJson(body);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies"))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(json, charset))
                .build();
        client.send(postRequest, HttpResponse.BodyHandlers.ofString(charset));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies?year=" + filmYyear))
                .GET()
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString(charset));
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");
        JsonElement jsonElement = JsonParser.parseString(response.body());
        String moviesStr = jsonElement.getAsJsonArray().toString();
        List<Movie> moviesList = gson.fromJson(moviesStr, new ListOfMoviesTypeToken().getType());

        assertEquals(contentType, contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(200, response.statusCode(), "200");
        System.out.println("Список фильмов: ");
        for (Movie movie : moviesList) {
            System.out.println(movie.getTitle());
        }
    }

}