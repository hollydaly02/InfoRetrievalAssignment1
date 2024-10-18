package com.example.lucene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CranqrelModifier {

    public static void main(String[] args) {
        String inputFilePath = "src\\main\\java\\com\\example\\lucene\\cranqrel";// Input file path
        String outputFilePath = "modified_cranqrel.txt"; // Output file path

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Split the line into parts
                String[] parts = line.split(" ");
                // Insert '0' after the first part
                String modifiedLine = parts[0] + " 0";
                // Append the rest of the parts
                for (int i = 1; i < parts.length; i++) {
                    modifiedLine += " " + parts[i];
                }
                // Write the modified line to the output file
                writer.write(modifiedLine);
                writer.newLine();
            }
            System.out.println("Modification complete. Check the output file: " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
