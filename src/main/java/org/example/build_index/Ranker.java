package org.example.build_index;

import static org.example.build_index.Document.Zone;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Ranker {

    public static final Map<Zone, Double> WEIGHTS = new EnumMap<>(Zone.class);

    /*  Weights distribution
        BODY           ███████████████ 40 %
        AUTHOR         ████████ 22.5 %
        TITLE          ████████ 22.5 %
        SECTION_TITLE  ████ 5 %
        GENERATOR      ░ 2.5 %
        GENRE          ░ 2.5 %
        LANGUAGE       ░ 2.5 %
        DATE           ░ 2.5 %  */

    static {
        WEIGHTS.put(Zone.AUTHOR, 0.225);
        WEIGHTS.put(Zone.TITLE, 0.225);
        WEIGHTS.put(Zone.GENERATOR, 0.025);
        WEIGHTS.put(Zone.BODY, 0.4);
        WEIGHTS.put(Zone.SECTION_TITLE, 0.05);
        WEIGHTS.put(Zone.GENRE, 0.025);
        WEIGHTS.put(Zone.LANGUAGE, 0.025);
        WEIGHTS.put(Zone.DATE, 0.025);
    }

    public static double zoneScore(int docId, Zone zone,
                                   List<String> queryTerms,
                                   ZoneIndex zoneIndex) {
        double score = 0.0;
        for (String term : queryTerms) {
            score += zoneIndex.tfidf(docId, zone, term);
        }
        return score;
    }

    // final importance ranking
    public static double weightedSum(int docId,
                                     List<String> queryTerms,
                                     ZoneIndex zoneIndex) {
        double score = 0.0;
        for (Zone z : Zone.values()) {
            score += WEIGHTS.getOrDefault(z, 0.0)
                    * zoneScore(docId, z, queryTerms, zoneIndex);
        }
        return score;
    }
}
