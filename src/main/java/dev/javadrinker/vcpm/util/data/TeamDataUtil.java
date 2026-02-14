package dev.javadrinker.vcpm.util.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class TeamDataUtil {

    private static final String BASE_URL =
            "https://vlr.orlandomm.net/api/v1/teams";

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final List<String> regions = List.of(
      "all",
              "na",
              "eu",
              "br" ,
              "ap" ,
              "asia" ,
              "pacific" ,
              "kr" ,
              "ch" ,
              "jp" ,
              "las" ,
              "la-s" ,
              "lan" ,
              "la-n" ,
              "oce" ,
              "oceania" ,
              "mena" ,
              "gc" ,
              "world"
    );

    public static boolean isLOADED() {
        return LOADED;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Cache: normalized name -> team
    private static final Map<String, TeamSummary> TEAM_NAME_CACHE = new HashMap<>();
    private static final Map<String, TeamSummary> TEAM_ID_CACHE = new HashMap<>();

    private static final Path CACHE_FILE =
            Path.of("data","cached_teams.json");

    private static volatile boolean LOADING = false;
    private static boolean LOADED = false;

    private TeamDataUtil() {}

    /* ============================
       ===== PUBLIC API ===========
       ============================ */

    /** Loads all teams (paginated) once */
    public static synchronized CompletableFuture<Void> loadAllTeamsAsync() {

        if (LOADING) {
            return CompletableFuture.completedFuture(null);
        }

        // Load cached data immediately
        if (!LOADED) {
            loadFromDisk();
        }

        LOADING = true;

        return CompletableFuture.runAsync(() -> {
            try {
                TEAM_ID_CACHE.clear();
                TEAM_NAME_CACHE.clear();

                for (String region : regions) {

                    int page = 0;
                    boolean hasNext = true;

                    System.out.println("Refreshing teams for region: " + region);

                    while (hasNext) {
                        ApiListResponse response = fetchPage(page, region);

                        if (response.data != null) {
                            for (TeamSummary team : response.data) {
                                TEAM_ID_CACHE.put(team.id, team);
                                TEAM_NAME_CACHE.put(
                                        normalize(team.name), team
                                );
                            }
                        }

                        hasNext = response.pagination != null
                                && response.pagination.hasNextPage;

                        page++;
                    }
                }

                LOADED = true;
                writeToDisk();

            } catch (Exception e) {
                System.err.println("Team refresh failed: " + e.getMessage());
            } finally {
                LOADING = false;
            }
        });
    }

    public static TeamSummary getTeamByName(String name) {
        if (!LOADED) loadFromDisk();
        return TEAM_NAME_CACHE.get(normalize(name));
    }

    public static TeamSummary getTeamById(String id) {
        if (!LOADED) loadFromDisk();
        return TEAM_ID_CACHE.get(id);
    }

    public static List<TeamSummary> getAllTeams() {
        if (!LOADED) loadFromDisk();
        return TEAM_ID_CACHE.values()
                .stream()
                .sorted(Comparator.comparing(t -> t.name))
                .collect(Collectors.toList());
    }

    public static Boolean isFileCacheEmpty() {
        if (!Files.exists(CACHE_FILE)) {
            return true;
        }
        if (TEAM_ID_CACHE.isEmpty()) {
            return true;
        }
        return !LOADED;
    }

    /** Fetch detailed info for a single team */
    public static TeamDetails getTeamDetails(String teamId)
            throws IOException, InterruptedException {

        String url = BASE_URL + "/" + teamId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response =
                CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        ApiSingleResponse parsed =
                MAPPER.readValue(response.body(), ApiSingleResponse.class);

        return parsed.data;
    }

    /* ============================
       ===== INTERNAL ============
       ============================ */

    private static void loadFromDisk() {
        if (!Files.exists(CACHE_FILE)) return;

        try {
            List<TeamSummary> teams = Arrays.asList(
                    MAPPER.readValue(
                            Files.readString(CACHE_FILE),
                            TeamSummary[].class
                    )
            );

            for (TeamSummary team : teams) {
                TEAM_ID_CACHE.put(team.id, team);
                TEAM_NAME_CACHE.put(normalize(team.name), team);
            }

            LOADED = true;
            System.out.println("Loaded teams from cached_teams.json");

        } catch (IOException e) {
            System.err.println("Failed to load cached teams: " + e.getMessage());
        }
    }

    private static void writeToDisk() {
        try {
            List<TeamSummary> teams =
                    new ArrayList<>(TEAM_ID_CACHE.values());

            Files.writeString(
                    CACHE_FILE,
                    MAPPER.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(teams)
            );

            System.out.println("Wrote cached_teams.json");

        } catch (IOException e) {
            System.err.println("Failed to write cached teams: " + e.getMessage());
        }
    }


    private static ApiListResponse fetchPage(int page, String region)
            throws IOException, InterruptedException {

        String url = BASE_URL +
                "?region="+ region +
                "&page=" + page+
                "&limit=100";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response =
                CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Team API returned " + response.statusCode());
        }

        return MAPPER.readValue(response.body(), ApiListResponse.class);
    }

    private static String normalize(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }

    /* ============================
       ===== MODELS ===============
       ============================ */

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiListResponse {
        public String status;
        public String region;
        public int size;
        public Pagination pagination;
        public List<TeamSummary> data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiSingleResponse {
        public String status;
        public TeamDetails data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Pagination {
        public int page;
        public int limit;
        public int totalElements;
        public int totalPages;
        public boolean hasNextPage;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeamSummary {
        public String id;
        public String url;
        public String name;
        public String img;
        public String country;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeamDetails {
        public TeamInfo info;
        public List<Player> players;
        public List<Staff> staff;
        public List<MatchSummary> results;
        public List<MatchSummary> upcoming;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeamInfo {
        public String name;
        public String tag;
        public String logo;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Player {
        public String id;
        public String name;
        public String user;
        public String img;
        public String country;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Staff {
        public String id;
        public String name;
        public String tag;
        public String img;
        public String country;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MatchSummary {
        public String id;
        public String url;
    }
}
