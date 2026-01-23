package net.liopyu.lioloader.pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.repository.PackRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class LioloaderPackLoadOrder {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Path lioloaderDir(Path gameDir) {
        return gameDir.resolve("lioloader");
    }

    public static Path datapackOrderFile(Path gameDir) {
        return lioloaderDir(gameDir).resolve("datapack_load_order.json");
    }

    public static Path resourcepackOrderFile(Path gameDir) {
        return lioloaderDir(gameDir).resolve("resourcepack_load_order.json");
    }

    public static List<String> readOrder(Path file) {
        try {
            if (!Files.exists(file)) {
                return List.of();
            }

            String raw = Files.readString(file, StandardCharsets.UTF_8).trim();
            if (raw.isEmpty()) {
                return List.of();
            }

            JsonElement root = JsonParser.parseString(raw);

            JsonElement orderEl = null;

            if (root.isJsonObject()) {
                if (root.getAsJsonObject().has("order")) {
                    orderEl = root.getAsJsonObject().get("order");
                } else {
                    LogUtils.getLogger().warn("[Lioloader] Load order file missing 'order' field: {}", file);
                    return List.of();
                }
            } else if (root.isJsonArray()) {
                orderEl = root;
            } else {
                LogUtils.getLogger().warn("[Lioloader] Load order file is not a JSON object or array: {}", file);
                return List.of();
            }

            if (orderEl == null || !orderEl.isJsonArray()) {
                LogUtils.getLogger().warn("[Lioloader] Load order 'order' field is not a JSON array: {}", file);
                return List.of();
            }

            ArrayList<String> out = new ArrayList<>();
            orderEl.getAsJsonArray().forEach(e -> {
                if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
                    String s = e.getAsString().trim();
                    if (!s.isEmpty()) out.add(s);
                }
            });

            return out;
        } catch (Throwable t) {
            LogUtils.getLogger().error("[Lioloader] Failed reading load order {}", file, t);
            return List.of();
        }
    }

    public static boolean applyOrder(PackRepository repo, List<String> preferredOrder) {
        if (preferredOrder == null || preferredOrder.isEmpty()) return false;

        java.util.HashMap<String, String> normalizedToActual = new java.util.HashMap<>();
        for (String id : repo.getAvailableIds()) {
            String norm = normalizePackKey(id);
            normalizedToActual.putIfAbsent(norm, id);
        }

        LinkedHashSet<String> selected = new LinkedHashSet<>();

        for (String raw : preferredOrder) {
            if (raw == null) continue;
            String key = normalizePackKey(raw);
            if (key.isEmpty()) continue;

            String actual = normalizedToActual.get(key);
            if (actual != null && repo.isAvailable(actual)) {
                selected.add(actual);
            } else if (repo.isAvailable(raw)) {
                selected.add(raw);
            } else {
                LogUtils.getLogger().info("[Lioloader] Load order references missing pack id='{}'", raw);
            }
        }

        for (String id : repo.getSelectedIds()) {
            if (repo.isAvailable(id)) selected.add(id);
        }

        if (selected.isEmpty() && repo.isAvailable("vanilla")) selected.add("vanilla");

        ArrayList<String> newSelected = new ArrayList<>(selected);
        if (newSelected.equals(new ArrayList<>(repo.getSelectedIds()))) return false;

        repo.setSelected(newSelected);
        LogUtils.getLogger().info("[Lioloader] Applied pack load order. selected={}", newSelected);
        return true;
    }

    private static String normalizePackKey(String s) {
        if (s == null) return "";
        String out = s.trim().toLowerCase(java.util.Locale.ROOT);
        if (out.endsWith(".zip")) out = out.substring(0, out.length() - 4);
        while (out.endsWith("/") || out.endsWith("\\")) out = out.substring(0, out.length() - 1).trim();
        return out;
    }

    public static void ensureOrderFiles(Path gameDir) {
        try {
            Path instanceDir = gameDir.resolve("lioloader");
            Files.createDirectories(instanceDir);

            Path datapacks = datapackOrderFile(gameDir);
            if (Files.notExists(datapacks)) {
                Files.writeString(datapacks, defaultDatapackOrderJson());
                LogUtils.getLogger().info("[Lioloader] Created {}", datapacks);
            }

            Path resourcepacks = resourcepackOrderFile(gameDir);
            if (Files.notExists(resourcepacks)) {
                Files.writeString(resourcepacks, defaultResourcepackOrderJson());
                LogUtils.getLogger().info("[Lioloader] Created {}", resourcepacks);
            }
        } catch (Throwable t) {
            LogUtils.getLogger().error("[Lioloader] Failed to ensure load order files", t);
        }
    }

    private static String defaultDatapackOrderJson() {
        return """
                {
                  "order": [
                    "highest_priority_example",
                    "second_priority_example",
                    "lowest_priority_example"
                  ]
                }
                """;
    }

    private static String defaultResourcepackOrderJson() {
        return """
                {
                  "order": [
                    "highest_priority_example",
                    "second_priority_example",
                    "lowest_priority_example"
                  ]
                }
                """;
    }

}
