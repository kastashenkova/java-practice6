package org.example.build_index;

import static org.example.build_index.Document.Zone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ZoneIndex {
    private static final double SMOOTHING_TERM = 0.4;
    private final Map<String, List<ZonePosition>> index = new TreeMap<>();
    private final Map<Integer, Map<Zone, Map<String, Integer>>> wordBags
            = new HashMap<>();
    private int totalDocs = 0;

    public Map<String, Double> buildVector(int docId,
                                           Map<Zone, Double> zoneWeights) {
        Set<String> vocabulary = index.keySet();
        Map<String, Double> vector = new HashMap<>();
        double normalized = normalizeScore(docId, zoneWeights);
        for (String term : vocabulary) {
            double score = 0.0;
            for (Zone z : Zone.values()) {
                score += zoneWeights.getOrDefault(z, 0.0)
                        * tfidf(docId, z, term);
            }
            if (score > 0) {
                vector.put(term, score / normalized);
            }
        }
        return vector;
    }

    // overloading
    public Map<String, Double> buildVector(String[] words) {
        Map<String, Double> vector = new HashMap<>();
        for (String w : words) {
            vector.merge(w, 1.0, Double::sum);
        }

        double normalized = 0;
        for (Map.Entry<String, Double> entry : vector.entrySet()) {
            double tfidf = entry.getValue() * idf(entry.getKey());
            entry.setValue(tfidf);
            normalized += tfidf * tfidf;
        }

        normalized = Math.sqrt(normalized);
        if (normalized > 0) {
            for (Map.Entry<String, Double> e : vector.entrySet()) {
                e.setValue(e.getValue() / normalized);
            }
        }

        return vector;
    }

    public void addDoc(Document doc) {
        totalDocs++;
        wordBags.put(doc.id, new EnumMap<>(Zone.class));
        int globalPos = 0;
        for (Zone z : Zone.values()) {
            List<String> tokens = doc.tokens(z);
            Map<String, Integer> zoneTf = new HashMap<>();
            for (int i = 0; i < tokens.size(); i++) {
                String term = tokens.get(i);
                index.computeIfAbsent(term, k -> new ArrayList<>())
                        .add(new ZonePosition(doc.id, globalPos + i, z));
                zoneTf.merge(term, 1, Integer::sum);
            }
            wordBags.get(doc.id).put(z, zoneTf);
            globalPos += tokens.size();
        }
    }

    public List<ZonePosition> postings(String term) {
        return index.getOrDefault(term, Collections.emptyList());
    }

    // term frequency tf(t,d) (t — term, d — doc)
    public int tf(int docId, Zone zone, String term) {
        return wordBags.getOrDefault(docId, Collections.emptyMap())
                .getOrDefault(zone, Collections.emptyMap())
                .getOrDefault(term, 0);
    }

    // document frequency df(i) (number of docs that have the term)
    public int df(String term) {
        return (int) postings(term).stream()
                .mapToInt(ZonePosition::docId)
                .distinct()
                .count();
    }

    // inverse document frequency(t) (t — term)
    public double idf(String term) {
        int df = df(term);
        if (df == 0) {
            return 0.0;
        }
        return Math.log((double) (totalDocs + 1) / (df + 1)) + 1.0;
    }

    // weighting scheme tf-idf(t,d) (t — term, d — document)
    public double tfidf(int docId, Zone zone, String term) {
        int tf = tf(docId, zone, term);
        if (tf == 0) {
            return 0.0;
        }
        double ntf = SMOOTHING_TERM + (1 - SMOOTHING_TERM)
                * (double) tf / tfMax(docId); // normalize term frequency
        return ntf * idf(term);
    }

    // Cosine score
    public double normalizeScore(int docId, Map<Zone, Double> weights) {
        Map<Zone, Map<String, Integer>> zoneMap = wordBags.get(docId);
        if (zoneMap == null) {
            return 1.0;
        }
        double sum = 0.0;
        for (Zone z : Zone.values()) {
            double zoneWeight = weights.getOrDefault(z, 0.0);
            Map<String, Integer> bag = zoneMap.getOrDefault(z, Map.of());
            for (String term : bag.keySet()) {
                double w = zoneWeight * tfidf(docId, z, term);
                sum += w * w;
            }
        }
        double norm = Math.sqrt(sum);
        return norm == 0.0 ? 1.0 : norm;
    }

    private int tfMax(int docId) {
        return wordBags.getOrDefault(docId, Map.of())
                .values().stream()
                .flatMap(bag -> bag.values().stream())
                .mapToInt(Integer::intValue)
                .max()
                .orElse(1);
    }
}
