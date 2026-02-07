package dev.javadrinker.vcpm.util.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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
       ===== USER DATA =========
       ========================= */

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
       ===== MODELS ============
       ========================= */

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServerData {
        public String prediction_channel;
        public String reminder_channel;

        public Map<String, String> prediction_ids = new HashMap<>();
        public Map<String, UserData> user_data = new HashMap<>();
        public boolean developer_mode;
        public boolean predictions_enabled;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserData {
        public int score = 0;
        public int total_predictions_made = 0;
    }
}
