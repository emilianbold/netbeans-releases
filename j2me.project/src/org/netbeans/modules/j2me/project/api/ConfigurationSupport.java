/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2me.project.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2me.project.J2MEProject;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 * Support for {@link ProjectConfiguration}s.
 * Allows to list project configurations, retrieve active configuration
 * and get and set configuration specific properties.
 * @author Tomas Zezula
 * @since 1.2
 */
//@ThreadSafe
public final class ConfigurationSupport {

    private static final String CFG_PATHS = "nbproject/configs/";    //NOI18N
    private static final String CFG_PRIVATE_PATHS = "nbproject/private/configs/";    //NOI18N

    private ConfigurationSupport() {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }

    /**
     * Returns project configuration including the default configuration.
     * @param project the {@link Project} to return configurations for
     * @return the project's configurations or an empty collection when
     * the project type does not support configurations.
     */
    @NonNull
    public static Collection<? extends ProjectConfiguration> getConfigurations(@NonNull final Project project) {
        Parameters.notNull("project", project);
        final ProjectConfigurationProvider<? extends ProjectConfiguration> pcp =
            project.getLookup().lookup(ProjectConfigurationProvider.class);
        return pcp == null ?
            Collections.<ProjectConfiguration>emptySet() :
            pcp.getConfigurations();
    }

    /**
     * Returns the project active configuration.
     * @param project the {@link Project} to return active configuration for
     * @return the project's active configuration, default configuration when
     * no specific configuration is set or null when the project type does not
     * support configurations.
     */
    @CheckForNull
    public static ProjectConfiguration getActiveConfiguration(@NonNull final Project project) {
        Parameters.notNull("project", project);
        final ProjectConfigurationProvider<? extends ProjectConfiguration> pcp =
                project.getLookup().lookup(ProjectConfigurationProvider.class);
        return pcp == null ?
            null :
            pcp.getActiveConfiguration();
    }

    /**
     * Returns {@link EditableProperties} for given {@link ProjectConfiguration}.
     * @param project the {@link Project} to return {@link EditableProperties} for
     * @param cfg the {@link ProjectConfiguration} to return {@link EditableProperties} for
     * @param shared shared or private properties
     * @return the {@link EditableProperties} for given {@link ProjectConfiguration}
     * In case of null configuration or default configuration the project.properties
     * or private properties are returned depending on the shared parameter.
     * Threading: Thread safe, acquires {@link ProjectManager#mutex} in shared mode.
     */
    @NonNull
    public static EditableProperties getProperties(
        @NonNull final Project project,
        @NullAllowed final ProjectConfiguration cfg,
        final boolean shared) {
        Parameters.notNull("project", project); //NOI18N
        final J2MEProject j2meProject = project.getLookup().lookup(J2MEProject.class);
        if (j2meProject == null) {
            throw new IllegalArgumentException(String.format(
                "The project: %s (%s) is not a J2ME Embedded project", //NOI18N
                ProjectUtils.getInformation(project).getDisplayName(),
                FileUtil.getFileDisplayName(project.getProjectDirectory())));
        }
        return ProjectManager.mutex().readAccess(new Mutex.Action<EditableProperties>() {
            @Override
            public EditableProperties run() {
                final String path = getPath(project.getProjectDirectory(), cfg, shared);
                return j2meProject.getUpdateHelper().getProperties(path);
            }
        });
    }

    /**
     * Sets {@link EditableProperties} for given {@link ProjectConfiguration}.
     * @param project the {@link Project} to return {@link EditableProperties} for
     * @param cfg the {@link ProjectConfiguration} to return {@link EditableProperties} for
     * null or default configuration for project.properties (private.properties).
     * @param shared shared or private properties
     * @param ep the properties to be set
     * Threading: Thread safe, acquires {@link ProjectManager#mutex} in exclusive mode.
     */
    public static void putProperties(
        @NonNull final Project project,
        @NullAllowed final ProjectConfiguration cfg,
        final boolean shared,
        @NonNull final EditableProperties ep) {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("ep", ep);   //NOI18N
        final J2MEProject j2meProject = project.getLookup().lookup(J2MEProject.class);
        if (j2meProject == null) {
            throw new IllegalArgumentException(String.format(
                "The project: %s (%s) is not a J2ME Embedded project", //NOI18N
                ProjectUtils.getInformation(project).getDisplayName(),
                FileUtil.getFileDisplayName(project.getProjectDirectory())));
        }
        final String path = getPath(project.getProjectDirectory(), cfg, shared);
        j2meProject.getUpdateHelper().putProperties(path, ep);
    }

    @NonNull
    private static String getPath(
        @NonNull final FileObject root,
        @NullAllowed final ProjectConfiguration cfg,
        final boolean shared) {
        Parameters.notNull("root", root);   //NOI18N
        if (cfg == null) {
            //Default configuration -> return project.xml or private.xml
            return shared ?
                AntProjectHelper.PROJECT_PROPERTIES_PATH :
                AntProjectHelper.PRIVATE_PROPERTIES_PATH;
        }
        final Map<String,Pair<String,String>> nameToPaths = new TreeMap<String, Pair<String,String>>();
        final FileObject cfgFolder = root.getFileObject(CFG_PATHS);   //NOI18N
        if (cfgFolder != null) {
            for (FileObject fo : cfgFolder.getChildren()) {
                if ("properties".equals(fo.getExt())) { //NOI18N
                    final Properties p = new Properties();
                    try (final InputStream in = fo.getInputStream()) {
                        p.load(in);
                    } catch(IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                    String name = p.getProperty("$label");  //NOI18N
                    if (name == null) {
                        name = fo.getName();
                    }
                    nameToPaths.put(
                        name,
                        Pair.<String,String>of(
                            CFG_PATHS + fo.getNameExt(),
                            CFG_PRIVATE_PATHS + fo.getNameExt()));
                }
            }
        }
        final Pair<String,String> paths = nameToPaths.get(cfg.getDisplayName());
        return paths == null ?
                shared ?
                    AntProjectHelper.PROJECT_PROPERTIES_PATH :
                    AntProjectHelper.PRIVATE_PROPERTIES_PATH :
                shared ?
                    paths.first() :
                    paths.second();
    }

}
