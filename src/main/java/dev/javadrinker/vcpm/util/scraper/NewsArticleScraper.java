package dev.javadrinker.vcpm.util.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class NewsArticleScraper {
    public static ArrayList<String> getArticleSegments(String url) {
        StringBuilder builder = scrapeArticle(url);

        String content = builder.toString()
                .replaceAll("\\s+([.,'\"!?])", "$1")
                .replaceAll("([\"'])\\s+", "$1");

        if (builder == null) {
            return new ArrayList<>();
        }
        if (builder.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder temp = new StringBuilder();
        ArrayList<String> finalSegments = new ArrayList<String>();

        for (String string : content.split("\n\n")) {
            String innerTemp = temp+"\n\n"+string;
            if (innerTemp.length() > 4096) {
                finalSegments.add(temp.toString());
                temp = new StringBuilder();
            }

            String lineBreaks = "\n\n";
            if (temp.toString().isEmpty()) { lineBreaks = "";}

            temp.append(lineBreaks).append(string);
        }

        if (!temp.toString().isEmpty()) {
            finalSegments.add(temp.toString());
        }

        return finalSegments;
    }

    private static StringBuilder scrapeArticle(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .get();

            Element articleBody = doc.selectFirst(".article-body");

            if (articleBody != null) {

                articleBody.select(".wf-hover-card, style, script").remove();

                Elements blocks = articleBody.select("p, li");

                StringBuilder cleanContent = new StringBuilder();

                for (Element block : blocks) {
                    String text = block.text().trim();

                    if (!text.isEmpty()) {
                        // If it's a list item, maybe add a bullet point for style
                        if (block.tagName().equals("li")) {
                            cleanContent.append("- ");
                        }

                        // Add the text and two line breaks for paragraph spacing
                        cleanContent.append(text).append("\n\n");
                    }
                }

                return cleanContent;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
