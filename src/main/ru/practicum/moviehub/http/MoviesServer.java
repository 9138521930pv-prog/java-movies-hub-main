package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

public class MoviesServer {
    private final HttpServer server;
    public static final AtomicLong newId = new AtomicLong();
    public MoviesServer(MoviesStore store, int PORT) {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);

            server.createContext("/movies", new MoviesHandler(store));

        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать HTTP-сервер", e);
        }
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен");
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен");
    }

    public void CiearID() {
        newId.set(0);
    }
}