/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.eclipse;

import java.io.BufferedInputStream;
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
import org.netbeans.modules.projectimport.ProjectImporterException;

/**
 * Parses given workspace and fills up it with found data.
 *
 * @author mkrauskopf
 */
final class WorkspaceParser {
    
    private static final String VM_XML =
            "org.eclipse.jdt.launching.PREF_VM_XML";
    
    private static final String VARIABLE_PREFIX =
            "org.eclipse.jdt.core.classpathVariable.";
    private static final int VARIABLE_PREFIX_LENGTH = VARIABLE_PREFIX.length();
    
    //    private static final String CP_CONTAINER_PREFIX =
    //            "org.eclipse.jdt.core.classpathContainer.";
    //    private static final int CP_CONTAINER_PREFIX_LENGTH = CP_CONTAINER_PREFIX.length();
    //    private static final String CP_CONTAINER_SUFFIX =
    //            "|org.eclipse.jdt.launching.JRE_CONTAINER";
    //    private static final int CP_CONTAINER_SUFFIX_LENGTH = CP_CONTAINER_SUFFIX.length();
    
    private Workspace workspace;
    
    /** Creates a new instance of WorkspaceParser */
    WorkspaceParser(Workspace workspace) {
        this.workspace = workspace;
    }
    
    /** Returns classpath content from project's .classpath file */
    void parse() throws ProjectImporterException {
        try {
            parseLaunchingPreferences();
            parseCorePreferences();
            parseWorkspaceProjects();
        } catch (IOException e) {
            throw new ProjectImporterException(
                    "Cannot load workspace properties", e);
        }
    }
    
    private void parseLaunchingPreferences() throws IOException, ProjectImporterException {
        Properties launchProps = loadProperties(workspace.getLaunchingPrefsFile());
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
    
    private void parseCorePreferences() throws IOException {
        Properties coreProps = loadProperties(workspace.getCorePreferenceFile());
        for (Iterator it = coreProps.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.startsWith(VARIABLE_PREFIX)) {
                Workspace.Variable var = new Workspace.Variable();
                var.setName(key.substring(VARIABLE_PREFIX_LENGTH));
                var.setLocation(value);
                workspace.addVariable(var);
            }
            //                            else if (key.startsWith(CP_CONTAINER_PREFIX) &&
            //                                    key.endsWith(CP_CONTAINER_SUFFIX)) {
            //                                String project = key.substring(CP_CONTAINER_PREFIX_LENGTH,
            //                                        key.length() - CP_CONTAINER_SUFFIX_LENGTH);
            //                                String jdkDir = parseJDKDir(ClassPathParser.parse(value));
            //                                workspace.addJDKDir(project, jdkDir);
            //                            } else {
            //                                // we don't use other properties in the meantime
            //                            }
        }
        
    }
    
    //    private String parseJDKDir(ClassPath cp) {
    //        for (Iterator it = cp.getEntries().iterator(); it.hasNext(); ) {
    //            ClassPathEntry entry = (ClassPathEntry) it.next();
    //            if (entry.getRawPath().endsWith("rt.jar")) {
    //                return entry.getRawPath();
    //            }
    //        }
    //        return null;
    //    }
    
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
                addLightProject(projectsDirs, prjDir, true);
            } // else .metadata or something we don't care about yet
        }
        
        // let's try to find external projects
        File[] resourceDirs = workspace.getResourceProjectsDir().listFiles(dirFilter);
        for (int i = 0; i < resourceDirs.length; i++) {
            File resDir = resourceDirs[i];
            File location = getLocation(resDir);
            if (location != null &&
                    EclipseUtils.isRegularProject(location) &&
                    !projectsDirs.contains(location.getName())) {
                addLightProject(projectsDirs, location, false);
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
    private File getLocation(File prjDir) throws ProjectImporterException {
        File locationFile = new File(prjDir, ".location");
        if (locationFile.isFile()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(locationFile);
                // starts with 17 bytes.
                long toSkip = 17;
                while(toSkip != 0) {
                    toSkip -= fis.skip(toSkip);
                }
                // follows byte describing path length
                int pathLength = fis.read();
                // follows path itself
                byte[] path = new byte[pathLength];
                fis.read(path);
                return new File(new String(path, "ISO-8859-1"));
            } catch (IOException e) {
                throw new ProjectImporterException("Error during reading " +
                        ".location file", e);
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
     * Load properties from a given <code>file</code>.
     *
     * @throws IOException when reading file failed
     */
    private Properties loadProperties(File file) throws IOException {
        InputStream propsIS = new BufferedInputStream(new FileInputStream(file));
        Properties properties = new Properties();
        properties.load(propsIS);
        propsIS.close();
        return properties;
    }
    
}
