package com.meteoro.monorepomanager;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class MonorepoManagerPlugin implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        MonorepoManagerService service = ServiceManager.getService(project, MonorepoManagerService.class);
        if (service != null) {
            service.initComponent();
        }
    }
}
