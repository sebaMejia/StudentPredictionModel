package com.example;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;

public class TestSuite {
    // Helper method
    private File createTempCSVFile(String content) throws IOException {
        File tempFile = File.createTempFile("test_data", ".csv");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile;
    }

    // Node Tests
    @Test
    public void testFeatureRetrieval() {
        double[] features = {1.0, 2.0, 3.0};
        Node node = new Node(features, 1);
        assertEquals(1.0, node.getFeature(0), 0.001);
        assertEquals(2.0, node.getFeature(1), 0.001);
        assertEquals(3.0, node.getFeature(2), 0.001);
    }

    @Test
    public void testMissingFeatureHandling() {
        Node node = new Node(new double[]{1.0, 2.0}, 1);
        assertTrue(Double.isNaN(node.getFeature(2)));
    }

    @Test
    public void testNodeLabel() {
        Node node = new Node(new double[]{1.0, 2.0}, 1);
        assertEquals(1, node.getLabel());
    }

    // DataLoader Tests
    @Test
    public void testBasicDataLoading() throws IOException {
        String csvContent = "Hours Studied,Attendance,Final Score\n" +
                          "10,90,75\n" +
                          "5,60,65\n";
        File tempFile = createTempCSVFile(csvContent);
        DataLoader.LoadResult result = DataLoader.loadData(tempFile.getAbsolutePath());
        assertNotNull(result);
        assertEquals(2, result.data.size());
        assertEquals(0, result.linesSkipped);
        tempFile.delete();
    }

    @Test
    public void testExtremeNumericValues() throws IOException {
        String csvContent = "Hours Studied,Attendance,Previous Scores,Final Score\n" +
                          "0.1,0.1,0.1,70\n" +     // Very low values
                          "167.9,99.9,99.9,85\n" + // Just under maximum
                          "168.1,100.1,100.1,85\n"; // Just over maximum
        
        File tempFile = createTempCSVFile(csvContent);
        DataLoader.LoadResult result = DataLoader.loadData(tempFile.getAbsolutePath());
        assertEquals(2, result.data.size(), "Should accept only valid boundary values");
        assertEquals(1, result.linesSkipped, "Should skip values over maximum");
        tempFile.delete();
    }

    @Test
    public void testDataLoaderErrorHandling() {
        assertThrows(FileNotFoundException.class, () -> {
            DataLoader.loadData("nonexistent.csv");
        });
    }

    @Test
    public void testCategoricalValueMapping() throws IOException {
        String csvContent = "School Type,Gender,Final Score\n" +
                          "Public,Male,75\n" +
                          "Private,Female,85\n";
        File tempFile = createTempCSVFile(csvContent);
        DataLoader.LoadResult result = DataLoader.loadData(tempFile.getAbsolutePath());
        assertEquals(0.0, result.data.get(0).getFeature(0)); // Public = 0
        assertEquals(1.0, result.data.get(1).getFeature(0)); // Private = 1
        tempFile.delete();
    }

    // Decision Tree Tests
    @Test
    public void testTreeTraining() {
        DecisionTree tree = new DecisionTree(2, 2);
        List<Node> trainData = Arrays.asList(
            new Node(new double[]{10, 90}, 1),
            new Node(new double[]{5, 60}, 0)
        );
        tree.train(trainData);
        assertNotNull(tree.root);
        assertNotNull(tree.getFeatureImportances());
    }

