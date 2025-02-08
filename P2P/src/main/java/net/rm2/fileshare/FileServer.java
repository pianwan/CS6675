package net.rm2.fileshare;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

public class FileServer {
    private static final Logger logger = LogManager.getLogger(FileServer.class);
    private HashMap<String, Path> fileMap = new HashMap<>();

    public FileServer(String path) {
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths.filter(Files::isRegularFile).forEach(p -> fileMap.put(p.getFileName().toString(), p));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(Main.FILE_SERVER_PORT)) {
            logger.info("File Server on " + Main.FILE_SERVER_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new FileHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class FileHandler implements Runnable {
        private Socket socket;

        public FileHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                 OutputStream outputStream = socket.getOutputStream()) {

                String request = reader.readLine();
                if (request.startsWith("GET")) {
                    String fileName = request.substring(request.indexOf(" ") + 1);
                    File file = null;
                    for (String name : fileMap.keySet()) {
                        if (name.toLowerCase().contains(fileName.toLowerCase())) {
                            file = fileMap.get(name).toFile();
                            break;
                        }
                    }

                    if (file == null) {
                        writer.println("NOT_FOUND");
                        return;
                    }

                    writer.println("FOUND");
                    writer.println(file.getName());

                    byte[] buffer = new byte[4096];
                    try (FileInputStream fis = new FileInputStream(file)) {
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    logger.info("File: {} sent successfully", fileName);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
