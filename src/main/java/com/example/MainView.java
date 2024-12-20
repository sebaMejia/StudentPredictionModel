package com.example;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

// Main GUI class that handles all the visual components
public class MainView {
    // Main components
    JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    // Different screen panels
    private JPanel welcomePanel;
    private JPanel predictionPanel;
    private JPanel feedbackPanel;

    // Welcome screen components
    private JTextField nameField;
    private JButton uploadButton;
    JLabel fileStatusLabel;
    private JButton proceedButton;

    // Prediction screen components
    private JTextField hoursStudied, attendance, sleepHours, previousScores, tutoringSessions, physicalActivity;
    private JComboBox<String> parentalInvolvement, accessToResources, extracurricularActivities, motivationLevel, internetAccess;
    private JComboBox<String> familyIncome, teacherQuality, schoolType, peerInfluence, learningDisabilities, parentalEducation, distanceFromHome, gender;
    private JButton predictButton;
    private JLabel predictionResultLabel;

    // Feedback screen components
    JTextArea feedbackArea;
    private JButton backButton;
    private JButton exportButton;

    JTextArea updatesArea;

    // Constructor - sets up the whole GUI
    public MainView() {
        // Try to use Nimbus look and feel for better appearance
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus fails, use default look and feel
        }

        // Setup main window
        frame = new JFrame("Student Prediction System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 900);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        // Setup card layout for switching between screens
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create all screens
        createWelcomePanel();
        createPredictionPanel();
        createFeedbackPanel();

        // Add screens to main panel
        mainPanel.add(welcomePanel, "Welcome");
        mainPanel.add(predictionPanel, "Prediction");
        mainPanel.add(feedbackPanel, "Feedback");

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // Creates the welcome screen
    private void createWelcomePanel() {
        welcomePanel = new JPanel(new BorderLayout(10, 10));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
        // Create mission statement area
        updatesArea = new JTextArea();
        updatesArea.setEditable(false);
        updatesArea.setLineWrap(true);
        updatesArea.setWrapStyleWord(true);
        updatesArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        updatesArea.setText(
            "Mission Statement:\n" +
            "Our model aims to predict whether a student will pass or fail based on various factors.\n\n" +
            "Instructions:\n" +
            "1. Enter your name\n" +
            "2. Upload your student data CSV file\n" +
            "3. Click proceed to start making predictions"
        );
    
        JScrollPane updatesScroll = new JScrollPane(updatesArea);
        updatesScroll.setPreferredSize(new Dimension(550, 200));
    
        JPanel updatesPanel = new JPanel(new BorderLayout());
        updatesPanel.add(updatesScroll, BorderLayout.CENTER);
        updatesPanel.setBorder(BorderFactory.createTitledBorder("Mission & Instructions"));
    
        // Create input section
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
    
        // Name input
        JLabel nameLabel = new JLabel("Enter your name:");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
        nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(300, 40));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // File upload section
        uploadButton = new JButton("Upload CSV File");
        uploadButton.setMaximumSize(new Dimension(300, 50));
        uploadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        fileStatusLabel = new JLabel("<html><div style='text-align: center; width: 280px'>No file uploaded</div></html>");
        fileStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        fileStatusLabel.setPreferredSize(new Dimension(300, 80));
        fileStatusLabel.setMinimumSize(new Dimension(300, 80));
        fileStatusLabel.setMaximumSize(new Dimension(300, 80));
    
        proceedButton = new JButton("Proceed to Prediction");
        proceedButton.setMaximumSize(new Dimension(300, 50));
        proceedButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        proceedButton.setEnabled(false);
    
        // Add everything to input panel
        inputPanel.add(Box.createVerticalGlue());
        inputPanel.add(nameLabel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(nameField);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        inputPanel.add(uploadButton);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(fileStatusLabel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        inputPanel.add(proceedButton);
        inputPanel.add(Box.createVerticalGlue());
    
        // Add all panels to welcome screen
        welcomePanel.add(updatesPanel, BorderLayout.NORTH);
        welcomePanel.add(inputPanel, BorderLayout.CENTER);
    }

// Creates the prediction input screen
private void createPredictionPanel() {
    predictionPanel = new JPanel(new GridLayout(22, 2, 10, 10));
    predictionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Create text fields for numeric inputs
    hoursStudied = new JTextField();
    hoursStudied.setPreferredSize(new Dimension(300, 40));
    attendance = new JTextField();
    attendance.setPreferredSize(new Dimension(300, 40));
    sleepHours = new JTextField();
    sleepHours.setPreferredSize(new Dimension(300, 40));
    previousScores = new JTextField();
    previousScores.setPreferredSize(new Dimension(300, 40));
    tutoringSessions = new JTextField();
    tutoringSessions.setPreferredSize(new Dimension(300, 40));
    physicalActivity = new JTextField();
    physicalActivity.setPreferredSize(new Dimension(300, 40));

    // Create dropdown menus for categorical inputs
    parentalInvolvement = new JComboBox<>(new String[]{"High", "Medium", "Low"});
    accessToResources = new JComboBox<>(new String[]{"High", "Medium", "Low"});
    extracurricularActivities = new JComboBox<>(new String[]{"Yes", "No"});
    motivationLevel = new JComboBox<>(new String[]{"High", "Medium", "Low"});
    internetAccess = new JComboBox<>(new String[]{"Yes", "No"});
    familyIncome = new JComboBox<>(new String[]{"High", "Medium", "Low"});
    teacherQuality = new JComboBox<>(new String[]{"High", "Medium", "Low"});
    schoolType = new JComboBox<>(new String[]{"Public", "Private"});
    peerInfluence = new JComboBox<>(new String[]{"Positive", "Neutral", "Negative"});
    learningDisabilities = new JComboBox<>(new String[]{"Yes", "No"});
    parentalEducation = new JComboBox<>(new String[]{"High School", "College", "Postgraduate"});
    distanceFromHome = new JComboBox<>(new String[]{"Near", "Moderate", "Far"});
    gender = new JComboBox<>(new String[]{"Male", "Female"});

    predictButton = new JButton("Predict");
    predictionResultLabel = new JLabel("Result: ");
    predictionResultLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

    // Add all components to the panel
    predictionPanel.add(new JLabel("Hours Studied per week:"));
    predictionPanel.add(hoursStudied);
    predictionPanel.add(new JLabel("Percentage of class attended:"));
    predictionPanel.add(attendance);
    predictionPanel.add(new JLabel("Parental Involvement:"));
    predictionPanel.add(parentalInvolvement);
    predictionPanel.add(new JLabel("Access to Resources:"));
    predictionPanel.add(accessToResources);
    predictionPanel.add(new JLabel("Extracurricular Activities:"));
    predictionPanel.add(extracurricularActivities);
    predictionPanel.add(new JLabel("Sleep Hours per night:"));
    predictionPanel.add(sleepHours);
    predictionPanel.add(new JLabel("Previous Exam Score:"));
    predictionPanel.add(previousScores);
    predictionPanel.add(new JLabel("Motivation Level:"));
    predictionPanel.add(motivationLevel);
    predictionPanel.add(new JLabel("Internet Access:"));
    predictionPanel.add(internetAccess);
    predictionPanel.add(new JLabel("Tutoring Sessions per month:"));
    predictionPanel.add(tutoringSessions);
    predictionPanel.add(new JLabel("Family Income:"));
    predictionPanel.add(familyIncome);
    predictionPanel.add(new JLabel("Teacher Quality:"));
    predictionPanel.add(teacherQuality);
    predictionPanel.add(new JLabel("School Type:"));
    predictionPanel.add(schoolType);
    predictionPanel.add(new JLabel("Peer Influence:"));
    predictionPanel.add(peerInfluence);
    predictionPanel.add(new JLabel("Hours of Physical Activity Per Week:"));
    predictionPanel.add(physicalActivity);
    predictionPanel.add(new JLabel("Learning Disabilities:"));
    predictionPanel.add(learningDisabilities);
    predictionPanel.add(new JLabel("Parental Education:"));
    predictionPanel.add(parentalEducation);
    predictionPanel.add(new JLabel("Distance from Home:"));
    predictionPanel.add(distanceFromHome);
    predictionPanel.add(new JLabel("Gender:"));
    predictionPanel.add(gender);

    predictionPanel.add(predictButton);
    predictionPanel.add(predictionResultLabel);
}

// Creates the feedback screen
private void createFeedbackPanel() {
    feedbackPanel = new JPanel(new BorderLayout(10, 10));
    feedbackPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    feedbackArea = new JTextArea();
    feedbackArea.setEditable(false);
    feedbackArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

    exportButton = new JButton("Export Results");
    backButton = new JButton("Back to Prediction");

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
    buttonPanel.add(exportButton);
    buttonPanel.add(backButton);

    feedbackPanel.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);
    feedbackPanel.add(buttonPanel, BorderLayout.SOUTH);
}

// Helper methods for managing the GUI
public void showCard(String cardName) {
    cardLayout.show(mainPanel, cardName);
}

public String getNameInput() {
    return nameField.getText().trim();
}

public void setWelcomeMessage(String message, Color color) {
    JPanel inputPanel = (JPanel) welcomePanel.getComponent(1);
    JLabel messageLabel = new JLabel(message);
    messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    messageLabel.setForeground(color);
    inputPanel.add(messageLabel);
}

// Method to update file status display with proper formatting
public void setFileStatus(String status, boolean enableProceed) {
    String wrappedText = "<html><div style='text-align: center; width: 280px'>" + 
                       status.replace("\n", "<br>") + 
                       "</div></html>";
    fileStatusLabel.setText(wrappedText);
    proceedButton.setEnabled(enableProceed);
}

// Button listener setters
public void addUploadButtonListener(ActionListener listener) {
    uploadButton.addActionListener(listener);
}

public void addProceedButtonListener(ActionListener listener) {
    proceedButton.addActionListener(listener);
}

public void addPredictButtonListener(ActionListener listener) {
    predictButton.addActionListener(listener);
}

public void addBackButtonListener(ActionListener listener) {
    backButton.addActionListener(listener);
}

public void addExportButtonListener(ActionListener listener) {
    exportButton.addActionListener(listener);
}

// Collects all prediction inputs and converts to array
public double[] getPredictionInput() throws NumberFormatException {
    double[] input = new double[19];
    input[0] = Double.parseDouble(hoursStudied.getText());
    input[1] = Double.parseDouble(attendance.getText());
    input[2] = parentalInvolvement.getSelectedIndex();
    input[3] = accessToResources.getSelectedIndex();
    input[4] = extracurricularActivities.getSelectedIndex();
    input[5] = Double.parseDouble(sleepHours.getText());
    input[6] = Double.parseDouble(previousScores.getText());
    input[7] = motivationLevel.getSelectedIndex();
    input[8] = internetAccess.getSelectedIndex();
    input[9] = Double.parseDouble(tutoringSessions.getText());
    input[10] = familyIncome.getSelectedIndex();
    input[11] = teacherQuality.getSelectedIndex();
    input[12] = schoolType.getSelectedIndex();
    input[13] = peerInfluence.getSelectedIndex();
    input[14] = Double.parseDouble(physicalActivity.getText());
    input[15] = learningDisabilities.getSelectedIndex();
    input[16] = parentalEducation.getSelectedIndex();
    input[17] = distanceFromHome.getSelectedIndex();
    input[18] = gender.getSelectedIndex();
    return input;
}

// Updates the prediction result display
public void setPredictionResult(String result) {
    predictionResultLabel.setText("Result: " + result);
}

// Updates the feedback text
public void setFeedbackText(String feedback) {
    feedbackArea.setText(feedback);
}
}