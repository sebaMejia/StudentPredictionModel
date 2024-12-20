package com.example;

import java.util.*;

// This class builds a single decision tree for classification.
public class DecisionTree {
    TreeNode root; // Root node of the decision tree
    private int maxFeatures; // Maximum number of features to consider when splitting
    private Random rand; // Random number generator for feature selection
    private double[] importances; // Array to store feature importance scores

    // Constructor to initialize the decision tree
    public DecisionTree(int maxFeatures, int totalFeatures) {
        this.maxFeatures = maxFeatures;
        this.rand = new Random();
        this.importances = new double[totalFeatures];
    }

    // Method to train the decision tree using the given dataset
    public void train(List<Node> data) {
        root = buildTree(data); // Build the tree recursively
    }

    // Method to predict the class label for a given set of features
    public int predict(double[] feats) {
        TreeNode node = root; // Start at the root node
        while (!(node instanceof LeafTreeNode)) { // Traverse until a leaf node is reached
            DecisionTreeNode dtn = (DecisionTreeNode) node;
            // Decide to go left or right based on the feature's threshold
            if (feats[dtn.featureIndex] <= dtn.threshold) {
                node = dtn.left;
            } else {
                node = dtn.right;
            }
        }
        return ((LeafTreeNode) node).label; // Return the class label at the leaf node
    }

    // Method to retrieve the importance scores of features
    public double[] getFeatureImportances() {
        return importances;
    }

    // Recursive method to build the decision tree
    private TreeNode buildTree(List<Node> data) {
        // If data is empty or pure, create a leaf node
        if (data.isEmpty() || isPure(data)) {
            int lbl = majorityLabel(data); // Determine the majority class label
            return new LeafTreeNode(lbl);
        }

        int nf = data.get(0).getNumFeatures(); // Number of features in the dataset
        int[] selectedFeatures = selectFeatures(nf, maxFeatures); // Select a subset of features
        Split best = findBestSplit(data, selectedFeatures); // Find the best split based on Gini impurity

        if (best == null || best.gain <= 0) { // If no good split is found, create a leaf node
            int lbl = majorityLabel(data);
            return new LeafTreeNode(lbl);
        }

        importances[best.featureIndex] += best.gain; // Update feature importance

        // Recursively build the left and right subtrees
        TreeNode left = buildTree(best.leftData);
        TreeNode right = buildTree(best.rightData);
        return new DecisionTreeNode(best.featureIndex, best.threshold, left, right);
    }

    // Helper method to check if all labels in the data are the same
    private boolean isPure(List<Node> data) {
        if (data.isEmpty()) return true;
        int first = data.get(0).getLabel();
        for (Node n : data) {
            if (n.getLabel() != first) return false;
        }
        return true;
    }

    // Helper method to determine the majority class label in the data
    private int majorityLabel(List<Node> data) {
        int[] counts = new int[2]; // Assuming binary classification (0 and 1)
        for (Node n : data) counts[n.getLabel()]++;
        return counts[0] > counts[1] ? 0 : 1;
    }

    // Helper method to randomly select a subset of features
    private int[] selectFeatures(int total, int maxF) {
        int[] arr = new int[total];
        for (int i = 0; i < total; i++) arr[i] = i; // Populate the array with feature indices
        shuffle(arr); // Shuffle the array randomly
        return Arrays.copyOfRange(arr, 0, maxF); // Return the first maxF features
    }

    // Helper method to shuffle an array randomly
    private void shuffle(int[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int idx = rand.nextInt(i + 1);
            int tmp = arr[idx];
            arr[idx] = arr[i];
            arr[i] = tmp;
        }
    }

    // Helper method to find the best split for the data based on Gini impurity
    private Split findBestSplit(List<Node> data, int[] feats) {
        double baseImp = gini(data); // Calculate the base Gini impurity
        Split best = null;
        for (int f : feats) { // Iterate over selected features
            double[] thresholds = data.stream().mapToDouble(d -> d.getFeature(f)).distinct().sorted().toArray();
            for (double t : thresholds) { // Iterate over unique thresholds for the feature
                List<Node> left = new ArrayList<>();
                List<Node> right = new ArrayList<>();
                for (Node d : data) {
                    if (d.getFeature(f) <= t) left.add(d);
                    else right.add(d);
                }
                if (left.isEmpty() || right.isEmpty()) continue; // Skip if split is invalid

                double newImp = weightedImpurity(left, right); // Calculate weighted impurity
                double gain = baseImp - newImp; // Calculate the information gain
                if (gain > 0 && (best == null || gain > best.gain)) {
                    best = new Split(f, t, left, right, gain); // Update the best split
                }
            }
        }
        return best;
    }

    // Helper method to calculate the Gini impurity of a dataset
    private double gini(List<Node> data) {
        int[] counts = new int[2];
        for (Node n : data) counts[n.getLabel()]++;
        double imp = 1.0;
        int tot = data.size();
        for (int c : counts) {
            double p = (double) c / tot;
            imp -= p * p; // Subtract the squared probability of each class
        }
        return imp;
    }

    // Helper method to calculate the weighted impurity of a split
    private double weightedImpurity(List<Node> l, List<Node> r) {
        int tot = l.size() + r.size();
        double li = gini(l);
        double ri = gini(r);
        return (l.size() * li + r.size() * ri) / tot; // Weighted average of left and right impurities
    }

    // Abstract base class for tree nodes
    private abstract class TreeNode {}

    // Class for decision nodes in the tree
    class DecisionTreeNode extends TreeNode {
        int featureIndex; // Feature index used for splitting
        double threshold; // Threshold value for splitting
        TreeNode left, right; // Left and right child nodes

        DecisionTreeNode(int f, double t, TreeNode L, TreeNode R) {
            featureIndex = f;
            threshold = t;
            left = L;
            right = R;
        }
    }

    // Class for leaf nodes in the tree
    class LeafTreeNode extends TreeNode {
        int label; // Class label at the leaf node

        LeafTreeNode(int l) {
            label = l;
        }
    }

    // Class to represent a potential split of the data
    private class Split {
        int featureIndex; // Feature used for the split
        double threshold; // Threshold value for the split
        List<Node> leftData, rightData; // Data for the left and right splits
        double gain; // Information gain of the split

        Split(int fi, double thr, List<Node> ld, List<Node> rd, double g) {
            featureIndex = fi;
            threshold = thr;
            leftData = ld;
            rightData = rd;
            gain = g;
        }
    }
}
