package com.example.eventmap.util;

import java.nio.file.*;

public final class AppPaths {
    private AppPaths() {}
    public static final Path SAVE_DIR =
            Paths.get(System.getProperty("user.home"), "Documents", "EventMapSaves");

    public static Path ensureSaveDir() {
        try { Files.createDirectories(SAVE_DIR); } catch (Exception ignored) {}
        return SAVE_DIR;
    }
}
