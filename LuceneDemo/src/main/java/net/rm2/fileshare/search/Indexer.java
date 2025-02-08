package net.rm2.fileshare.search;

import net.rm2.fileshare.crawler.Crawler;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.BaseDirectory;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.util.List;

public class Indexer {
    private BaseDirectory directory;

    public void index(List<Crawler.Wiki> wikis) {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        ByteBuffersDirectory bufferDir = new ByteBuffersDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(bufferDir, config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Crawler.Wiki wiki : wikis) {
            Document doc = new Document();
            doc.add(new TextField("introduction", wiki.introduction(), Field.Store.YES));
            doc.add(new StringField("title", wiki.title(), Field.Store.YES));
            doc.add(new StringField("url", wiki.url(), Field.Store.YES));
            try {
                writer.addDocument(doc);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Indexing complete!");
        directory = bufferDir;
    }

    public BaseDirectory getDirectory() {
        return directory;
    }
}
