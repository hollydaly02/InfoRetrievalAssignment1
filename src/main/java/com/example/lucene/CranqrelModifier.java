package com.example.lucene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CranqrelModifier {

    public static void main(String[] args) {
        String inputFilePath = "src\\main\\java\\com\\example\\lucene\\cranqrel"; // Input file path
        String outputFilePath = "modified_cranqrel.txt"; // Output file path

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Split the line into parts
                String[] parts = line.split(" ");
                
                // Start building the modified line
                StringBuilder modifiedLine = new StringBuilder();
                modifiedLine.append(parts[0]).append(" 0"); // Append the first part and '0'

                // Iterate over the rest of the parts
                for (int i = 1; i < parts.length; i++) {
                    // Replace -1 with 5 if found
                    if (parts[i].equals("-1")) {
                        modifiedLine.append(" 5"); // Append '5' instead of '-1'
                    } else {
                        modifiedLine.append(" ").append(parts[i]); // Append the rest of the parts
                    }
                }

                // Write the modified line to the output file
                writer.write(modifiedLine.toString());
                writer.newLine();
            }
            System.out.println("Modification complete. Check the output file: " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
