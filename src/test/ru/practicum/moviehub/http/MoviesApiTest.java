package ru.practicum.moviehub.http;

import com.google.gson.*;
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
    private final Gson gson = new Gson();

    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore store = new MoviesStore();
    private static final Charset CHAR_SET = StandardCharsets.UTF_8;
    private static final String CONTENT_TYPE = "application/json; charset=UTF-8";
    private static final int FILE_YEAR = 2025;
    private static final String FILE_NAME = "Новый фильм";

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
                client.send(req, HttpResponse.BodyHandlers.ofString(CHAR_SET));
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        String body = resp.body().trim();
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");
        assertEquals(CONTENT_TYPE, contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        JsonElement json = JsonParser.parseString(body);
        assertTrue(json.isJsonArray(),"Ожидается JSON-массив");
        JsonArray jArray = json.getAsJsonArray();
        assertEquals(0, jArray.size(),"Ожидается пустой JSON-массив");
    }

    @Test
    void getMovies_afterMovieIsCreated_ReturnsMovieInArray() throws Exception {
        MovieRequest body = new MovieRequest(FILE_NAME, FILE_YEAR);
        String json = gson.toJson(body);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies"))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(json, CHAR_SET))
                .build();

        HttpResponse<String> postResponse =
                client.send(postRequest, HttpResponse.BodyHandlers.ofString(CHAR_SET));

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
        MovieRequest body = new MovieRequest(FILE_NAME, FILE_YEAR);

        String json = gson.toJson(body);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies"))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(json, CHAR_SET))
                .build();

        client.send(postRequest, HttpResponse.BodyHandlers.ofString(CHAR_SET));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies/1"))
                .GET()
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString(CHAR_SET));

        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals(CONTENT_TYPE, contentTypeHeaderValue,
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
        MovieRequest body = new MovieRequest(FILE_NAME, FILE_YEAR);

        String json = gson.toJson(body);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies"))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(json, CHAR_SET))
                .build();
        HttpResponse<String> response1 =
                client.send(postRequest, HttpResponse.BodyHandlers.ofString(CHAR_SET));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies/1"))
                .DELETE()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString(CHAR_SET));

        assertEquals(204, response.statusCode(), "DELETE /movies должен вернуть 204");
    }

    @Test
    void getFilteredByYear_thenReturnsMoviesWithThatYear() throws Exception {
        Gson gson = new Gson();
        MovieRequest body = new MovieRequest(FILE_NAME, FILE_YEAR);

        String json = gson.toJson(body);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies"))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(json, CHAR_SET))
                .build();
        client.send(postRequest, HttpResponse.BodyHandlers.ofString(CHAR_SET));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(srvAddress + "/movies?year=" + FILE_YEAR))
                .GET()
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString(CHAR_SET));
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");
        JsonElement jsonElement = JsonParser.parseString(response.body());
        String moviesStr = jsonElement.getAsJsonArray().toString();
        List<Movie> moviesList = gson.fromJson(moviesStr, new ListOfMoviesTypeToken().getType());

        assertEquals(CONTENT_TYPE, contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(200, response.statusCode(), "200");
        System.out.println("Список фильмов: ");
        for (Movie movie : moviesList) {
            System.out.println(movie.getTitle());
        }
    }

}