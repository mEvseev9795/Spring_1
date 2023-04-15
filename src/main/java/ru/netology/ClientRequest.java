package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static ru.netology.Main.validPaths;


public class ClientRequest {

    private String resourcePath;
    private final Client client;
    private boolean endOfConnect = false;
    private String[] parts = null;

    public ClientRequest(Client client) {
        this.client = client;
    }

    // read only request line for simplicity
    // must present in form GET /path HTTP/1.1.
    public boolean prepareRequest(String requestLine) throws IOException {
        parts = requestLine.split(" ");
        if (this.parts.length != 3) return false;
        BufferedOutputStream out = this.client.getClientOut();

        final var path = this.parts[1];
        if (path.equals("/end")) {
            endOfConnect = true;
            return true;
        } else {
            if (!validPaths.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return false;
            } else {
                resourcePath = path;
                return true;
            }
        }
    }

    public boolean requestToDo() throws IOException {

        final var filePath = Path.of(".", "public", resourcePath);
        final var mimeType = Files.probeContentType(filePath);
        BufferedOutputStream out = this.client.getClientOut();

        if (endOfConnect) return true;

        // special case for classic
        if (resourcePath.equals("/classic.html")) {
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
        } else {
            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        }
        return false;
    }
}
