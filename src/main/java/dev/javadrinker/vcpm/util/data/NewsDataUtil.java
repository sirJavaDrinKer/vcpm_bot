package dev.javadrinker.vcpm.util.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.javadrinker.vcpm.Main;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class NewsDataUtil {
    private static final String NEWS_URL =
            Main.VLRAPI_SELF+"v2/news";

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private NewsDataUtil() {}

    public static Article getLatestArticle()
            throws IOException, InterruptedException {

        List<Article> articles = getArticles();
        if (articles.isEmpty()) return null;

        return articles.get(0);
    }

    /* =========================
    ===== PUBLIC API ===========
    ==========================*/

    public static List<Article> getArticles()
            throws IOException, InterruptedException {

        return fetch(NEWS_URL, Article.class)
                .data.segments;
    }

    /* =========================
    ===== INTERNAL =============
    ========================= */

    private static <T> NewsDataUtil.ApiResponse<T> fetch(String url, Class<T> segmentType)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response =
                CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("VLR API returned status " + response.statusCode());
        }

        return MAPPER.readValue(
                response.body(),
                MAPPER.getTypeFactory()
                        .constructParametricType(NewsDataUtil.ApiResponse.class, segmentType)
        );
    }

    /* =========================
    ===== MODELS ===============
    ========================= */

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiResponse<T> {
        public String status;   // "success"
        public Data<T> data;
        public Object meta;
        public Object message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Data<T> {
        public int status;
        public List<T> segments;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Article {

        public String title;
        public String description;
        public String date;
        public String author;
        public String url_path;

        public String getArticleId() {
            if (url_path == null) return null;

            String[] parts = url_path.split("/");
            if (parts.length < 5) return null;

            return parts[3];
        }
    }

}
