package net.rm2.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Crawler {

    public record Wiki(String title, String url, String introduction) {
    }

    private List<Wiki> wikis = new ArrayList<>();
    private String baseUrl;

    public Crawler(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void scrape(int amounts) {
        for (int i = 0; i < amounts; i++) {
            try {
                // Connect to the random article URL
                Document doc = Jsoup.connect(baseUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .followRedirects(true)
                        .get();

                String title = doc.title().replace(" - Wikipedia", "");

                String url = doc.location();
                String introduction;
                if (doc.select("p").first() != null) {
                    introduction = doc.select("p").first().text();
                } else {
                    introduction = "";
                }

                wikis.add(new Wiki(title, url, introduction));

                System.out.println("Crawled: " + title);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Wiki> getWikis() {
        return wikis;
    }
}
