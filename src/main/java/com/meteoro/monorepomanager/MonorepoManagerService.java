package com.meteoro.monorepomanager;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public final class MonorepoManagerService {

    private final Project project;
    private ConfigManager.Config config;

    public MonorepoManagerService(Project project) {
        this.project = project;
    }

    public void initComponent() {
        // Инициализация компонента
        // Загрузка начальной конфигурации
        try {
            config = ConfigManager.loadConfig(project.getBasePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfiguration() {
        try {
            config = ConfigManager.loadConfig(project.getBasePath());
            // Обновление UI или других компонентов с новой конфигурацией
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfiguration(String selectedProject) {
        try {
            Path projectRoot = Paths.get(project.getBasePath());
            Path selectedProjectPath = Paths.get(config.repositoryPath, selectedProject);

            for (String filePath : config.files) {
                Path sourcePath = projectRoot.resolve(filePath);
                Path targetPath = selectedProjectPath.resolve(filePath);

                if (!Files.exists(targetPath.getParent())) {
                    Files.createDirectories(targetPath.getParent());
                }

                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void switchProject(String projectName) {
        // Логика переключения между проектами, возможно изменение config.files
    }

    public List<String> getConfiguredFiles() {
        return config.files;
    }

    public String getRepositoryPath() {
        return config.repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.config.repositoryPath = repositoryPath;
    }

    public List<String> getFilesInSelectedProject(String selectedProject) {
        try {
            Path projectPath = Paths.get(config.repositoryPath, selectedProject);
            if (Files.exists(projectPath) && Files.isDirectory(projectPath)) {
                return Files.list(projectPath)
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    public void copyFilesToProjectRoot(String selectedProject) throws IOException {
        Path projectRoot = Paths.get(project.getBasePath());
        Path selectedProjectPath = Paths.get(config.repositoryPath, selectedProject);

        if (Files.exists(selectedProjectPath) && Files.isDirectory(selectedProjectPath)) {
            Files.walkFileTree(selectedProjectPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = projectRoot.resolve(selectedProjectPath.relativize(file));
                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = projectRoot.resolve(selectedProjectPath.relativize(dir));
                    if (!Files.exists(targetPath)) {
                        Files.createDirectory(targetPath);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }



    public String checkConfiguredFilesInProject() {
        if (config == null || config.files == null || config.files.isEmpty()) {
            return "Configuration is empty or not set.";
        }

        StringBuilder missingFiles = new StringBuilder();
        for (String filePath : config.files) {
            Path path = Paths.get(project.getBasePath(), filePath);
            if (!Files.exists(path)) {
                if (missingFiles.length() > 0) {
                    missingFiles.append("\n");
                }
                missingFiles.append(filePath);
            }
        }

        if (missingFiles.length() > 0) {
            return "The following configured files are missing:\n" + missingFiles.toString();
        }

        return null; // No errors
    }




    public List<String> getProjects() {
        try {
            return Files.list(Paths.get(config.repositoryPath))
                    .filter(Files::isDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
