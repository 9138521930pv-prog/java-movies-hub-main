package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected final Gson gson = new Gson();

    protected void sendJsonResponse(HttpExchange ex, int statusCode, Object obj) throws IOException {
        String json = gson.toJson(obj);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendNoContentResponse(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Content-Type","application/json; charset=UTF-8");
        ex.sendResponseHeaders(204, -1);
    }

    protected void sendStatusResponse(HttpExchange ex, int status) throws IOException {
        ex.sendResponseHeaders(status, -1);
    }
}
