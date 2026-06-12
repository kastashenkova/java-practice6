package org.example.build_index;

import static org.example.build_index.Document.Zone;

public record ZonePosition(int docId, int position, Zone zone) {

    @Override
    public String toString() {
        return docId + "."
                + zone.name().toLowerCase()
                + "(" + position + ")";
    }
}
