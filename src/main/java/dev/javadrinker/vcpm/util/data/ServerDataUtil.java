package dev.javadrinker.vcpm.util.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class ServerDataUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final Path DATA_PATH = Path.of("data", "server_data.json");
    private static final String RESOURCE_PATH = "server_data.json";

    private static Map<String, ServerData> SERVER_DATA;

    private ServerDataUtil() {}

    public static void init() {
        if (SERVER_DATA != null) return;

        try {
            Files.createDirectories(DATA_PATH.getParent());

            if (!Files.exists(DATA_PATH)) {
                copyFromResources();
            }

            SERVER_DATA = MAPPER.readValue(
                    DATA_PATH.toFile(),
                    MAPPER.getTypeFactory().constructMapType(
                            Map.class,
                            String.class,
                            ServerData.class
                    )
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize server data", e);
        }
    }

    private static void copyFromResources() throws IOException {
        try (InputStream is = ServerDataUtil.class
                .getClassLoader()
                .getResourceAsStream(RESOURCE_PATH)) {

            if (is == null) {
                SERVER_DATA = new HashMap<>();
                save();
                return;
            }

            Files.copy(is, DATA_PATH);
        }
    }

    /* =========================
       ===== SAVE ==============
       ========================= */

    public static synchronized void save() {
        try {
            MAPPER.writeValue(DATA_PATH.toFile(), SERVER_DATA);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save server data", e);
        }
    }

    /* =========================
       ===== ACCESS ============
       ========================= */

    public static Map<String, ServerData> getAllServers() {
        init();
        return SERVER_DATA;
    }

    public static ServerData getOrCreateServer(String serverId) {
        init();
        return SERVER_DATA.computeIfAbsent(serverId, id -> new ServerData());
    }

    public static void removeServer(String serverId) {
        init();
        SERVER_DATA.remove(serverId);
        save();
    }

    /* =========================
       ========= OTHER =========
       ========================= */

    public static void toggleDeveloperMode(String serverId) {
        System.out.println("toggling dev mode for server: " + serverId);
        ServerData server = getOrCreateServer(serverId);
        server.developer_mode = !server.developer_mode;
        save();
    }

    public static boolean isDeveloperMode(String serverId) {
        ServerData server = getOrCreateServer(serverId);
        return server.developer_mode;
    };

    public static void setDeveloperMode(String serverId, boolean mode) {
        ServerData server = getOrCreateServer(serverId);
        server.developer_mode = mode;
        save();
    }

    public static void setPredictionsEnabled(String serverId, boolean enabled) {
        ServerData server = getOrCreateServer(serverId);
        server.predictions_enabled = enabled;
        save();
    }

    public static Boolean getPredictionsEnabled(String serverId) {
        return ServerDataUtil.getOrCreateServer(serverId).predictions_enabled;
    }

    /* =========================
       ===== CHANNELS ==========
       ========================= */

    public static void setPredictionChannel(String serverId, String channelId) {
        ServerData server = getOrCreateServer(serverId);
        server.prediction_channel = channelId;
        save();
    }

    public static void setReminderChannel(String serverId, String channelId) {
        ServerData server = getOrCreateServer(serverId);
        server.reminder_channel = channelId;
        save();
    }

    public static void setNewsChannel(String serverId, String channelId) {
        ServerData server = getOrCreateServer(serverId);
        server.news_channel = channelId;
        save();
    }

    /* =========================
       ===== NEWS ==============
       ========================= */

    public static void setLatestNewsId(String serverId, String newsId) {
        ServerData server = getOrCreateServer(serverId);
        server.latest_news_id = newsId;
        save();
    }

    public static void setNewsEnabled(String serverId, boolean enabled) {
        ServerData server = getOrCreateServer(serverId);
        server.news_enabled = enabled;
        save();
    }

    public static boolean getNewsEnabled(String serverId) {
        ServerData server = getOrCreateServer(serverId);
        return server.news_enabled;
    }

    public static List<String> getNewsIds(String serverId) {
        ServerData server = getOrCreateServer(serverId);
        return server.news_ids;
    }

    public static void setNewsIds(String serverId, List<String> newsIds) {
        ServerData server = getOrCreateServer(serverId);
        server.news_ids = newsIds;
        save();
    }

    /* =========================
       ===== PREDICTIONS =======
       ========================= */

    public static void setPredictionMessage(
            String serverId,
            String gameId,
            String messageId
    ) {
        ServerData server = getOrCreateServer(serverId);
        server.prediction_ids.put(gameId, messageId);
        save();
    }

    public static void removePrediction(
            String serverId,
            String gameId
    ) {
        ServerData server = getOrCreateServer(serverId);
        server.prediction_ids.remove(gameId);
        save();
    }

    /* =========================
       ===== REMINDERS ========
       ========================= */

    public static void setReminderMessage(
            String serverId,
            String gameId,
            String messageId
    ) {
        ServerData server = getOrCreateServer(serverId);
        server.reminder_ids.put(gameId, messageId);
        save();
    }

    public static void removeReminder(
            String serverId,
            String gameId
    ) {
        ServerData server = getOrCreateServer(serverId);
        server.reminder_ids.remove(gameId);
        save();
    }

    public static void setRemindersEnabled(String serverId, boolean enabled) {
        ServerData server = getOrCreateServer(serverId);
        server.reminders_enabled = enabled;
        save();
    }

    public static boolean getRemindersEnabled(String serverId) {
        ServerData server = getOrCreateServer(serverId);
        return server.reminders_enabled;
    }

    /* =========================
       ===== USER DATA =========
       ========================= */

    public static String zeroOutAllUserData(String serverId) {
        ServerData server = getOrCreateServer(serverId);
        String restoreCode = generateRestoreCode(serverId);
        server.user_data.values().forEach(user -> {
            user.score = 0;
            user.total_predictions_made = 0;
        });
        save();
        return restoreCode;
    }

    public static String resetAllUserData(String serverId) {
        ServerData server = getOrCreateServer(serverId);
        String restoreCode = generateRestoreCode(serverId);
        // Clearing the map removes all users from this server's JSON node
        server.user_data.clear();
        save();
        return restoreCode;
    }

    public static UserData getOrCreateUser(
            String serverId,
            String userId
    ) {
        ServerData server = getOrCreateServer(serverId);
        return server.user_data.computeIfAbsent(userId, id -> new UserData());
    }

    public static int getUserScore(String serverId, String userId) {
        return getOrCreateUser(serverId, userId).score;
    }

    public static int getUserTotalPredictions(
            String serverId,
            String userId
    ) {
        return getOrCreateUser(serverId, userId).total_predictions_made;
    }

    public static void setUserScore(
            String serverId,
            String userId,
            int score
    ) {
        getOrCreateUser(serverId, userId).score = score;
        save();
    }

    public static void addUserScore(
            String serverId,
            String userId,
            int delta
    ) {
        getOrCreateUser(serverId, userId).score += delta;
        save();
    }

    public static void incrementPredictionsMade(
            String serverId,
            String userId
    ) {
        getOrCreateUser(serverId, userId).total_predictions_made++;
        save();
    }

    /* =========================
       ===== BACKUP/RESTORE ====
       ========================= */

    /**
     * Generates a Base64 encoded JSON string representing the server's current data.
     * This acts as a single-string "restore code".
     */
    public static String generateRestoreCode(String serverId) {
        ServerData server = getOrCreateServer(serverId);
        try {
            // Convert the ServerData object to a JSON string
            String json = MAPPER.writeValueAsString(server);

            // Encode to Base64 for easy copy/pasting
            return Base64.getEncoder().encodeToString(json.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate restore code for server: " + serverId, e);
        }
    }

    /**
     * Restores server data from a Base64 encoded JSON string.
     * * @return true if successful, false if the code was invalid or corrupted.
     */
    public static boolean restoreFromCode(String serverId, String restoreCode) {
        init(); // Ensure map is initialized
        try {
            // Decode the Base64 string back into raw JSON
            byte[] decodedBytes = Base64.getDecoder().decode(restoreCode);
            String json = new String(decodedBytes);

            // Parse the JSON back into a ServerData object
            ServerData restoredData = MAPPER.readValue(json, ServerData.class);

            // Overwrite the current server data in the map
            SERVER_DATA.put(serverId, restoredData);

            // Save the updated map to the main server_data.json file
            save();
            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Base64 restore code provided for server: " + serverId);
            return false;
        } catch (Exception e) {
            System.err.println("Failed to parse restored data for server: " + serverId);
            e.printStackTrace();
            return false;
        }
    }

    /* =========================
       ===== MODELS ============
       ========================= */

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServerData {
        public String prediction_channel;
        public String reminder_channel;
        public String news_channel;

        public Map<String, String> prediction_ids = new HashMap<>();
        public Map<String, UserData> user_data = new HashMap<>();
        public Map<String, String> reminder_ids = new HashMap<>();
        public boolean developer_mode;
        public boolean predictions_enabled;
        public boolean reminders_enabled;
        public boolean news_enabled;
        public String latest_news_id;

        public List<String> news_ids = new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserData {
        public int score = 0;
        public int total_predictions_made = 0;
    }
}
