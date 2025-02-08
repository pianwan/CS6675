package net.rm2.fileshare;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PeerServer {
    private static final Logger logger = LogManager.getLogger(PeerServer.class);
    @Getter
    private static Set<String> peers = ConcurrentHashMap.newKeySet();

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(Main.PEER_SERVER_PORT)) {
            logger.info("Peer Server listen on {}", Main.PEER_SERVER_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new PeerHandler(socket)).start();
            }
        } catch (IOException e) {
            logger.error("Error", e);
        }
    }


    private static class PeerHandler implements Runnable {
        private Socket socket;

        public PeerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                String request = reader.readLine();
                if (request.startsWith("REGISTER")) {
                    String peerAddress = request.split(" ")[1];
                    peers.add(peerAddress);
                    writer.println("REGISTERED");
                } else if (request.equals("LIST")) {
                    writer.println(String.join(",", peers));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
