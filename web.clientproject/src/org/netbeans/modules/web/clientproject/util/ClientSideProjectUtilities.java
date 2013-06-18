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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.UIManager;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectType;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.clientproject.ui.customizer.ClientSideProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author david
 */
public final class ClientSideProjectUtilities {

    private static final Logger LOGGER = Logger.getLogger(ClientSideProjectUtilities.class.getName());
    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.web.clientproject"); // NOI18N

    public static final Charset DEFAULT_PROJECT_CHARSET = getDefaultProjectCharset();


    private ClientSideProjectUtilities() {
    }

    /**
     * Check whether the given folder is already a project.
     * @param folder folder to be checked
     * @return {@code true} if the given folder is already a project, {@code false} otherwise
     */
    public static boolean isProject(File folder) {
        Project prj = null;
        boolean foundButBroken = false;
        try {
            prj = ProjectManager.getDefault().findProject(FileUtil.toFileObject(FileUtil.normalizeFile(folder)));
        } catch (IOException ex) {
            foundButBroken = true;
        } catch (IllegalArgumentException ex) {
            // noop
        }
        return prj != null || foundButBroken;
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
        // #231319
        ProjectManager.getDefault().clearNonProjectCache();
        Project project = FileOwnerQuery.getOwner(dirFO);
        assert project != null;
        ClientSideProject clientSideProject = project.getLookup().lookup(ClientSideProject.class);
        if (clientSideProject == null) {
            throw new IllegalStateException("HTML5 project needed but found " + project.getClass().getName());
        }
        // set encoding
        ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(clientSideProject);
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

    private static void ensureDirectoryExists(File folder) throws IOException {
        if (!folder.isDirectory()) {
            if (!folder.mkdirs()) {
                throw new IOException("Cannot create folder " + folder);
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

    /**
     * Splits paths like 'index.html#/path' into  'index.html' and '#/path'
     */
    public static String[] splitPathAndFragment(String url) {
        int index = url.lastIndexOf('#');
        if (index != -1) {
            return new String[]{url.substring(0, index), url.substring(index)};
        } else {
            return new String[]{url,""};
        }
    }

    public static void logUsage(Class<? extends Object> srcClass, String message, Object[] params) {
        Parameters.notNull("message", message); // NOI18N

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params);
        }
        USG_LOGGER.log(logRecord);
    }

    public static boolean isBroken(ClientSideProject project) {
        return !project.getLookup().lookup(ProjectProblemsProvider.class).getProblems().isEmpty();
    }

    @NonNull
    public static Color getErrorForeground() {
        Color result = UIManager.getDefaults().getColor("nb.errorForeground");  // NOI18N
        if (result == null) {
            result = Color.RED;
        }
        return getSafeColor(result.getRed(), result.getGreen(), result.getBlue());
    }

    public static Color getSafeColor(int red, int green, int blue) {
        red = Math.max(red, 0);
        red = Math.min(red, 255);
        green = Math.max(green, 0);
        green = Math.min(green, 255);
        blue = Math.max(blue, 0);
        blue = Math.min(blue, 255);
        return new Color(red, green, blue);
    }

}
