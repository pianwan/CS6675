package net.rm2.fileshare;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {
    private static final Logger logger = LogManager.getLogger(Client.class);

    public void registerPeers(String hostName) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        AtomicInteger ret = new AtomicInteger();
        scheduler.scheduleAtFixedRate(() -> {
            if (ret.get() > 5) {
                scheduler.shutdown();
            }
            if (connectPeer(hostName)) {
                ret.getAndIncrement();
                logger.info("Connected successfully to {}", hostName);
                PeerServer.getPeers().add(hostName);
                scheduler.shutdown();
            } else {
                logger.warn("Failed to connect, retrying in 5s...");
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public boolean searchFile(String name) {
        // Search local
        long cur = System.currentTimeMillis();
        if (requestFile(Main.HOST_NAME, name)) {
            logger.info("Find {} in local", name);
            return true;
        }
        // Search others
        Set<String> discoveredPeers = PeerServer.getPeers();
        HashSet<String> visitedPeers = new HashSet<>();
        LinkedList<String> queue = new LinkedList<>();
        queue.addAll(discoveredPeers);
        visitedPeers.add(Main.HOST_NAME);

        int queryAmount = 0;
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String current = queue.poll();
                if (!visitedPeers.contains(current)) {
                    queryAmount++;
                    // request file
                    boolean status = requestFile(current, name);
                    // successfully downloaded stop
                    if (status) {
                        logger.info("Successfully Query File in {} ms", System.currentTimeMillis() - cur);
                        logger.info("Queried {} times", queryAmount);
                        return true;
                    }

                    // next bfs
                    List<String> nexts = listPeers(current);
                    queue.addAll(nexts);
                    visitedPeers.add(current);
                }
            }
        }
        logger.info("Failed Query File in {} ms", System.currentTimeMillis() - cur);
        logger.info("Queried {} times", queryAmount);
        return false;
    }


    public boolean searchFileRandomWalk(String name) {
        int MAX_HOPS = 3;
        long cur = System.currentTimeMillis();

        if (requestFile(Main.HOST_NAME, name)) {
            logger.info("Find {} in local", name);
            return true;
        }

        Set<String> discoveredPeers = PeerServer.getPeers();
        HashSet<String> visitedPeers = new HashSet<>();
        visitedPeers.add(Main.HOST_NAME);

        int queryAmount = 0;
        int hops = 0;
        String currentPeer = getRandomPeer(discoveredPeers, visitedPeers);

        while (currentPeer != null && hops < MAX_HOPS) {
            queryAmount++;
            visitedPeers.add(currentPeer);

            if (requestFile(currentPeer, name)) {
                logger.info("Successfully Query File in {} ms", System.currentTimeMillis() - cur);
                logger.info("Queried {} times", queryAmount);
                return true;
            }

            List<String> neighbors = listPeers(currentPeer);
            currentPeer = getRandomPeer(neighbors, visitedPeers);
            hops++;
        }

        logger.info("Failed Query File in {} ms", System.currentTimeMillis() - cur);
        logger.info("Queried {} times", queryAmount);
        return false;
    }

    private String getRandomPeer(Collection<String> peers, Set<String> visited) {
        List<String> availablePeers = new ArrayList<>();
        for (String peer : peers) {
            if (!visited.contains(peer)) {
                availablePeers.add(peer);
            }
        }

        if (availablePeers.isEmpty()) {
            return null;
        }

        Random random = new Random();
        return availablePeers.get(random.nextInt(availablePeers.size()));
    }

    private List<String> listPeers(String peerHostName) {
        List<String> neighbors = new ArrayList<>();
        try (Socket socket = new Socket(peerHostName, Main.PEER_SERVER_PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            writer.println("LIST");
            logger.info("Req Peer List");
            String peers = reader.readLine();
            logger.info("Peers: {}", peers);
            neighbors.addAll(Arrays.stream(peers.split(",")).toList());
        } catch (IOException e) {
            logger.error("Req Peer Failed", e);
        }
        return neighbors;
    }

    private boolean connectPeer(String peerHostName) {
        try (Socket socket = new Socket(peerHostName, Main.PEER_SERVER_PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            writer.println("REGISTER" + " " + Main.HOST_NAME);
            logger.info("Registering peers");
            String response = reader.readLine();
            if (response.equalsIgnoreCase("REGISTERED")) {
                logger.info("Successfully connected to peer: {}", peerHostName);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            logger.error("Request Peer Connection Failed", e);
        }
        return false;
    }

    private boolean requestFile(String peerHostName, String searchName) {
        try (Socket socket = new Socket(peerHostName, Main.FILE_SERVER_PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {

            writer.println("GET " + searchName);
            logger.info("File Searching {} Request to {} port {}", searchName, peerHostName, Main.FILE_SERVER_PORT);

            String response = reader.readLine();
            if (response.equals("NOT_FOUND")) {
                // not found
                logger.warn("File not find: {} in host {}", searchName, peerHostName);
                return false;
            } else if (response.equals("FOUND")) {
                // found
                String filename = reader.readLine();

                byte[] buffer = new byte[4096];
                InputStream inputStream = socket.getInputStream();
                int bytesRead;

                File downloadDir = new File(Main.DOWNLOAD_FOLDER);
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs();
                }

                try (FileOutputStream fos = new FileOutputStream(Main.DOWNLOAD_FOLDER + filename)) {
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    logger.info("File downloaded successfully: {}", filename);
                    return true;
                } catch (IOException e) {
                    logger.error("File write failed", e);
                    return false;
                }
            }
        } catch (IOException e) {
            logger.error("File Request failed", e);
        }
        return false;
    }

}