    // Random Forest Tests
    @Test
    public void testForestAccuracy() {
        RandomForest forest = new RandomForest(100, 4, 19);
        List<Node> data = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            data.add(new Node(new double[]{20, 90}, 1));
            data.add(new Node(new double[]{5, 30}, 0));
        }
        forest.train(data);
        double accuracy = forest.evaluate(data);
        assertTrue(accuracy > 0.9, "Should achieve high accuracy on clear patterns");
    }

    @Test
    public void testConcurrentPredictions() throws InterruptedException {
        RandomForest forest = new RandomForest(5, 2, 2);
        List<Node> trainData = Arrays.asList(
            new Node(new double[]{10, 90}, 1),
            new Node(new double[]{5, 60}, 0)
        );
        forest.train(trainData);
        
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Integer> predictions = Collections.synchronizedList(new ArrayList<>());
        
        for(int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    predictions.add(forest.predict(new double[]{8, 85}));
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(threadCount, predictions.size());
    }

    // Feedback Analyzer Tests
    @Test
    public void testFeedbackForHighValues() {
        List<Node> data = Arrays.asList(
            new Node(new double[]{10, 90, 2, 2, 1, 8, 85, 2, 1, 5, 2, 2, 1, 2, 4, 0, 2, 0, 0}, 1)
        );
        FeedbackAnalyzer analyzer = new FeedbackAnalyzer(data);
        
        double[] highInput = {20, 95, 2, 2, 1, 8, 90, 2, 1, 5, 2, 2, 1, 2, 5, 0, 2, 0, 0};
        List<String> suggestions = analyzer.getSuggestions(highInput, "Pass");
        
        assertTrue(suggestions.stream().anyMatch(s -> s.contains("Great work")));
    }

    @Test
    public void testFeedbackConsistency() {
        List<Node> data = Arrays.asList(
            new Node(new double[]{10, 90, 2, 2, 1, 8, 85, 2, 1, 5, 2, 2, 1, 2, 4, 0, 2, 0, 0}, 1)
        );
        FeedbackAnalyzer analyzer = new FeedbackAnalyzer(data);
        
        double[] input = new double[19];
        Arrays.fill(input, 1.0);
        
        List<String> suggestions1 = analyzer.getSuggestions(input, "Fail");
        List<String> suggestions2 = analyzer.getSuggestions(input, "Fail");
        
        assertEquals(suggestions1, suggestions2);
    }

    @Test
    public void testInvalidInputFeedback() {
        FeedbackAnalyzer analyzer = new FeedbackAnalyzer(new ArrayList<>());
        double[] shortInput = new double[5];
        List<String> feedback = analyzer.getSuggestions(shortInput, "Fail");
        assertTrue(feedback.get(0).contains("Error"));
    }

    // System Integration Test
    @Test
    public void testCompleteSystemWorkflow() throws IOException {
        String csvContent = 
            "Hours Studied,Attendance,Parental Involvement,Access to Resources," +
            "Extracurricular Activities,Sleep Hours,Previous Scores,Motivation Level," +
            "Internet Access,Tutoring Sessions,Family Income,Teacher Quality," +
            "School Type,Peer Influence,Physical Activity,Learning Disabilities," +
            "Parental Education,Distance from Home,Gender,Final Score\n" +
            "10,90,High,High,Yes,8,85,Medium,Yes,5,High,High,Private,Positive,4,No,Postgraduate,Near,Male,75\n" +
            "5,60,Low,Low,No,4,55,Low,No,0,Low,Low,Public,Negative,0,Yes,High School,Far,Female,65\n";
        
        File tempFile = createTempCSVFile(csvContent);
        
        DataLoader.LoadResult loadResult = DataLoader.loadData(tempFile.getAbsolutePath());
        assertTrue(loadResult.data.size() > 0);
        
        RandomForest forest = new RandomForest(5, 4, 19);
        forest.train(loadResult.data);
        
        double[] passInput = new double[19];
        Arrays.fill(passInput, 2.0);
        assertEquals(1, forest.predict(passInput));
        
        double[] failInput = new double[19];
        Arrays.fill(failInput, 0.0);
        assertEquals(0, forest.predict(failInput));
        
        FeedbackAnalyzer analyzer = new FeedbackAnalyzer(loadResult.data);
        List<String> feedback = analyzer.getSuggestions(failInput, "Fail");
        assertFalse(feedback.isEmpty());
        
        tempFile.delete();
    }
}