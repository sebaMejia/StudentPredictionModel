package com.example;

import java.io.*;
import java.util.*;

public class DataLoader {
    private static String[] featureNames;

    public static class LoadResult {
        public List<Node> data;
        public int linesSkipped;
        public int imputedCount;
        
        public LoadResult(List<Node> data, int linesSkipped, int imputedCount) {
            this.data = data;
            this.linesSkipped = linesSkipped;
            this.imputedCount = imputedCount;
        }
    }

    private static boolean isValidNumericValue(String val, int featureIndex) {
        try {
            double num = Double.parseDouble(val);
            // Validate based on feature type
            switch(featureIndex) {
                case 0: // Hours Studied
                    return num >= 0 && num <= 168; // Max hours in a week
                case 1: // Attendance
                    return num >= 0 && num <= 100; // Percentage
                case 6: // Previous Scores
                    return num >= 0 && num <= 100; // Score range
                case 5: // Sleep Hours
                    return num >= 0 && num <= 24; // Hours per day
                case 9: // Tutoring Sessions
                    return num >= 0 && num <= 100; // Monthly sessions
                case 14: // Physical Activity
                    return num >= 0 && num <= 168; // Hours per week
                default:
                    return num >= 0; // All other numeric fields shouldn't be negative
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static LoadResult loadData(String filePath) throws IOException {
        List<Node> data = new ArrayList<>();
    int linesSkipped = 0;
    int imputedCount = 0;
    Map<String, Integer> mapping = buildMapping();

    List<String[]> records = parseCSV(filePath);
    
    if (records.isEmpty()) {
        return new LoadResult(data, linesSkipped, imputedCount);
    }

        String[] headers = records.get(0);
        featureNames = Arrays.copyOf(headers, headers.length - 1);
        int nf = headers.length - 1;

        Map<Integer, Double> means = new HashMap<>();
        Map<Integer, String> modes = new HashMap<>();
        
        for (int j = 0; j < nf; j++) {
            List<Double> numericVals = new ArrayList<>();
            Map<String, Integer> categoryFreq = new HashMap<>();
            
            for (int i = 1; i < records.size(); i++) {
                String val = records.get(i)[j].trim();
                if (!val.isEmpty()) {
                    if (mapping.containsKey(val)) {
                        categoryFreq.merge(val, 1, Integer::sum);
                    } else {
                        try {
                            if (isValidNumericValue(val, j)) {
                                numericVals.add(Double.parseDouble(val));
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid numbers
                        }
                    }
                }
            }
            
            if (!numericVals.isEmpty()) {
                means.put(j, numericVals.stream().mapToDouble(d -> d).average().orElse(0.0));
            } else if (!categoryFreq.isEmpty()) {
                modes.put(j, Collections.max(categoryFreq.entrySet(), Map.Entry.comparingByValue()).getKey());
            }
        }

        for (int i = 1; i < records.size(); i++) {
            String[] tokens = records.get(i);
            if (tokens.length != headers.length) {
                linesSkipped++;
                continue;
            }

            double[] feats = new double[nf];
            boolean recordValid = true;
            boolean recordImputed = false;

            for (int j = 0; j < nf; j++) {
                String val = tokens[j].trim();
                if (val.isEmpty()) {
                    if (means.containsKey(j)) {
                        feats[j] = means.get(j);
                        recordImputed = true;
                    } else if (modes.containsKey(j)) {
                        feats[j] = mapping.get(modes.get(j));
                        recordImputed = true;
                    } else {
                        recordValid = false;
                        break;
                    }
                } else if (mapping.containsKey(val)) {
                    feats[j] = mapping.get(val);
                } else {
                    if (isValidNumericValue(val, j)) {
                        feats[j] = Double.parseDouble(val);
                    } else {
                        recordValid = false;
                        break;
                    }
                }
            }

            if (!recordValid) {
                linesSkipped++;
                continue;
            }

            try {
                double examScore = Double.parseDouble(tokens[nf].trim());
                if (examScore >= 0 && examScore <= 100) {
                    int label = examScore >= 70 ? 1 : 0;
                    data.add(new Node(feats, label));
                    if (recordImputed) imputedCount++;
                } else {
                    linesSkipped++;
                }
            } catch (NumberFormatException e) {
                linesSkipped++;
            }
        }

        return new LoadResult(data, linesSkipped, imputedCount);
    }

    public static String[] getFeatureNames() {
        return featureNames;
    }

    private static Map<String, Integer> buildMapping() {
        Map<String, Integer> m = new HashMap<>();
        m.put("Low", 0);
        m.put("Medium", 1);
        m.put("High", 2);
        m.put("No", 0);
        m.put("Yes", 1);
        m.put("Public", 0);
        m.put("Private", 1);
        m.put("Negative", 0);
        m.put("Neutral", 1);
        m.put("Positive", 2);
        m.put("High School", 0);
        m.put("College", 1);
        m.put("Postgraduate", 2);
        m.put("Near", 0);
        m.put("Moderate", 1);
        m.put("Far", 2);
        m.put("Male", 0);
        m.put("Female", 1);
        return m;
    }

    private static List<String[]> parseCSV(String filePath) throws IOException {
        List<String[]> recs = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = br.readLine()) != null) {
            List<String> tokens = new ArrayList<>();
            boolean inQuotes = false;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '"') {
                    inQuotes = !inQuotes;
                } else if (c == ',' && !inQuotes) {
                    tokens.add(sb.toString().trim());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
            tokens.add(sb.toString().trim());
            recs.add(tokens.toArray(new String[0]));
        }
        br.close();
        return recs;
    }
}