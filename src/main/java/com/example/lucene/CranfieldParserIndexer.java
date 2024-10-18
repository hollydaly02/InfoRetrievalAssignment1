package com.example.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CranfieldParserIndexer {

    private static final String INDEX_DIRECTORY = "index"; 

    public static void main(String[] args) {
        try {
            // Setup the analyser and index writer, uncomment desired analyser
            // Standard analyser
           Analyzer analyzer = new StandardAnalyzer();

            // English analyser
            //EnglishAnalyzer analyzer = new EnglishAnalyzer();

            Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(directory, config);

            // Parse and index the documents from cran.all.1400
            String filePath = "src\\main\\java\\com\\example\\lucene\\cran.all.1400";
            try (FileWriter logWriter = new FileWriter("parsed_data_log.txt")) { // Create a log file writer
                parseAndIndex(writer, Paths.get(filePath), logWriter);
            }

            // Commit and close the writer
            writer.commit();
            writer.close();
            directory.close();

            System.out.println("Indexing completed successfully.");
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Parsing and indexing the file
    private static void parseAndIndex(IndexWriter writer, Path filePath, FileWriter logWriter) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            Document doc = null;
            String currentField = "";
            StringBuilder contentBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith(".I")) {
                    // Index the previous document
                    if (doc != null) {
                        // Add the retreived content to the document
                        if (contentBuilder.length() > 0) {
                            doc.add(new TextField("content", contentBuilder.toString(), TextField.Store.YES));
                            contentBuilder.setLength(0);
                        }
                        writer.addDocument(doc);
                        logParsedData(logWriter, doc);
                    }
                    // Start a new document
                    doc = new Document();
                    String id = line.split("\\s+")[1]; // Extract ID
                    doc.add(new StringField("id", id, StringField.Store.YES)); // Store ID
                } else if (line.equals(".T")) {
                    currentField = "Title";
                } else if (line.equals(".A")) {
                    currentField = "Author";
                } else if (line.equals(".W")) {
                    currentField = "Content";
                } else {
                    if (currentField.equals("Title")) {
                        doc.add(new TextField("title", line, TextField.Store.YES));
                    } else if (currentField.equals("Author")) {
                        doc.add(new TextField("author", line, TextField.Store.YES));
                    } else if (currentField.equals("Content")) {
                        contentBuilder.append(line).append(" "); 
                    }
                }
            }

            // Index the last document
            if (doc != null) {
                if (contentBuilder.length() > 0) {
                    doc.add(new TextField("content", contentBuilder.toString(), TextField.Store.YES));
                }
                writer.addDocument(doc);
                logParsedData(logWriter, doc);
            }
        }
    }

    // Method to log parsed data to a file
    private static void logParsedData(FileWriter logWriter, Document doc) throws IOException {
        String id = doc.get("id");
        String title = doc.get("title");
        String author = doc.get("author");
        String content = doc.get("content");

        // Log the data in a readable format
        logWriter.write("Document ID: " + id + "\n");
        logWriter.write("Title: " + title + "\n");
        logWriter.write("Author: " + author + "\n");
        logWriter.write("Content: " + content + "\n");
        logWriter.write("--------------------------------------------------\n"); // Separator for each document
        logWriter.flush(); // Ensure data is written to file
    }
}
