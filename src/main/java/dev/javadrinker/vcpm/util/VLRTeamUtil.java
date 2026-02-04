package dev.javadrinker.vcpm.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public final class VLRTeamUtil {

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

    private static boolean LOADED = false;

    private VLRTeamUtil() {}

    /* ============================
       ===== PUBLIC API ===========
       ============================ */

    /** Loads all teams (paginated) once */
    public static synchronized void loadAllTeams()
            throws IOException, InterruptedException {

        if (LOADED) return;

        for (String region : regions) {

            int page = 0;
            boolean hasNext = true;

            System.out.println("Loading teams for region: " + region);

            while (hasNext) {
                ApiListResponse response = fetchPage(page, region);

                if (response.data != null) {
                    for (TeamSummary team : response.data) {
                        TEAM_ID_CACHE.put(team.id, team);
                        TEAM_NAME_CACHE.put(normalize(team.name), team);
                    }
                }

                hasNext = response.pagination != null
                        && response.pagination.hasNextPage;

                System.out.println(
                        "Paginating teams | region=" + region +
                                " page=" + page +
                                " next=" + hasNext
                );

                page++;
            }
        }

        LOADED = true;
    }


    /** Find a team by full name */
    public static TeamSummary getTeamByName(String name)
            throws IOException, InterruptedException {

        loadAllTeams();
        return TEAM_NAME_CACHE.get(normalize(name));
    }

    /** Find a team by ID */
    public static TeamSummary getTeamById(String id)
            throws IOException, InterruptedException {

        loadAllTeams();
        return TEAM_ID_CACHE.get(id);
    }

    /** Returns all teams */
    public static List<TeamSummary> getAllTeams()
            throws IOException, InterruptedException {

        loadAllTeams();
        return TEAM_ID_CACHE.values()
                .stream()
                .sorted(Comparator.comparing(t -> t.name))
                .collect(Collectors.toList());
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
