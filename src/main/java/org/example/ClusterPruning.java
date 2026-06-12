package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ClusterPruning {
    private final static int B1 = 2; // number of leaders closest to the follower
    private final static int B2 = 3; // number of leaders closest to the query
    private final Map<Integer, Map<String, Double>> vectors;
    private final Map<Integer, List<Integer>> clusters = new LinkedHashMap<>();

    public ClusterPruning(Map<Integer, Map<String, Double>> vectors) {
        this.vectors = vectors;
    }

    // cluster pre-pruning (docs vectors clustering; offline)
    public void prePrune() {
        List<Integer> docIds = new ArrayList<>(vectors.keySet());
        int n = docIds.size();
        int countLeaders = (int) Math.sqrt(n);

        Collections.shuffle(docIds, new Random());
        Set<Integer> leaderIds = new LinkedHashSet<>(docIds.subList(0, countLeaders));
        for (int leaderId : leaderIds) {
            clusters.put(leaderId, new ArrayList<>(List.of(leaderId)));
        }

        for (int docId : docIds) {
            if (leaderIds.contains(docId)) {
                continue;
            }
            Set<Integer> copyLeaderIds = new LinkedHashSet<>(leaderIds);
            for (int i = 0; i < Math.min(B1, copyLeaderIds.size()); i++) {
                int closestLeader = findClosestLeader(docId, copyLeaderIds);
                clusters.get(closestLeader).add(docId);
                copyLeaderIds.remove(closestLeader);
            }
        }
    }

    // cluster pruning (online)
    public List<Integer> prune(Map<String, Double> query) {
        List<Integer> sortedLeaders = clusters.keySet()
                .stream()
                .sorted(Comparator.comparingDouble(
                        id -> -cosineSimilarity(query, vectors.get(id))))
                .toList();
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0;
             i < Math.min(sortedLeaders.size(), B2);
             i++) {
            candidates.addAll(clusters.get(sortedLeaders.get(i)));
        }

        return candidates.stream()
                .filter(id -> cosineSimilarity(query, vectors.get(id)) > 0.0)
                .sorted((a, b) -> Double.compare(
                        cosineSimilarity(query, vectors.get(b)),
                        cosineSimilarity(query, vectors.get(a))
                ))
                .toList();
    }

    private int findClosestLeader(int docId, Set<Integer> leaderIds) {
        Map<String, Double> docVec = vectors.get(docId);
        int bestDoc = -1;
        double bestSimilarity = -1;
        for (int leaderId : leaderIds) {
            double similarity = cosineSimilarity(docVec, vectors.get(leaderId));
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestDoc = leaderId;
            }
        }
        return bestDoc;
    }

    private double cosineSimilarity(Map<String, Double> docA,
                                    Map<String, Double> docB) {
        if (docA == null || docB == null) {
            return 0.0;
        }
        double res = 0.0; // dot product of vectors
        for (Map.Entry<String, Double> entry : docA.entrySet()) {
            res += entry.getValue() * docB.getOrDefault(
                    entry.getKey(), 0.0);
        }
        return res;
    }
}
