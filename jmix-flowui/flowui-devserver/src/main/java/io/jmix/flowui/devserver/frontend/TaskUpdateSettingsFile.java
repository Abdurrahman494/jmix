/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.flowui.devserver.frontend;

import com.vaadin.flow.server.PwaConfiguration;
import com.vaadin.flow.server.frontend.DevBundleUtils;
import com.vaadin.flow.server.frontend.FallibleCommand;
import elemental.json.Json;
import elemental.json.JsonObject;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.vaadin.flow.server.Constants.VAADIN_WEBAPP_RESOURCES;
import static com.vaadin.flow.shared.ApplicationConstants.VAADIN_STATIC_FILES_PATH;
import static elemental.json.impl.JsonUtil.stringify;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.SERVICE_WORKER_SRC;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.SERVICE_WORKER_SRC_JS;

/**
 * Creates a vaadin-dev-server-settings.json file for use with dev server
 * configuration.
 */
public class TaskUpdateSettingsFile implements FallibleCommand, Serializable {

    public static final String DEV_SETTINGS_FILE = "vaadin-dev-server-settings.json";
    File npmFolder;
    File frontendDirectory;
    File jarFrontendResourcesFolder;
    File webappResourcesDirectory;
    File buildDirectory;
    String buildDirectoryName;
    String themeName;
    PwaConfiguration pwaConfiguration;

    TaskUpdateSettingsFile(Options builder, String themeName,
            PwaConfiguration pwaConfiguration) {
        this.npmFolder = builder.getStudioFolder();
        this.frontendDirectory = builder.getFrontendDirectory();
        this.jarFrontendResourcesFolder = builder
                .getJarFrontendResourcesFolder();
        this.webappResourcesDirectory = builder.getWebappResourcesDirectory();
        this.buildDirectory = builder.getBuildDirectory();
        this.buildDirectoryName = builder.getBuildDirectoryName();
        this.themeName = themeName;
        this.pwaConfiguration = pwaConfiguration;
    }

    @Override
    public void execute() {
        if (npmFolder == null)
            return;

        JsonObject settings = Json.createObject();
        String buildDirectoryPath = FrontendUtils.getUnixPath(buildDirectory.toPath());
        settings.put("frontendFolder", FrontendUtils.getUnixPath(frontendDirectory.toPath()));
        settings.put("themeFolder", "themes");
        settings.put("themeResourceFolder",
                FrontendUtils.getUnixPath(jarFrontendResourcesFolder.toPath()));
        String webappResources, statsOutput;
        if (webappResourcesDirectory == null) {
            webappResources = combinePath(buildDirectoryPath, "classes", VAADIN_WEBAPP_RESOURCES);
            statsOutput = combinePath(buildDirectoryPath, "classes",
                    VAADIN_WEBAPP_RESOURCES, "..", "config");
        } else {
            webappResources = webappResourcesDirectory.getPath();
            statsOutput = new File(webappResourcesDirectory.getParentFile(),
                    "config").getPath();
        }
        String staticOutput = combinePath(webappResources, VAADIN_STATIC_FILES_PATH);

        File devBundleOutputFolder = new File(
                DevBundleUtils.getDevBundleFolder(npmFolder), "webapp");
        String devBundleOutputFolderString = FrontendUtils
                .getUnixPath(devBundleOutputFolder.toPath());
        String devBundleStatsFolderString = FrontendUtils.getUnixPath(
                new File(DevBundleUtils.getDevBundleFolder(npmFolder), "config")
                        .toPath());

        settings.put("staticOutput", FrontendUtils.getUnixPath(new File(staticOutput).toPath()));
        settings.put("generatedFolder", "generated");
        settings.put("statsOutput", statsOutput);
        settings.put("frontendBundleOutput", webappResources);
        settings.put("devBundleOutput", devBundleOutputFolderString);
        settings.put("devBundleStatsOutput", devBundleStatsFolderString);
        settings.put("jarResourcesFolder",
                FrontendUtils.getUnixPath(jarFrontendResourcesFolder.toPath()));

        settings.put("themeName", themeName);

        settings.put("clientServiceWorkerSource", getServiceWorkerFile());

        settings.put("pwaEnabled", pwaConfiguration.isEnabled());

        settings.put("offlineEnabled", pwaConfiguration.isOfflineEnabled());
        settings.put("offlinePath", getOfflinePath());

        File settingsFile = new File(buildDirectory, "/" + DEV_SETTINGS_FILE);

        try {
            FileUtils.write(settingsFile, stringify(settings, 2), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log().error("Failed to write file: {}", settingsFile, e);
            FrontendUtils.logInFile("Failed to write file " + settingsFile + "\n" + e);
        }
    }

    private String combinePath(String... parts) {
        StringBuilder path = new StringBuilder();
        for (String part : parts) {
            path.append(part);
            if (!part.endsWith("/")) {
                path.append('/');
            }
        }
        return path.toString();
    }

    private String getServiceWorkerFile() {
        boolean exists = new File(frontendDirectory, SERVICE_WORKER_SRC).exists()
                || new File(frontendDirectory, SERVICE_WORKER_SRC_JS).exists();

        String serviceWorkerFile = SERVICE_WORKER_SRC;
        if (new File(frontendDirectory, SERVICE_WORKER_SRC_JS).exists()) {
            serviceWorkerFile = SERVICE_WORKER_SRC_JS;
        }

        if (!exists) {
            Path path = Paths.get(FrontendUtils.getUnixPath(buildDirectory.toPath()),
                    serviceWorkerFile);
            return path.toString();
        } else {
            return Paths.get(frontendDirectory.toString(), serviceWorkerFile)
                    .toString();
        }
    }

    private String getOfflinePath() {
        if (pwaConfiguration.isOfflinePathEnabled()) {
            return "'" + getEscapedRelativePath(
                    Paths.get(pwaConfiguration.getOfflinePath())) + "'";
        }
        return "'.'";
    }

    private String getEscapedRelativePath(Path path) {
        if (path.isAbsolute()) {
            return FrontendUtils.getUnixRelativePath(npmFolder.toPath(), path);
        } else {
            return FrontendUtils.getUnixPath(path);
        }
    }

    private Logger log() {
        return LoggerFactory.getLogger(this.getClass());
    }
}
