package com.meteoro.monorepomanager;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;


public class MonorepoManagerToolWindow {
    private JPanel contentPanel;
    private JComboBox<String> projectSelector;
    private JButton loadButton;
    private JButton saveButton;
    private JButton refreshButton;
    private JButton instructionsButton;



    private final MonorepoManagerService service;

    public MonorepoManagerToolWindow(Project project) {
        // Создание панели и компонентов


        contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setPreferredSize(new Dimension(300, 200));
        GridBagConstraints gbc = new GridBagConstraints();

        projectSelector = new JComboBox<>();
        loadButton = new JButton("Load");
        saveButton = new JButton("Save");
        refreshButton = createRefreshButton();
        instructionsButton = new JButton("Инструкция по использованию");


        // Получение сервиса
        service = project.getService(MonorepoManagerService.class);

        // Установка параметров для компонента instructionsButton
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(10);
        contentPanel.add(instructionsButton, gbc);

        // Установка параметров для компонента projectSelector
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(projectSelector, gbc);


        // Установка параметров для компонента refreshButton
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(refreshButton, gbc);

        // Установка параметров для компонента loadButton
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(loadButton, gbc);

        // Установка параметров для компонента saveButton
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(saveButton, gbc);

        // Обработка событий
        loadButton.addActionListener(e -> {
            // Логика загрузки репозитория
            loadRepository();
        });

        saveButton.addActionListener(e -> {
            // Логика сохранения репозитория
            saveRepository();
        });

        refreshButton.addActionListener(e -> {
            // Логика обновления проектов
            loadProjects();
        });

        instructionsButton.addActionListener(e -> {
            // Открытие окна с инструкцией
            showInstructions();
        });


        // Пример заполнения селектора проектов
        projectSelector.addItem("Project A");
        projectSelector.addItem("Project B");

        // Инициализация начальной конфигурации
        initConfig();
    }

    private JButton createRefreshButton() {
        URL iconURL = getClass().getResource("/icons/refresh.svg");
        ImageIcon icon = null;
        if (iconURL != null) {
            icon = new ImageIcon(iconURL);
        } else {
            System.err.println("Icon not found, using default text");
        }
        JButton button = new JButton("Refresh");
        return button;
    }

    private void initConfig() {
        service.initComponent();
        loadProjects();
    }

    public JPanel getContent() {
        return contentPanel;
    }

    private void loadRepository() {
        service.loadConfiguration();

        String selectedProject = (String) projectSelector.getSelectedItem();
        if (selectedProject != null && !selectedProject.equals("No projects available")) {
            String errorMessage = service.checkConfiguredFilesInProject();
            if (errorMessage != null) {
                JOptionPane.showMessageDialog(contentPanel, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    System.out.println("Selected project: " + selectedProject);
                    service.copyFilesToProjectRoot(selectedProject);
                    JOptionPane.showMessageDialog(contentPanel, "Files and folders have been copied to the project root.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(contentPanel, "An error occurred while copying files.", "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("No project selected or no projects available");
        }
        System.out.println("Loading repository...");
    }


    private void saveRepository() {
        String selectedProject = (String) projectSelector.getSelectedItem();
        if (selectedProject != null && !selectedProject.equals("No projects available")) {
            service.saveConfiguration(selectedProject);
            System.out.println("Saving repository for project: " + selectedProject);
            JOptionPane.showMessageDialog(contentPanel, "Configuration has been saved for the selected project.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println("No project selected or no projects available");
            JOptionPane.showMessageDialog(contentPanel, "No project selected or no projects available.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void loadProjects() {
        List<String> projects = service.getProjects();
        updateProjectSelector(projects);
    }


    private void updateProjectSelector(List<String> projects) {
        projectSelector.removeAllItems();
        if (projects == null || projects.isEmpty()) {
            projectSelector.addItem("No projects available");
            projectSelector.setEnabled(false);
        } else {
            for (String project : projects) {
                projectSelector.addItem(project);
            }
            projectSelector.setEnabled(true);
        }
    }

    private void showInstructions() {
        JFrame instructionsFrame = new JFrame("Инструкция по использованию плагина монорепозитория");
        instructionsFrame.setSize(400, 300);

        JEditorPane instructionsPane = new JEditorPane();
        instructionsPane.setContentType("text/html");
        instructionsPane.setText(
                "<html><body style='width: 360px;'>" +
                        "<h3>Инструкция по использованию плагина монорепозитория:</h3>" +
                        "<ol>" +
                        "<li>Для использования плагина важно убедиться в наличии <code>pm.json</code> файла в корне проекта, который имеет примерно такую структуру:<br>" +
                        "<pre style='background-color: #f0f0f0; padding: 10px; border: 1px solid #ccc;'>"+
                        "{\"repositoryPath\":\"/Users/22x99/IdeaProjects/demo1/src/main/projects\",\"files\":[<br>" +
                        "  \"pubspec.yaml\",<br>" +
                        "  \"ios/Info.pconfig\",<br>" +
                        "  \"test\"<br>" +
                        "]}</pre>" +
                        "В случае отсутствия данного файла или если он заполнен некорректно - будет ошибка.</li>" +
                        "<li>Также важно указать путь до папки с проектами репозитория. Он должен также находиться в корне проекта (задаем <code>repositoryPath</code>).</li>" +
                        "<li>Для того, чтобы загрузить репозиторий - выберите проект в селекторе и нажмите <strong>Load</strong>.</li>" +
                        "<li>Для сохранения конфигурации по данному репозиторию, нажмите <strong>Save</strong>.</li>" +
                        "</ol>" +
                        "</body></html>"
        );
        instructionsPane.setEditable(false);
        instructionsPane.setOpaque(false);

        instructionsFrame.getContentPane().add(new JScrollPane(instructionsPane));
        instructionsFrame.setVisible(true);
    }

}
