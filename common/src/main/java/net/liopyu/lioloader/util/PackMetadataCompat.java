package net.liopyu.lioloader.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class PackMetadataCompat {
    public static final String CLIENT_RESOURCES = "client_resources";

    private PackMetadataCompat() {
    }

    public static String buildPackMetadataJson(String packKind, String description) {
        int packFormat = resolvePackFormat(packKind);
        return """
                {
                  "pack": {
                    "pack_format": %d,
                    "description": "%s"
                  }
                }
                """.formatted(packFormat, description.replace("\\", "\\\\").replace("\"", "\\\""));
    }

    private static int resolvePackFormat(String packKind) {
        try (InputStream stream = PackMetadataCompat.class.getClassLoader().getResourceAsStream("version.json")) {
            if (stream == null) return 34;
            JsonElement root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            if (!root.isJsonObject()) return 34;
            JsonObject packVersion = getObject(root.getAsJsonObject(), "pack_version");
            if (packVersion == null) return 34;
            String key = "client_resources".equals(packKind) ? "resource" : "data";
            Integer value = readInt(packVersion, key);
            return value != null ? value : 34;
        } catch (Exception ignored) {
            return 34;
        }
    }

    private static JsonObject getObject(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return el != null && el.isJsonObject() ? el.getAsJsonObject() : null;
    }

    private static Integer readInt(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber() ? el.getAsInt() : null;
    }
}
