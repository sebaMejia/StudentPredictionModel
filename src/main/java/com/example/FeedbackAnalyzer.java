package com.example;

import java.util.*;

public class FeedbackAnalyzer {
    private Map<String, Double> passAvg;
    private Map<String, Double> failAvg;
    private final List<Integer> controllable = Arrays.asList(0, 1, 5, 7, 4, 9, 14);
    final String[] featureNames = {
        "Hours Studied", "Attendance", "Parental Involvement", "Access to Resources",
        "Extracurricular Activities", "Sleep Hours", "Previous Scores", "Motivation Level",
        "Internet Access", "Tutoring Sessions", "Family Income", "Teacher Quality",
        "School Type", "Peer Influence", "Physical Activity", "Learning Disabilities",
        "Parental Education Level", "Distance from Home", "Gender"
    };
    private final Map<Integer, String[]> catMap;

    public FeedbackAnalyzer(List<Node> data) {
        catMap = buildCatMap();
        calcAvgs(data);
    }

    private Map<Integer, String[]> buildCatMap() {
        Map<Integer, String[]> m = new HashMap<>();
        // Must match exactly with DataLoader's mapping (Low=0, Medium=1, High=2)
        m.put(2, new String[]{"Low", "Medium", "High"});
        m.put(3, new String[]{"Low", "Medium", "High"});
        m.put(4, new String[]{"No", "Yes"});
        m.put(7, new String[]{"Low", "Medium", "High"});
        m.put(8, new String[]{"No", "Yes"});
        m.put(10, new String[]{"Low", "Medium", "High"});
        m.put(11, new String[]{"Low", "Medium", "High"});
        m.put(12, new String[]{"Public", "Private"});
        m.put(13, new String[]{"Negative", "Neutral", "Positive"});
        m.put(15, new String[]{"No", "Yes"});
        m.put(16, new String[]{"High School", "College", "Postgraduate"});
        m.put(17, new String[]{"Near", "Moderate", "Far"});
        m.put(18, new String[]{"Male", "Female"});
        return m;
    }

    private boolean isValidInput(double[] input) {
        if (input == null || input.length != featureNames.length) {
            return false;
        }
        
        return input[0] >= 0 && input[0] <= 168 && // Hours studied (per week)
               input[1] >= 0 && input[1] <= 100 && // Attendance
               input[5] >= 0 && input[5] <= 24 &&  // Sleep hours
               input[6] >= 0 && input[6] <= 100 && // Previous scores
               input[9] >= 0 && input[9] <= 100 && // Tutoring sessions
               input[14] >= 0 && input[14] <= 168; // Physical activity
    }

    public List<String> getSuggestions(double[] userInput, String result) {
        List<String> sugs = new ArrayList<>();
        
        if (!isValidInput(userInput)) {
            sugs.add("Error: Invalid input values detected");
            sugs.add("Please ensure all values are within valid ranges:");
            sugs.add("- Hours studied: 0-168 hours per week");
            sugs.add("- Attendance: 0-100%");
            sugs.add("- Sleep hours: 0-24 hours per day");
            sugs.add("- Previous scores: 0-100");
            sugs.add("- Tutoring sessions: 0-100 per month");
            sugs.add("- Physical activity: 0-168 hours per week");
            return sugs;
        }

        sugs.add("Your Result: " + result);
        sugs.add("");
        sugs.add("Suggestions:");
        boolean hasSuggestions = false;

        for (int i : controllable) {
            double uv = userInput[i];
            double pav = passAvg.get("feature" + i);

            if (catMap.containsKey(i)) {
                int uvIndex = (int) Math.min(Math.max(uv, 0), catMap.get(i).length - 1);
                int pavIndex = (int) Math.min(Math.max(Math.round(pav), 0), catMap.get(i).length - 1);
                
                String ucat = catMap.get(i)[uvIndex];
                String pcat = catMap.get(i)[pavIndex];

                // Only suggest improvement if current value is lower than target
                if (!ucat.equals(pcat) && uvIndex < pavIndex) {
                    sugs.add("- Consider improving " + featureNames[i] + " from '" + ucat + "' to '" + pcat + "'.");
                    hasSuggestions = true;
                }
            } else {
                int ruv = (int) Math.round(uv);
                int rpav = (int) Math.round(pav);

                if (Math.abs(ruv - rpav) > 2) {
                    if (ruv < rpav) {
                        String unit = "";
                        if (i == 0 || i == 14) unit = " hours";
                        else if (i == 1) unit = "%";
                        
                        sugs.add("- Consider improving " + featureNames[i] + " closer to " + rpav + unit + ".");
                        hasSuggestions = true;
                    } else if (result.equals("Fail")) {
                        sugs.add("- Your " + featureNames[i] + " is good! Consider focusing on other areas for improvement.");
                        hasSuggestions = true;
                    }
                }
            }
        }

        if (!hasSuggestions) {
            if (result.equals("Pass")) {
                sugs.add("- Great work! Keep maintaining your current effort levels.");
            } else {
                sugs.add("- Consider working on consistent improvement across all areas.");
            }
        }

        return sugs;
    }

    private void calcAvgs(List<Node> data) {
        Map<String, Double> pSum = new HashMap<>();
        Map<String, Double> fSum = new HashMap<>();
        int pCount = 0, fCount = 0;
        int totalF = featureNames.length;

        for (int i = 0; i < totalF; i++) {
            pSum.put("feature" + i, 0.0);
            fSum.put("feature" + i, 0.0);
        }

        for (Node d : data) {
            if (d.getLabel() == 1) {
                pCount++;
                for (int i = 0; i < totalF; i++) {
                    pSum.put("feature" + i, pSum.get("feature" + i) + d.getFeature(i));
                }
            } else {
                fCount++;
                for (int i = 0; i < totalF; i++) {
                    fSum.put("feature" + i, fSum.get("feature" + i) + d.getFeature(i));
                }
            }
        }

        passAvg = new HashMap<>();
        failAvg = new HashMap<>();
        for (int i = 0; i < totalF; i++) {
            passAvg.put("feature" + i, pCount > 0 ? pSum.get("feature" + i) / pCount : 0);
            failAvg.put("feature" + i, fCount > 0 ? fSum.get("feature" + i) / fCount : 0);
        }
    }
}