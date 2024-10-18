package com.example.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

public class Searcher {

    public static void main(String[] args) throws Exception {
        String indexPath = "index"; // Path to index folder
        String queriesPath = "src\\main\\java\\com\\example\\lucene\\cran.qry"; // Path to queries file
        String outputPath = "results.txt"; // Output file path

        // Initialise the searcher
        IndexSearcher searcher = createSearcher(indexPath);
        analyzeQueries(queriesPath, searcher, outputPath);
    }

    private static IndexSearcher createSearcher(String indexPath) throws IOException {
        DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        return new IndexSearcher(reader);
    }

    private static void analyzeQueries(String queriesPath, IndexSearcher searcher, String outputPath) throws Exception {
        // Standard analyser, uncomment the desired anaylser
        //Analyzer analyzer = new StandardAnalyzer();

        // English analyser, uncomment the desired anaylser
        EnglishAnalyzer analyzer = new EnglishAnalyzer();

        // Field names and their boost factors for multi-field queries
        HashMap<String, Float> boostedScores = new HashMap<>();
        boostedScores.put("title", 1.0f);
        boostedScores.put("author", 0.5f);
        boostedScores.put("bibliography", 0.2f);
        boostedScores.put("content", 1.0f); // Ensure the field names match indexing fields

        MultiFieldQueryParser parser = new MultiFieldQueryParser(boostedScores.keySet().toArray(new String[0]), analyzer, boostedScores);

        try (BufferedReader reader = new BufferedReader(new FileReader(queriesPath));
             FileWriter writer = new FileWriter(outputPath)) { // Write results to a file
            String line;
            int queryId = 1; // Start query ID from 1
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && queryId <= 225) { // ensure queries above 225 aren't processed
                    Query query = parser.parse(QueryParser.escape(line));
                    performSearch(searcher, query, queryId, writer);
                    queryId++; // Increment query ID for the next query
                }
            }
        }
    }

    private static void performSearch(IndexSearcher searcher, Query query, int queryId, FileWriter writer) throws IOException {
        TopDocs results = searcher.search(query, 10); // Retrieve top 10 hits
        ScoreDoc[] hits = results.scoreDocs;

        int rank = 1; // Initialise rank for each query
        for (ScoreDoc hit : hits) {
            Document doc = searcher.doc(hit.doc);
            float score = hit.score; // Use the Lucene score
            // Write in the text format
            writer.write(String.format("%d Q0 %s %d %.4f STANDARD%n", queryId, doc.get("id"), rank, score));
            rank++; // Increment rank for the next document
        }
    }
}