package com.example.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

public class Searcher {

    public static void main(String[] args) throws Exception {
        String indexPath = "index"; // Path to your index directory
        //String queriesPath = "src/main/java/com/example/lucene/resources/cran.qry"; // Path to your queries file
        String queriesPath = "src\\main\\java\\com\\example\\lucene\\cran.qry";

        // Initialize the searcher
        IndexSearcher searcher = createSearcher(indexPath);
        analyzeQueries(queriesPath, searcher);
    }

    private static IndexSearcher createSearcher(String indexPath) throws IOException {
        DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        return new IndexSearcher(reader);
    }

    private static void analyzeQueries(String queriesPath, IndexSearcher searcher) throws Exception {
        Analyzer analyzer = new StandardAnalyzer();

        // Field names and their boost factors for multi-field queries
        HashMap<String, Float> boostedScores = new HashMap<>();
        boostedScores.put("title", 1.0f);
        boostedScores.put("author", 0.5f);
        boostedScores.put("bibliography", 0.2f);
        boostedScores.put("content", 1.0f); // Make sure the field names match your indexing fields

        MultiFieldQueryParser parser = new MultiFieldQueryParser(boostedScores.keySet().toArray(new String[0]), analyzer, boostedScores);

        try (BufferedReader reader = new BufferedReader(new FileReader(queriesPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    Query query = parser.parse(QueryParser.escape(line));
                    performSearch(searcher, query);
                }
            }
        }
    }

    private static void performSearch(IndexSearcher searcher, Query query) throws IOException {
        TopDocs results = searcher.search(query, 10); // Change number of hits to retrieve if needed
        ScoreDoc[] hits = results.scoreDocs;

        System.out.println("Found " + hits.length + " hits.");
        for (ScoreDoc hit : hits) {
            Document doc = searcher.doc(hit.doc);
            System.out.println("ID: " + doc.get("id") + ", Title: " + doc.get("title") + ", Author: " + doc.get("author"));
        }
    }
}
