package net.rm2.fileshare.search;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.BaseDirectory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Searcher {

    public List<Document> search(BaseDirectory directory, String queryStr) {
        DirectoryReader reader = null;
        try {
            reader = DirectoryReader.open(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        IndexSearcher searcher = new IndexSearcher(reader);

        StandardAnalyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser("introduction", analyzer);
        Query query = null;
        try {
            query = parser.parse(queryStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        TopDocs results = null;
        try {
            results = searcher.search(query, 100);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Found " + results.totalHits + " hits.");

        List<Document> res = Arrays.stream(results.scoreDocs).map(a -> {
            try {
                return searcher.storedFields().document(a.doc);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }
}
