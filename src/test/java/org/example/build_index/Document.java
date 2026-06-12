package org.example.build_index;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Document {
    public final int id;
    public final String filePath;
    public final Map<Zone, String> zones = new EnumMap<>(Zone.class);

    // metadata
    public final Map<String, LocalDate> dateField = new LinkedHashMap<>();
    public final Map<String, String> fields = new LinkedHashMap<>();

    public final String bodyText;

    public Document(int id,
                    String filePath,
                    String author,
                    String title,
                    String generator,
                    String bodyText,
                    String sectionTitle,
                    String genre,
                    String language,
                    LocalDate date,
                    String version) {
        this.id = id;
        this.filePath = filePath;
        this.bodyText = safeLower(bodyText);

        zones.put(Zone.AUTHOR, safeLower(author));
        zones.put(Zone.TITLE, safeLower(title));
        zones.put(Zone.GENERATOR, safeLower(generator));
        zones.put(Zone.BODY, safeLower(bodyText));
        zones.put(Zone.SECTION_TITLE, safeLower(sectionTitle));
        zones.put(Zone.GENRE, safeLower(genre));
        zones.put(Zone.LANGUAGE, safeLower(language));
        zones.put(Zone.DATE, date != null ? date.toString() : "");

        dateField.put("date", date);
        fields.put("language", safeLower(language));
        fields.put("genre", safeLower(genre));
        fields.put("version", version);
    }

    // to avoid NullPointerException
    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    public List<String> tokens(Zone z) {
        String text = zones.getOrDefault(z, "");
        List<String> result = new ArrayList<>();
        for (String t : text.split("\\W+")) {
            if (!t.isEmpty()) {
                result.add(t);
            }
        }
        return result;
    }

    public enum Zone {
        AUTHOR,
        TITLE,
        GENERATOR,
        BODY,
        SECTION_TITLE,
        GENRE,
        LANGUAGE,
        DATE
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Document " + id + ": " + zones.get(Zone.TITLE)
                + ", " + zones.get(Zone.AUTHOR);
    }
}
