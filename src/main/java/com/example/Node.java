package com.example;

import java.util.HashMap;
import java.util.Map;

// This class stores a single record (features + label).
// Features are stored in a map from index to value.
// Label is 0 or 1.
public class Node {
    private Map<Integer, Double> features;
    private int label;

    public Node(double[] featArr, int label) {
        features = new HashMap<>();
        for (int i = 0; i < featArr.length; i++) {
            features.put(i, featArr[i]);
        }
        this.label = label;
    }

    public double getFeature(int idx) {
        return features.getOrDefault(idx, Double.NaN);
    }

    public int getLabel() {
        return label;
    }

    public int getNumFeatures() {
        return features.size();
    }
}
