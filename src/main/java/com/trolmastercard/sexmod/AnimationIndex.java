package com.trolmastercard.sexmod;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Server-side view of each character's animation file: clip names, lengths and loop modes.
 * GeckoLib only loads animations on the client, but scene timing is decided by the server.
 */
public final class AnimationIndex {
    public record Clip(String fullName, float seconds, boolean loops) {}

    private static final Map<GirlType, AnimationIndex> CACHE = new EnumMap<>(GirlType.class);
    private final Map<String, Clip> clips = new HashMap<>();

    private AnimationIndex(GirlType type) {
        try (InputStream stream = AnimationIndex.class.getResourceAsStream(type.animationResource())) {
            if (stream == null) {
                JennyMod.LOGGER.error("Missing animation file {}", type.animationResource());
                return;
            }
            JsonObject animations = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject("animations");
            for (Map.Entry<String, JsonElement> entry : animations.entrySet()) {
                JsonObject clip = entry.getValue().getAsJsonObject();
                float length = clip.has("animation_length") ? clip.get("animation_length").getAsFloat() : 0.0F;
                JsonElement loop = clip.get("loop");
                boolean loops = loop != null && loop.isJsonPrimitive() && loop.getAsJsonPrimitive().isBoolean() && loop.getAsBoolean();
                String shortName = entry.getKey().substring(entry.getKey().lastIndexOf('.') + 1).toLowerCase();
                this.clips.putIfAbsent(shortName, new Clip(entry.getKey(), length, loops));
            }
        } catch (Exception exception) {
            JennyMod.LOGGER.error("Could not read animation file {}", type.animationResource(), exception);
        }
    }

    public static synchronized AnimationIndex get(GirlType type) {
        return CACHE.computeIfAbsent(type, AnimationIndex::new);
    }

    public @Nullable Clip clip(String shortName) {
        return this.clips.get(shortName.toLowerCase());
    }

    public boolean has(String shortName) {
        return this.clips.containsKey(shortName.toLowerCase());
    }
}
