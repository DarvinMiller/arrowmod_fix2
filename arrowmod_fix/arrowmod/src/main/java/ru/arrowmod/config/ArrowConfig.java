package ru.arrowmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArrowConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("arrowmod.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean enabled = true;
    public boolean showDistance = true;
    public boolean showActivity = true;
    public String position = "TOP";
    public int closeDistance = 100;
    public int mediumDistance = 500;
    public int toggleKey = 75; // K
    public int friendsKey = 72; // H
    public int pingKey = 344; // right alt fallback; middle mouse is also handled by click
    public static ArrowConfig load() {
        try { if (Files.exists(FILE)) return GSON.fromJson(Files.readString(FILE), ArrowConfig.class); } catch (Exception ignored) {}
        return new ArrowConfig();
    }
    public void save() { try { Files.createDirectories(FILE.getParent()); Files.writeString(FILE, GSON.toJson(this)); } catch (Exception ignored) {} }
}
