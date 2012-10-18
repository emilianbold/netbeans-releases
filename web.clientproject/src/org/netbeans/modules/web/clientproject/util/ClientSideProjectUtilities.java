/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.util;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectType;
import org.netbeans.modules.web.clientproject.api.MissingLibResourceException;
import org.netbeans.modules.web.clientproject.api.WebClientLibraryManager;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.clientproject.ui.JavaScriptLibrarySelection;
import org.netbeans.modules.web.clientproject.ui.customizer.ClientSideProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author david
 */
public final class ClientSideProjectUtilities {

    private static final Logger LOGGER = Logger.getLogger(ClientSideProjectUtilities.class.getName());

    public static final Charset DEFAULT_PROJECT_CHARSET = getDefaultProjectCharset();


    private ClientSideProjectUtilities() {
    }

    /**
     * Setup project with the given name and also set the following properties:
     * <ul>
     *   <li>file encoding - set to UTF-8 (or default charset if UTF-8 is not available)</li>
     * </ul>
     * @param dirFO project directory
     * @param name project name
     * @return {@link AntProjectHelper}
     * @throws IOException if any error occurs
     */
    public static AntProjectHelper setupProject(FileObject dirFO, String name) throws IOException {
        // create project
        AntProjectHelper projectHelper = ProjectGenerator.createProject(dirFO, ClientSideProjectType.TYPE);
        setProjectName(projectHelper, name);
        ClientSideProject project = (ClientSideProject) FileOwnerQuery.getOwner(dirFO);
        // set encoding
        ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(project);
        projectProperties.setEncoding(DEFAULT_PROJECT_CHARSET.name());
        projectProperties.save();
        return projectHelper;
    }

    public static void initializeProject(@NonNull ClientSideProject project, @NonNull String siteRoot, @NullAllowed String test,
            @NullAllowed String config) throws IOException {
        File projectDirectory = FileUtil.toFile(project.getProjectDirectory());
        assert projectDirectory != null;
        assert projectDirectory.isDirectory();
        // ensure directories exists
        ensureDirectoryExists(PropertyUtils.resolveFile(projectDirectory, siteRoot));
        if (test != null) {
            ensureDirectoryExists(PropertyUtils.resolveFile(projectDirectory, test));
        }
        if (config != null) {
            ensureDirectoryExists(PropertyUtils.resolveFile(projectDirectory, config));
        }
        // save project
        ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(project);
        projectProperties.setSiteRootFolder(siteRoot);
        projectProperties.setTestFolder(test);
        projectProperties.setConfigFolder(config);
        projectProperties.save();
    }

    private static void ensureDirectoryExists(File folder) {
        if (!folder.isDirectory()) {
            if (!folder.mkdirs()) {
                LOGGER.log(Level.WARNING, "Folder cannot be created", folder);
            }
        }
    }

    public static void setProjectName(final AntProjectHelper projectHelper, final String name) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            @Override
            public void run() {
                Element data = projectHelper.getPrimaryConfigurationData(true);
                Document document = data.getOwnerDocument();
                NodeList nameList = data.getElementsByTagNameNS(ClientSideProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                Element nameElement;
                if (nameList.getLength() == 1) {
                    nameElement = (Element) nameList.item(0);
                    NodeList deadKids = nameElement.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameElement.removeChild(deadKids.item(0));
                    }
                } else {
                    nameElement = document.createElementNS(
                            ClientSideProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    data.insertBefore(nameElement, data.getChildNodes().item(0));
                }
                nameElement.appendChild(document.createTextNode(name));
                projectHelper.putPrimaryConfigurationData(data, true);
            }
        });
    }

    public static SourceGroup[] getSourceGroups(Project project) {
        assert project instanceof ClientSideProject : "ClientSideProject project expected but got: " + project.getClass().getName();
        Sources sources = ProjectUtils.getSources(project);
        List<SourceGroup> res = new ArrayList<SourceGroup>();
        res.addAll(Arrays.asList(sources.getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5)));
        res.addAll(Arrays.asList(sources.getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST)));
        res.addAll(Arrays.asList(sources.getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5_CONFIG)));
        return res.toArray(new SourceGroup[res.size()]);
    }

    public static SourceGroup[] getSourceGroups(Project project, String type) {
        Sources sources = ProjectUtils.getSources(project);
        return sources.getSourceGroups(type);
    }

    public static FileObject[] getSourceObjects(Project project) {
        SourceGroup[] groups = getSourceGroups(project);

        FileObject[] fileObjects = new FileObject[groups.length];
        for (int i = 0; i < groups.length; i++) {
            fileObjects[i] = groups[i].getRootFolder();
        }
        return fileObjects;
    }

    /**
     * Add JS libraries (<b>{@link JavaScriptLibrarySelection.SelectedLibrary#isDefault() non-default} only!</b>) to the given
     * site root, underneath the given JS libraries folder.
     * <p>
     * This method must be run in a background thread and stops if the current thread is interrupted.
     * @param selectedLibraries JS libraries to be added
     * @param jsLibFolder JS libraries folder
     * @param siteRootDir site root
     * @param handle progress handle, can be {@code null}
     * @return list of libraries that cannot be downloaded
     * @throws IOException if any error occurs
     */
    @NbBundle.Messages({
        "ClientSideProjectUtilities.error.copyingJsLib=Some of the library files could not be retrieved.",
        "# {0} - library name",
        "ClientSideProjectUtilities.msg.downloadingJsLib=Downloading {0}"
    })
    @CheckReturnValue
    public static List<JavaScriptLibrarySelection.SelectedLibrary> applyJsLibraries(List<JavaScriptLibrarySelection.SelectedLibrary> selectedLibraries,
            String jsLibFolder, FileObject siteRootDir, @NullAllowed ProgressHandle handle) throws IOException {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Must be run in a background thread");
        }
        List<JavaScriptLibrarySelection.SelectedLibrary> failed = new ArrayList<JavaScriptLibrarySelection.SelectedLibrary>(selectedLibraries.size());
        FileObject librariesRoot = null;
        for (JavaScriptLibrarySelection.SelectedLibrary selectedLibrary : selectedLibraries) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            if (selectedLibrary.isDefault()) {
                // ignore default js lib (they are already applied)
                continue;
            }
            if (librariesRoot == null) {
                librariesRoot = FileUtil.createFolder(siteRootDir, jsLibFolder);
            }
            JavaScriptLibrarySelection.LibraryVersion libraryVersion = selectedLibrary.getLibraryVersion();
            Library library = libraryVersion.getLibrary();
            if (handle != null) {
                handle.progress(Bundle.ClientSideProjectUtilities_msg_downloadingJsLib(library.getProperties().get(WebClientLibraryManager.PROPERTY_REAL_DISPLAY_NAME)));
            }
            try {
                WebClientLibraryManager.addLibraries(new Library[]{library}, librariesRoot, libraryVersion.getType());
            } catch (MissingLibResourceException e) {
                LOGGER.log(Level.FINE, null, e);
                failed.add(selectedLibrary);
            }
        }
        return failed;
    }

    // #217970
    private static Charset getDefaultProjectCharset() {
        try {
            return Charset.forName("UTF-8"); // NOI18N
        } catch (IllegalCharsetNameException exception) {
            // fallback
            LOGGER.log(Level.INFO, "UTF-8 charset not supported, falling back to the default charset.", exception);
        } catch (UnsupportedCharsetException exception) {
            // fallback
            LOGGER.log(Level.INFO, "UTF-8 charset not supported, falling back to the default charset.", exception);
        }
        return Charset.defaultCharset();
    }

}
