/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Parses given workspace and fills up it with found data.
 *
 * @author mkrauskopf
 */
final class WorkspaceParser {
    
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(WorkspaceParser.class.getName());
    
    private static final String VM_XML = "org.eclipse.jdt.launching.PREF_VM_XML"; // NOI18N
    private static final String IGNORED_CP_ENTRY = "##<cp entry ignore>##"; // NOI18N
    
    private static final String VARIABLE_PREFIX = "org.eclipse.jdt.core.classpathVariable."; // NOI18N
    private static final int VARIABLE_PREFIX_LENGTH = VARIABLE_PREFIX.length();
    
    private static final String RESOURCES_VARIABLE_PREFIX = "pathvariable."; // NOI18N
    private static final int RESOURCES_VARIABLE_PREFIX_LENGTH = RESOURCES_VARIABLE_PREFIX.length();

    private static final String USER_LIBRARY_PREFIX = "org.eclipse.jdt.core.userLibrary."; // NOI18N
    private static final int USER_LIBRARY_PREFIX_LENGTH = USER_LIBRARY_PREFIX.length();
    
    private final Workspace workspace;
    
    /** Creates a new instance of WorkspaceParser */
    WorkspaceParser(Workspace workspace) {
        this.workspace = workspace;
    }
    
    /** Returns classpath content from project's .classpath file */
    void parse() throws ProjectImporterException {
        try {
            parseLaunchingPreferences();
            parseCorePreferences();
            parseResourcesPreferences();
            parseWorkspaceProjects();
        } catch (IOException e) {
            throw new ProjectImporterException(
                    "Cannot load workspace properties", e); // NOI18N
        }
    }
    
    private void parseLaunchingPreferences() throws IOException, ProjectImporterException {
        Properties launchProps = EclipseUtils.loadProperties(workspace.getLaunchingPrefsFile());
        for (Iterator it = launchProps.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.equals(VM_XML)) {
                Map vmMap = PreferredVMParser.parse(value);
                workspace.setJREContainers(vmMap);
            }
        }
    }
    
    private void parseCorePreferences() throws IOException, ProjectImporterException {
        Properties coreProps = EclipseUtils.loadProperties(workspace.getCorePreferenceFile());
        for (Iterator it = coreProps.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.startsWith(VARIABLE_PREFIX)) {
                Workspace.Variable var = new Workspace.Variable();
                var.setName(key.substring(VARIABLE_PREFIX_LENGTH));
                var.setLocation(value);
                workspace.addVariable(var);
            } else if (key.startsWith(USER_LIBRARY_PREFIX) && !value.startsWith(IGNORED_CP_ENTRY)) { // #73542
                String libName = key.substring(USER_LIBRARY_PREFIX_LENGTH);
                workspace.addUserLibrary(libName, UserLibraryParser.getJars(value));
            } // else we don't use other properties in the meantime
        }
    }
    
    private void parseResourcesPreferences() throws IOException, ProjectImporterException {
        if (!workspace.getResourcesPreferenceFile().exists()) {
            return;
        }
        Properties coreProps = EclipseUtils.loadProperties(workspace.getResourcesPreferenceFile());
        for (Iterator it = coreProps.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.startsWith(RESOURCES_VARIABLE_PREFIX)) {
                Workspace.Variable var = new Workspace.Variable();
                var.setName(key.substring(RESOURCES_VARIABLE_PREFIX_LENGTH));
                var.setLocation(value);
                workspace.addResourcesVariable(var);
            }
        }
    }
    
    private void parseWorkspaceProjects() throws ProjectImporterException {
        // directory filter
        FileFilter dirFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
        
        Set projectsDirs = new HashSet();
        // let's find internal projects
        File[] innerDirs = workspace.getDirectory().listFiles(dirFilter);
        for (int i = 0; i < innerDirs.length; i++) {
            File prjDir = innerDirs[i];
            if (EclipseUtils.isRegularProject(prjDir)) {
                // we cannot load projects recursively until we have loaded
                // information of all projects in the workspace
                logger.finest("Found a regular Eclipse Project in: " // NOI18N
                        + prjDir.getAbsolutePath());
                if (!projectsDirs.contains(prjDir.getName())) {
                    addLightProject(projectsDirs, prjDir, true);
                } else {
                    logger.finest("Trying to add the same project twice: " // NOI18N
                            + prjDir.getAbsolutePath());
                }
            } // else .metadata or something we don't care about yet
        }
        
        // let's try to find external projects
        File[] resourceDirs = workspace.getResourceProjectsDir().listFiles(dirFilter);
        for (int i = 0; i < resourceDirs.length; i++) {
            File resDir = resourceDirs[i];
            File location = getLocation(resDir);
            if (location != null) {
                if (EclipseUtils.isRegularProject(location)) {
                    logger.finest("Found a regular Eclipse Project in: " // NOI18N
                            + location.getAbsolutePath());
                    if (!projectsDirs.contains(location.getName())) {
                        addLightProject(projectsDirs, location, false);
                    } else {
                        logger.finest("Trying to add the same project twice: " // NOI18N
                                + location.getAbsolutePath());
                    }
                } else {
                    logger.finest(location.getAbsolutePath() + " does not contain regular project"); // NOI18N
                }
            }
        }
        
        // Project instances with base infos are loaded, let's load all the
        // information we need (we have to do this here because project's
        // classpath needs at least project's names and abs. paths during
        // parsing
        for (Iterator it = workspace.getProjects().iterator(); it.hasNext(); ) {
            EclipseProject project = (EclipseProject) it.next();
            project.setWorkspace(workspace);
            ProjectFactory.getInstance().load(project);
        }
    }
    
    private void addLightProject(Set projectsDirs, File prjDir, boolean internal) {
        EclipseProject project = EclipseProject.createProject(prjDir);
        if (project != null) {
            project.setName(prjDir.getName());
            project.setInternal(internal);
            workspace.addProject(project);
            projectsDirs.add(prjDir.getName());
        }
    }
    
    /** Loads location of external project. */
    private static File getLocation(final File prjDir) throws ProjectImporterException {
        File locationFile = new File(prjDir, ".location"); // NOI18N
        if (locationFile.isFile()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(locationFile);
                return getLocation(fis);
            } catch (IOException e) {
                throw new ProjectImporterException("Error during reading " + // NOI18N
                        ".location file", e); // NOI18N
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        throw new ProjectImporterException(e);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Loads location of external project. Package-private for unit tests only.
     */
    static File getLocation(final InputStream is) throws IOException {
        // starts with 17 bytes.
        long toSkip = 17;
        while(toSkip != 0) {
            toSkip -= is.skip(toSkip);
        }
        // follows byte describing path length
        int pathLength = is.read();
        // follows path itself
        byte[] path = new byte[pathLength];
        int read = is.read(path);
        assert read == pathLength;
        String pathS = new String(path, "ISO-8859-1"); // NOI18N
        if (pathS.startsWith("URI//")) { // #89577 // NOI18N
            pathS = pathS.substring(pathS.indexOf(':') + 1);
        }
        if (pathS.length() == 0) {
            return null;
        }
        return new File(pathS);
    }
    
}
