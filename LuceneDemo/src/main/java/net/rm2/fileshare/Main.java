package net.rm2.fileshare;

import net.rm2.fileshare.crawler.Crawler;
import net.rm2.fileshare.search.Indexer;
import net.rm2.fileshare.search.Searcher;
import org.apache.lucene.document.Document;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Crawler crawler = new Crawler("https://en.wikipedia.org/wiki/Special:Random");
        Indexer indexer = new Indexer();
        Searcher searcher = new Searcher();
        List<Document> result = null;

        Scanner sc = new Scanner(System.in);
        System.out.println("Crawler/Indexer/Searcher for wikipedia.org");

        while (true) {
            System.out.println("Input your action:");
            String s = sc.next();

            if (s.equalsIgnoreCase("stop")) {
                System.out.println(">");
                System.out.println("Stop the Lucene Demo");
                break;
            }

            if (s.equalsIgnoreCase("craw")) {
                int i = 0;
                try {
                    i = sc.nextInt();
                } catch (InputMismatchException e) {
                    System.err.println("Please input a integer");
                }
                System.out.println(">");
                System.out.println("Crawler start");
                long time = System.currentTimeMillis();
                crawler.scrape(i);
                System.out.println("Total time: " + (System.currentTimeMillis() - time) + "ms");
            }

            if (s.equalsIgnoreCase("index")) {
                System.out.println(">");
                System.out.println("Creating index");
                long time = System.currentTimeMillis();
                indexer.index(crawler.getWikis());
                System.out.println("Total time: " + (System.currentTimeMillis() - time) + "ms");

            }

            if (s.equalsIgnoreCase("search")) {
                String str = sc.next();
                System.out.println(">");
                System.out.println("Searching for " + str);
                long time = System.currentTimeMillis();
                result = searcher.search(indexer.getDirectory(), str);
                System.out.println("Total time: " + (System.currentTimeMillis() - time) + "ms");
                System.out.println(">");
                System.out.println("Printing search result");
                if (result != null) {
                    result.forEach(doc -> {
                        System.out.println("Title: " + doc.get("title"));
                        System.out.println("Url: " + doc.get("url"));
                        System.out.println("Introduction: " + doc.get("introduction"));
                    });
                } else {
                    System.err.println("result is null");
                }
            }
        }
    }
}