package com.example;

import java.util.*;
import java.util.concurrent.*;

public class RandomForest {
    List<DecisionTree> trees;
    private int numTrees;
    private int maxFeatures;
    private int numTotalFeatures;
    private Random rand;

    public RandomForest(int numTrees, int maxFeatures, int totalF) {
        this.numTrees = numTrees;
        this.maxFeatures = maxFeatures;
        this.numTotalFeatures = totalF;
        this.trees = Collections.synchronizedList(new ArrayList<>());
        this.rand = new Random();
    }

    public void train(List<Node> data) {
        ExecutorService exec = Executors.newFixedThreadPool(
            Math.min(Runtime.getRuntime().availableProcessors(), numTrees)
        );
        
        CountDownLatch latch = new CountDownLatch(numTrees);
        
        for (int i = 0; i < numTrees; i++) {
            exec.execute(() -> {
                try {
                    List<Node> sample = bootstrapSample(data);
                    DecisionTree dt = new DecisionTree(maxFeatures, numTotalFeatures);
                    dt.train(sample);
                    trees.add(dt);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        exec.shutdown();
        try {
            // Wait for completion with timeout
            if (!latch.await(5, TimeUnit.MINUTES)) {
                exec.shutdownNow();
            }
        } catch (InterruptedException e) {
            exec.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private List<Node> bootstrapSample(List<Node> data) {
        List<Node> samp = new ArrayList<>();
        int N = Math.min(data.size(), 10000);
        
        // Count classes for balance
        int positiveCount = 0;
        for (Node n : data) {
            if (n.getLabel() == 1) positiveCount++;
        }
        int negativeCount = data.size() - positiveCount;
        
        // Maintain rough class balance
        int samplesPerClass = N / 2;
        int currentPositive = 0, currentNegative = 0;
        
        while (samp.size() < N) {
            int idx = rand.nextInt(data.size());
            Node node = data.get(idx);
            
            if (node.getLabel() == 1 && currentPositive < samplesPerClass) {
                samp.add(node);
                currentPositive++;
            } else if (node.getLabel() == 0 && currentNegative < samplesPerClass) {
                samp.add(node);
                currentNegative++;
            }
        }
        
        return samp;
    }

    public int predict(double[] feats) {
        int[] votes = new int[2];
        for (DecisionTree t : trees) {
            int p = t.predict(feats);
            votes[p]++;
        }
        return votes[0] > votes[1] ? 0 : 1;
    }

    public double evaluate(List<Node> test) {
        int correct = 0;
        for (Node n : test) {
            int pred = predict(nodeToArr(n));
            if (pred == n.getLabel()) correct++;
        }
        return (double) correct / test.size();
    }

    private double[] nodeToArr(Node n) {
        double[] arr = new double[n.getNumFeatures()];
        for (int i = 0; i < n.getNumFeatures(); i++) {
            arr[i] = n.getFeature(i);
        }
        return arr;
    }
}