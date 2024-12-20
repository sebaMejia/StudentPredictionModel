package com.example;

import java.util.Collections;
import java.util.List;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

// Main controller class that connects the GUI with the machine learning model
public class Main {
    // Static instances of our ML models
    private static RandomForest rf;
    private static FeedbackAnalyzer fa;
    
    // List of features our model expects
    private static final String[] EXPECTED_FEATURES = {
        "Hours Studied", "Attendance", "Parental Involvement", "Access to Resources",
        "Extracurricular Activities", "Sleep Hours", "Previous Scores", "Motivation Level",
        "Internet Access", "Tutoring Sessions", "Family Income", "Teacher Quality",
        "School Type", "Peer Influence", "Physical Activity", "Learning Disabilities",
        "Parental Education", "Distance from Home", "Gender"
    };
    
    public static void main(String[] args) {
        // Try to set system look and feel
        try {
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // If it fails, program will use default look and feel
        }

        // Create the main window
        MainView view = new MainView();

        // Setup file upload button action
        view.addUploadButtonListener(e -> {
            // Show file picker dialog
            FileDialog dialog = new FileDialog(view.frame, "Select Student Data CSV File", FileDialog.LOAD);
            dialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".csv"));
            dialog.setVisible(true);
            
            if (dialog.getFile() != null) {
                File selectedFile = new File(dialog.getDirectory(), dialog.getFile());
                try {
                    // Load and process the CSV file
                    DataLoader.LoadResult loadRes = DataLoader.loadData(selectedFile.getAbsolutePath());
                    List<Node> data = loadRes.data;
                    
                    // Check if file has data
                    if (data.isEmpty()) {
                        JOptionPane.showMessageDialog(view.frame,
                            "Error: The CSV file is empty or contains no valid data.",
                            "Invalid Data",
                            JOptionPane.ERROR_MESSAGE);
                        view.setFileStatus("Error: Empty or invalid CSV file", false);
                        return;
                    }

                    // Check if file has correct number of features
                    if (data.get(0).getNumFeatures() != EXPECTED_FEATURES.length) {
                        JOptionPane.showMessageDialog(view.frame,
                            "Error: The CSV file must contain exactly " + EXPECTED_FEATURES.length + " features.",
                            "Invalid Data Format",
                            JOptionPane.ERROR_MESSAGE);
                        view.setFileStatus("Error: Incorrect number of features", false);
                        return;
                    }

                    // Check if we have enough data to train
                    if (data.size() < 10) {
                        JOptionPane.showMessageDialog(view.frame,
                            "Error: The CSV file must contain at least 10 records for training.",
                            "Insufficient Data",
                            JOptionPane.ERROR_MESSAGE);
                        view.setFileStatus("Error: Insufficient data for training", false);
                        return;
                    }

                    // Split data into training and testing sets
                    Collections.shuffle(data);
                    int split = (int)(data.size() * 0.8);
                    List<Node> trainData = data.subList(0, split);
                    List<Node> testData = data.subList(split, data.size());

                    // Train the random forest model
                    int nf = trainData.get(0).getNumFeatures();
                    int mf = (int)Math.sqrt(nf);
                    rf = new RandomForest(100, mf, nf);
                    rf.train(trainData);

                    // Setup feedback analyzer
                    fa = new FeedbackAnalyzer(data);

                    // Calculate and display accuracy
                    int accuracy = (int)Math.round(rf.evaluate(testData) * 100);
                    StringBuilder statusMsg = new StringBuilder(String.format(
                        "Model trained (Accuracy: %d%%)\n%d records processed", 
                        accuracy, data.size()
                    ));
                    
                    // Add info about skipped/imputed rows
                    if (loadRes.linesSkipped > 0 || loadRes.imputedCount > 0) {
                        statusMsg.append("\n");
                        if (loadRes.linesSkipped > 0) {
                            statusMsg.append(loadRes.linesSkipped).append(" rows skipped");
                        }
                        if (loadRes.imputedCount > 0) {
                            if (loadRes.linesSkipped > 0) statusMsg.append("\n");
                            statusMsg.append(loadRes.imputedCount).append(" rows had missing values");
                        }
                    }
                    
                    view.setFileStatus(statusMsg.toString(), true);
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view.frame,
                        "Error: Unable to process the CSV file.\nPlease ensure it follows the required format.",
                        "Processing Error",
                        JOptionPane.ERROR_MESSAGE);
                    view.setFileStatus("Error processing file", false);
                }
            }
        });

        // Setup proceed button action
        view.addProceedButtonListener(e -> {
            String user = view.getNameInput();
            if (user.isEmpty()) {
                view.setWelcomeMessage("Please enter your name.", Color.RED);
            } else if (rf == null) {
                view.setWelcomeMessage("Please upload a CSV file first.", Color.RED);
            } else {
                view.setWelcomeMessage("Welcome, " + user + "!", Color.BLUE);
                view.showCard("Prediction");
            }
        });

        // Setup predict button action
        view.addPredictButtonListener(e -> {
            try {
                double[] input = view.getPredictionInput();
                int pred = rf.predict(input);
                String res = pred == 1 ? "Pass" : "Fail";
                view.setPredictionResult(res);
                List<String> sugs = fa.getSuggestions(input, res);
                StringBuilder sb = new StringBuilder();
                for (String s : sugs) sb.append(s).append("\n");
                view.setFeedbackText(sb.toString());
                view.showCard("Feedback");
            } catch(NumberFormatException ex) {
                view.setFeedbackText("Please enter valid numeric inputs.");
                view.showCard("Feedback");
            }
        });

        // Setup back button action
        view.addBackButtonListener(e -> view.showCard("Prediction"));

        // Setup export button action
        view.addExportButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog dialog = new FileDialog(view.frame, "Save Results", FileDialog.SAVE);
                dialog.setFile("results.txt");  // Default filename
                dialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".txt"));
                dialog.setVisible(true);
                
                if (dialog.getFile() != null) {
                    try {
                        String filePath = new File(dialog.getDirectory(), dialog.getFile()).getAbsolutePath();
                        // Make sure file ends with .txt
                        if (!filePath.toLowerCase().endsWith(".txt")) {
                            filePath += ".txt";
                        }
                        
                        String feedback = view.feedbackArea.getText();
                        FileWriter writer = new FileWriter(filePath);
                        writer.write(feedback);
                        writer.close();
                        JOptionPane.showMessageDialog(view.frame, 
                            "Results exported successfully!", "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } catch(IOException ioException){
                        JOptionPane.showMessageDialog(view.frame, 
                            "Error exporting results.", "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }
}