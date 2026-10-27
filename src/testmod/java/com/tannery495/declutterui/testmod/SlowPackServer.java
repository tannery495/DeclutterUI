package com.tannery495.declutterui.testmod;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Tiny local HTTP server for /dtest pack. It trickles out a 3 MB file over about three seconds, so the
 * client shows its download progress toast; the pushed hash never matches, so the download then fails.
 */
final class SlowPackServer {

    private static final int PORT = 25580;
    private static final int SENT_BYTES = 3 * 1024 * 1024;
    private static final int CHUNK = 64 * 1024;
    private static HttpServer server;

    private SlowPackServer() {}

    static synchronized String url() {
        if (server == null) {
            try {
                server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), PORT), 0);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            server.createContext("/pack.zip", exchange -> {
                exchange.getResponseHeaders().add("Content-Type", "application/zip");
                exchange.sendResponseHeaders(200, SENT_BYTES);
                byte[] chunk = new byte[CHUNK];
                try (OutputStream body = exchange.getResponseBody()) {
                    for (int sent = 0; sent < SENT_BYTES; sent += CHUNK) {
                        body.write(chunk);
                        body.flush();
                        Thread.sleep(60);
                    }
                } catch (IOException | InterruptedException ignored) {
                    // The client gave up on the download; nothing to clean up.
                }
                exchange.close();
            });
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
        }
        return "http://127.0.0.1:" + PORT + "/pack.zip";
    }
}
