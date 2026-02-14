package dev.javadrinker.vcpm.util.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class MatchDataUtil {

    private static final String UPCOMING_URL =
            "https://vlrggapi.vercel.app/match?q=upcoming";

    private static final String LIVE_URL =
            "https://vlrggapi.vercel.app/match?q=live_score";

    private static final String RESULTS_URL =
            "https://vlrggapi.vercel.app/match?q=results";

    private static final Set<String> TIER_ONE_KEYWORDS =
            Set.of("VCT", "Masters", "Champions");

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();


    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MatchDataUtil() {}

    /* =========================
       ===== UPCOMING ==========
       ========================= */

    public static List<UpcomingMatch> getUpcomingMatches()
            throws IOException, InterruptedException {
        
        return fetch(UPCOMING_URL, UpcomingMatch.class)
                .data.segments;
    }

    public static List<UpcomingMatch> getUpcomingMatches(boolean tierOneOnly)
            throws IOException, InterruptedException {

        if (!tierOneOnly) return getUpcomingMatches();

        return getUpcomingMatches().stream()
                .filter(MatchDataUtil::isTierOneEvent)
                .collect(Collectors.toList());
    }

    /* =========================
       ===== LIVE ===============
       ========================= */

    public static List<LiveMatch> getLiveMatches()
            throws IOException, InterruptedException {

        return fetch(LIVE_URL, LiveMatch.class)
                .data.segments;
    }

    public static List<LiveMatch> getLiveMatches(boolean tierOneOnly)
            throws IOException, InterruptedException {

        if (!tierOneOnly) return getLiveMatches();

        return getLiveMatches().stream()
                .filter(MatchDataUtil::isTierOneEvent)
                .collect(Collectors.toList());
    }

    public static boolean isLiveMatchAvailable(boolean tierOneOnly) {
        try {
            return !getLiveMatches(tierOneOnly).isEmpty();
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /* =========================
       ===== RESULTS ============
       ========================= */

    public static List<PastMatch> getPastMatches()
            throws IOException, InterruptedException {

        return fetch(RESULTS_URL, PastMatch.class)
                .data.segments;
    }

    public static List<PastMatch> getPastMatches(boolean tierOneOnly)
            throws IOException, InterruptedException {

        if (!tierOneOnly) return getPastMatches();

        return getPastMatches().stream()
                .filter(MatchDataUtil::isTierOneEvent)
                .collect(Collectors.toList());
    }

    /* =========================
       ===== INTERNAL ===========
       ========================= */

    private static <T> ApiResponse<T> fetch(String url, Class<T> segmentType)
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
                        .constructParametricType(ApiResponse.class, segmentType)
        );
    }

    private static boolean isTierOneEvent(BaseMatch match) {
        String event = match.getEventName();
        if (event == null) return false;

        event = event.toUpperCase();
        return TIER_ONE_KEYWORDS.stream().anyMatch(event::contains);
    }

    /* =========================
       ===== MODELS =============
       ========================= */

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiResponse<T> {
        public Data<T> data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Data<T> {
        public int status;
        public List<T> segments;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static abstract class BaseMatch {

        public String team1;
        public String team2;

        // upcoming / live
        public String match_event;

        public String match_series;
        public String unix_timestamp;
        public String match_page;
        public String time_until_match;

        public String getMatchId() {
            if (match_page == null) return null;
            String[] parts = match_page.split("/");
            return parts[parts.length - 1];
        }

        /** Event name used for tier-one filtering */
        public String getEventName() {
            return match_event;
        }
    }

    /* =========================
       ===== UPCOMING ===========
       ========================= */

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpcomingMatch extends BaseMatch {
        public String flag1;
        public String flag2;
    }

    /* =========================
       ===== LIVE ===============
       ========================= */

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LiveMatch extends BaseMatch {

        public String flag1;
        public String flag2;

        public String team1_logo;
        public String team2_logo;

        public String score1;
        public String score2;

        public String team1_round_ct;
        public String team1_round_t;
        public String team2_round_ct;
        public String team2_round_t;

        public String map_number;
        public String current_map;

        /* ===== Derived Map Scores ===== */

        public int getTeam1MapScore() {
            return parseRoundScore(team1_round_ct, team1_round_t);
        }

        public int getTeam2MapScore() {
            return parseRoundScore(team2_round_ct, team2_round_t);
        }

        private int parseRoundScore(String ct, String t) {
            if (isValidNumber(ct)) return Integer.parseInt(ct);
            if (isValidNumber(t)) return Integer.parseInt(t);
            return 0;
        }

        private boolean isValidNumber(String value) {
            return value != null && !value.equalsIgnoreCase("N/A");
        }
    }

    /* =========================
       ===== RESULTS ============
       ========================= */

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PastMatch extends BaseMatch {

        public String score1;
        public String score2;

        public String flag1;
        public String flag2;

        public String time_completed;
        public String round_info;
        public String tournament_name;
        public String tournament_icon;
        public int page_number;

        @Override
        public String getEventName() {
            return tournament_name;
        }
    }
}
