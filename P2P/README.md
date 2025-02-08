# Problem 1. Hand-on Experience with Unstructured Peer to Peer System

Pian Wan, pianwan@gatech.edu

## Introduction
In this homework, I select `Option (1)` Once you complete the setup of your toy P2P search system with minimum of 5 peers. You are asked to install 5 song files or text files at each peer. Hint: search keywords should be used to name your files. Then perform two types of measurement for your toy P2P search system.

**I build a P2P file sharing system from scratch, which is "You may also create a P2P application of your own" in the homework description.**

In the experiment, each peer shares PDF files downloaded from Wikipedia (https://en.wikipedia.org/). Some files were randomly selected, while others were manually chosen.
The system supports file searching and downloading, and performance is measured in terms of latency and throughput.

One container is equal to a peer in the p2p system. I use Docker Swarm to build network across peers.

## Build/Run

### Requirements
- Docker Engine
- Docker Compose & Docker Swarm
- maven
- jdk21

### Build
Before running the container, you have to build the p2p file sharing system first.
```bash
mvn clean package
```
The compiled JAR file `P2P-1.0-SNAPSHOT.jar` will be located in `./target`.

With this file, you can build the docker image with:

```bash
docker build .
```

With the image, you can start the p2p file sharing environment with
```bash
docker compose up
```

For simplify, you can also use:
```bash
docker compose up --build
```

- [DockerFile](./Dockerfile)
- [docker-compose.yml](./docker-compose.yml)

## Unstructured P2P 

### Single Node
![p2p](./docs/single.png)

For a single node, there are mainly 3 components.

First, a P2P Server [PeerServer.java](./src/main/java/net/rm2/fileshare/PeerServer.java).
This is used for maintain the graph structure of self-organizing overlay network. Every P2P Server stores the address and ports for the nearby P2P Server.
For a new node to join a P2P Network, it needs to create a TCP connection to at least one of the P2P Server in the network and then send a message "REGISTER <peerAddress>".
Every one can query the nearby nodes of any node in the network. To query nearby nodes, send "LIST" to the P2P Server.

Second, a File Server [FileServer.java](./src/main/java/net/rm2/fileshare/FileServer.java). This server is used for handling file download request.
If current node own the file needed, it will return the requested file otherwise return NOT_FOUND.
There is a volume along with this File Server. In the homework, we store 5 files for each peer.

Third, a Client [Client.java](./src/main/java/net/rm2/fileshare/Client.java). This is a user interface.
Users can use the client to interact with the node (e.g. searching, connecting to other nodes, etc).
To search a file, call `searchFile(String name)` method. You can input any sentence you want to search in the command line interface since the client is built with the jar.


### Overlay Network
![p2p](./docs/5peers.png)

This is an example overlay for the testing, you can choose any overlay you want. This overlay can be easily modified, to modify peer1, go `./p1/connection`

In order to make sure the connectivity of the network, every node in the overlay needs to know at least one other node (i.e. hostname and port).

In the image, P1 knows peer2. P2 knows peer3 and peer5. P3 knows peer1. P4 knows peer3 and peer5. P5 knows peer2.

- Peer 1 knows Peer 2
- Peer 2 knows Peer 3, Peer 5
- Peer 3 knows Peer 1
- Peer 4 knows Peer 3, Peer 5
- Peer 5 knows Peer 2

Each peer maintains connections in its configuration file (./pX/connection for peer X).

### Query
Two methods of querying are implemented.

- Flooding (default): We query all neighbours. This is efficient in a small p2p network.

- RandomWalk: We query random neighbour. This reduces workload in a large p2p network.

To use RandomWalk, the environment variable RANDOM should be true.

## Experiment Details

### (1) Baseline measurement: you are asked to measure the performance of your keyword query requests in terms of the latency (average time needed per query) and throughput (#queries served per time unit).
The response time is printed to compute latency and throughput.
For example, to search the keyword `UFC`, we will have the following:
![base](./docs/s0.png)

- Latency: 31ms
- Throughput: 1 / 31ms = 32.26 queries/sec

So, the latency is `31ms`, the throughput is `32.26/s` which is 1/latency.


### (2) You are required to use one of the following three types of workload setups: (2.2) varying the number of queries from 10, 20, 40, but with a fixed number of files shared among the 5 peers, say 5 or 10. Compare the latency and throughput performance of these three top P2P systems.

In (2), I do (2.2) as my choice.
The sentences used for query are at [./query.txt](query.txt). Our p2p system has 5 peers. The number and structure are fixed during the experiments.

For 10, use the 10 beginning sentences. The results are:
```text
2025-02-08 00:16:54 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 40 ms
2025-02-08 00:16:54 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 12 ms
2025-02-08 00:16:54 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 15 ms
2025-02-08 00:16:54 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 13 ms
2025-02-08 00:16:54 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 8 ms
2025-02-08 00:16:54 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 12 ms
2025-02-08 00:16:54 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 8 ms
2025-02-08 00:16:54 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:16:54 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 6 ms
2025-02-08 00:16:54 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 3 ms
```

Total 122ms, Throughput 8.19/s

latency: 12.2ms

throughput: 82/s


For 20, use the 20 beginning sentences. The results are:
```text
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 32 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 13 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 21 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 7 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 7 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 6 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 6 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 6 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 3 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 2 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 10 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 8 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 7 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 4 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 14 ms
2025-02-08 00:19:01 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 12 ms
```

Total 185ms, Throughput 5.40/s

latency: 9.2ms

throughput: 108/s

For 40, use the 40 beginning sentences. The results are:

```text
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 28 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 6 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 38 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 4 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 3 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 2 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 10 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 4 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 12 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 11 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 2 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 6 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 2 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 3 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:22:42 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
```

Total time 264ms, Throughput 3.79/s

latency: 6.6ms

throughput: 113.7/s

To summary, the latency and throughput for single query are almost the same while varying the number of queries from 10, 20, 40.
The bottleneck of latency is the workload, i.e. the size of the file.

## Deliverable

### URL of the P2P source code downloaded.
This repository. https://github.com/pianwan/CS6675

### Screen shots of your P2P Command lines or GUIs, showing the membership, the query and the routing functionality (operations) of this toy P2P system.
![screen shots](./docs/sc1.png)
From the screenshots, peer1's current neighbors are peer2 and peer3. peer2's current neighbours are peer1, peer3 and peer5.
peer3's current neighbors are peer1, peer2 and peer4. peer4's current neighbors are peer3 and peer5.
peer5's current neighbors are peer2 and peer4.

The routing operations are defined in the `connection` file. See peer1's connection file at [./p1/connection](./p1/connection).

![screen shots](./docs/sc2.png)
To attach into the container, use `docker attach peer1`. Then you can see the Command line interface for users.
To search a file which contains 'ETH' in the filename, input `ETH`.

### Measurement comparison in terms of throughput and latency of two routing protocols of this toy P2P system: the default routing protocol and your proposed routing protocol.

For flooding, see (2) in the experiment details.
For RandomWalk,

```text
2025-02-08 00:50:50 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 38 ms
2025-02-08 00:50:50 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 12 ms
2025-02-08 00:50:50 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 18 ms
2025-02-08 00:50:50 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 13 ms
2025-02-08 00:50:50 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 6 ms
2025-02-08 00:50:50 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 18 ms
2025-02-08 00:50:50 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 13 ms
2025-02-08 00:50:50 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 7 ms
2025-02-08 00:50:50 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:50:50 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 6 ms
```
Total time 136ms, Throughput 7.35294/s

latency: 13.6ms

throughput: 73.53/s

```text
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 30 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 10 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 13 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 8 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 4 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 7 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 6 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 6 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 15 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 12 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 8 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 6 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:51:03 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 13 ms
```
Total time 181ms, Throughput 5.52486/s

latency: 9.1ms

throughput: 110.5/s
```text
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 26 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 11 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 14 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 8 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 9 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 6 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 6 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 6 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 9 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 18 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 8 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 3 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 10 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 9 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 3 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 3 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 3 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 5 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 3 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 3 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 3 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 5 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Successfully Query File in 3 ms
2025-02-08 00:51:13 [main] INFO  net.rm2.fileshare.Client - Failed Query File in 4 ms
```
Total time 253ms, Throughput 3.95/s

latency: 6.3ms

throughput: 158.10/s


Comparison

|                         | Flooding | RandomWalk |
|:------------------------|---------:|:----------:|
| latency (10 queries)    |   12.2ms |   13.6ms   |
| throughput (10 queries) |     82/s |  73.53/s   |
| latency (20 queries)    |    9.2ms |   9.1ms    |
| throughput (20 queries) |    108/s |  110.5/s   |
| latency (40 queries)    |    6.6ms |   6.3ms    |
| throughput (40 queries) |  113.7/s |  158.10/s  |

### Discuss latency and throughput of your toy P2P system under the two alternative routing protocols in terms of pros and cons.
**Flooding** broadcasts queries to all neighbors. It is  an efficient approach for small networks where fast query resolution is required.
This method ensures a high success rate, as the file request reaches multiple peers simultaneously. However, the major drawback is its high network traffic, which results in redundant messages and excessive bandwidth consumption.
As the network grows, flooding becomes unsustainable due to its exponential increase in message transmissions, making it poorly scalable for large networks.

Pros:
- Fast in small networks.
- High success rate.

Cons:
- High network traffic.
- Not scalable (exponential growth).

**Random Walk** queries a random neighbor instead of broadcasting. This significantly reduces network load and minimizes redundant requests, making it a more efficient choice for large-scale P2P networks. However, its primary disadvantage is the higher query failure rate, as the request might not reach a peer containing the desired file within a limited number of hops. Additionally, the response time may be slower, since the query propagates sequentially instead of being processed in parallel by multiple peers. Despite these limitations, Random Walk provides a more scalable alternative to Flooding, especially in decentralized and large-scale peer-to-peer systems.

Pros:
- Reduces network load.
- Scales well for large networks.

Cons:
- Higher query failure rate.
- Slower response time.

### Discuss scalability, reliability and anonymity of your P2P system.
The scalability of the P2P systems depends on the query method utilized. Flooding does not scale very well because huge amounts of queries lead to an exponential traffic increase. All peers send relevant queries to all other peers within the vicinity, which results in heavy system loads in vast networks. On the other hand, Random Walk has stronger scalability due to the limitation on domination rate of the query. However, the efficiency is lower since there is no assurance that the query will be served within a reasonable time frame. One of the ways to sustain good scalability while improving the system performance is to introduce a Hybrid Routing System that takes advantage of both techniques. Flooding can be used for a greater number of local peers while Random Walk can be used for those situated further away to diminish wasted traffic.


For reliability, the system’s performance is influenced by the availability of peers. 
If a peer goes offline, its stored files become inaccessible, impacting query success rates. 
Flooding ensures higher reliability, as long as at least one peer has the requested file, while Random Walk is more prone to failures if the query is misrouted and does not reach the correct peer. 

For anonymity, Flooding exposes queries to all peers, making it easy for others to track search patterns and query origins.
Random Walk improves anonymity, as only a subset of peers processes the query, limiting exposure. 

### [Option] Discuss any additional functionality you wish to introduce if any.
- Caching: Peers cache popular files to reduce redundant requests.
- Data Encryption: Encrypt file transfers for improved security.
- File Prioritization: Allow peers to prioritize certain file types for better performance.