package org.example.build_index;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Fb2Parser {

    public static Document parse(int id, String filePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // security
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",
                true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities",
                false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities",
                false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document xml = builder.parse(new File(filePath));
        xml.getDocumentElement().normalize();

        Element titleInfo = findElement(xml, "title-info");
        Element docInfo = findElement(xml, "document-info");

        String author = extractAuthors(titleInfo);
        String title = textOf(titleInfo, "book-title");
        String generator = extractNickname(docInfo);
        String genre = textOf(titleInfo, "genre");
        String language = textOf(titleInfo, "lang");
        String version = textOf(docInfo, "version");
        LocalDate date = parseDate(textOf(docInfo, "date"));
        String bodyText = extractBody(xml);
        String sectionTitle = extractSectionTitle(
                findElement(xml, "body"));

        return new Document(id,
                filePath,
                author,
                title,
                generator,
                bodyText,
                sectionTitle,
                genre,
                language,
                date,
                version);
    }

    public static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            // ignore
        }

        try {
            String[] parts = s.split("-");
            if (parts.length >= 2) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                return LocalDate.of(year, month, 1);
            }
        } catch (Exception e) {
            // ignore
        }

        try {
            int year = Integer.parseInt(s);
            return LocalDate.of(year, 1, 1);
        } catch (NumberFormatException e) {
            // ignore
        }

        return null;
    }

    private static Element findElement(org.w3c.dom.Document xml,
                                       String tag) {
        NodeList list = xml.getElementsByTagNameNS("*", tag);
        if (list.getLength() == 0) {
            list = xml.getElementsByTagName(tag);
        }
        return list.getLength() > 0 ? (Element) list.item(0) : null;
    }

    private static String textOf(Element element, String tag) {
        if (element == null) {
            return "";
        }
        NodeList list = element.getElementsByTagNameNS("*", tag);
        if (list.getLength() == 0) {
            list = element.getElementsByTagName(tag);
        }
        if (list.getLength() > 0) {
            return list.item(0).getTextContent().trim();
        }
        return "";
    }

    private static String extractAuthors(Element titleInfo) {
        if (titleInfo == null) {
            return "";
        }

        // * — ignore namespace
        NodeList authors = titleInfo
                .getElementsByTagNameNS("*", "author");

        if (authors.getLength() == 0) {
            authors = titleInfo.getElementsByTagName("author");
        }

        List<String> names = new ArrayList<>();
        for (int i = 0; i < authors.getLength(); i++) {
            Node author = authors.item(i);
            if (author.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) author;
            String first = textOf(element, "first-name");
            String middle = textOf(element, "middle-name");
            String last = textOf(element, "last-name");
            String full = (first + " " + middle + " " + last).trim()
                    .replaceAll("\\s+", " ");
            if (!full.isBlank()) {
                names.add(full);
            }
        }
        return String.join(", ", names);
    }

    private static String extractNickname(Element docInfo) {
        if (docInfo == null) {
            return "";
        }
        NodeList authors = docInfo.getElementsByTagNameNS("*", "author");
        if (authors.getLength() == 0) {
            authors = docInfo.getElementsByTagName("author");
        }
        if (authors.getLength() == 0) {
            return "";
        }
        List<String> nicknames = new ArrayList<>();
        for (int i = 0; i < authors.getLength(); i++) {
            Node nickname = authors.item(i);
            if (nickname.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) nickname;
            String word = textOf(element, "nickname");
            if (!word.isBlank()) {
                nicknames.add(word);
            }
        }
        return String.join(", ", nicknames);
    }

    private static String extractSectionTitle(Node bodyNode) {
        if (bodyNode == null) {
            return "";
        }
        NodeList titles = ((Element) bodyNode).getElementsByTagNameNS(
                "*", "title");
        if (titles.getLength() == 0) {
            titles = ((Element) bodyNode).getElementsByTagName("title");
        }
        if (titles.getLength() > 0) {
            return titles.item(0).getTextContent().trim();
        }
        return "";
    }

    private static String extractBody(org.w3c.dom.Document xml) {
        NodeList bodies = xml.getElementsByTagNameNS("*", "body");
        if (bodies.getLength() == 0) {
            bodies = xml.getElementsByTagName("body");
        }
        if (bodies.getLength() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bodies.getLength(); i++) {
            collect(bodies.item(i), sb);
        }
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    // recursive
    private static void collect(Node node, StringBuilder sb) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            String text = node.getTextContent().trim();
            if (!text.isEmpty()) sb.append(text).append(" ");
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            collect(children.item(i), sb);
        }
    }

    public static List<Document> loadDir(String dirPath) {
        List<Document> docs = new ArrayList<>();
        File dir = new File(dirPath);
        File[] files = dir.listFiles((d, name) -> name.toLowerCase()
                .endsWith(".fb2"));
        if (files == null || files.length == 0) {
            throw new RuntimeException("No .fb2 files found in " + dirPath);
        }
        Arrays.sort(files, Comparator.comparing(File::getName));
        int id = 1;
        for (File f : files) {
            try {
                Document doc = parse(id++, f.getAbsolutePath());
                docs.add(doc);
            } catch (Exception e) {
                System.err.println("Failed to parse "
                        + f.getAbsolutePath()
                        + ": " + e.getMessage());
            }
        }
        return docs;
    }
}