package com.meteoro.monorepomanager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ConfigManager {
    private static final String CONFIG_FILE_NAME = "pm.json";

    public static Config loadConfig(String directoryPath) throws IOException {
        File configFile = new File(directoryPath, CONFIG_FILE_NAME);
        if (!configFile.exists()) {
            throw new FileNotFoundException("Config file not found: " + configFile.getAbsolutePath());
        }

        try (Reader reader = new FileReader(configFile)) {
            Gson gson = new Gson();
            Type configType = new TypeToken<Config>() {}.getType();
            Config config = gson.fromJson(reader, configType);
            config.repositoryPath = resolvePath(directoryPath, config.repositoryPath);
            return config;
        }
    }

    public static void saveConfig(String directoryPath, Config config) throws IOException {
        File configFile = new File(directoryPath, CONFIG_FILE_NAME);
        try (Writer writer = new FileWriter(configFile)) {
            Gson gson = new Gson();
            gson.toJson(config, writer);
        }
    }

    private static String resolvePath(String basePath, String relativePath) {
        Path base = Paths.get(basePath);
        Path resolvedPath = base.resolve(relativePath).normalize();
        return resolvedPath.toAbsolutePath().toString();
    }

    public static class Config {
        public String repositoryPath;
        public List<String> files;
    }
}
