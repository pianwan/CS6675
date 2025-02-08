package net.rm2.fileshare;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    // ENV
    public static final int PEER_SERVER_PORT = 5501;
    public static final int FILE_SERVER_PORT = 20000;
    public static final String SHARED_FOLDER = "shared_files/";
    public static final String DOWNLOAD_FOLDER = "download_files/";
    public static final String PEER_LIST = "connection";
    public static final String HOST_NAME = Objects.requireNonNullElse(System.getenv("HOST_NAME"), "localhost");
    public static final String RANDOM = Objects.requireNonNullElse(System.getenv("RANDOM"), "false");

    public static void main(String[] args) {
        if (HOST_NAME == null) {
            logger.error("Please set the HOST_NAME env before start");
            return;
        }
        logger.info("Starting Peer Server");
        new Thread(() -> new PeerServer().start()).start();
        logger.info("Starting File Server");
        new Thread(() -> new FileServer(SHARED_FOLDER).start()).start();

        Client client = new Client();
        Scanner sc = new Scanner(System.in);

        logger.info("Waiting for registering peers...");

        List<String> peers = new ArrayList<>();
        try {
            peers.addAll(Files.readAllLines(Path.of(PEER_LIST)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // register peers
        peers.forEach(client::registerPeers);


        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> logger.info("Current Peers: {}", PeerServer.getPeers()), 0, 10, TimeUnit.SECONDS);

        while (true) {
            logger.info("Input the file name you want to search:");
            String s = sc.next();
            boolean status = false;
            if (RANDOM.equalsIgnoreCase("true")) {
                status = client.searchFileRandomWalk(s);
            } else {
                status = client.searchFile(s);
            }
            if (status) {
                logger.info("Search success");
            } else {
                logger.info("Search failed");
            }
        }
    }
}