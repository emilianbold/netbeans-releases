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
package org.netbeans.modules.profiler.projectsupport.utilities;

import java.io.InputStream;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.lib.profiler.common.filters.SimpleFilter;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ProjectUtilities {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    private static final Logger LOGGER = Logger.getLogger(ProjectUtilities.class.getName());
    private static final String PROFILE_PROJECT_CLASSES_STRING = NbBundle.getMessage(ProjectUtilities.class,
            "ProjectUtilities_ProfileProjectClassesString"); // NOI18N
    private static final String PROFILE_PROJECT_SUBPROJECT_CLASSES_STRING = NbBundle.getMessage(ProjectUtilities.class,
            "ProjectUtilities_ProfileProjectSubprojectClassesString"); // NOI18N
    public static final SimpleFilter FILTER_PROJECT_ONLY = new SimpleFilter(PROFILE_PROJECT_CLASSES_STRING,
            SimpleFilter.SIMPLE_FILTER_INCLUSIVE,
            "{$project.classes.only}"); // NOI18N
    public static final SimpleFilter FILTER_PROJECT_SUBPROJECTS_ONLY = new SimpleFilter(PROFILE_PROJECT_SUBPROJECT_CLASSES_STRING,
            SimpleFilter.SIMPLE_FILTER_INCLUSIVE,
            "{$project.subprojects.classes.only}"); // NOI18N

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    public static ClasspathInfo getClasspathInfo(final Project project) {
        return getClasspathInfo(project, true);
    }

    public static ClasspathInfo getClasspathInfo(final Project project, final boolean includeSubprojects) {
        return getClasspathInfo(project, includeSubprojects, true, true);
    }

    public static ClasspathInfo getClasspathInfo(final Project project, final boolean includeSubprojects,
            final boolean includeSources, final boolean includeLibraries) {
        FileObject[] sourceRoots = getSourceRoots(project, includeSubprojects);
        Set<FileObject> srcRootSet = new HashSet<FileObject>(sourceRoots.length);
        java.util.List<URL> urlList = new ArrayList<URL>();

        srcRootSet.addAll(Arrays.asList(sourceRoots));

        if (((sourceRoots == null) || (sourceRoots.length == 0)) && !includeSubprojects) {
            sourceRoots = getSourceRoots(project, true);
        }

        final ClassPath cpEmpty = ClassPathSupport.createClassPath(new FileObject[0]);

        if (sourceRoots.length == 0) {
            return null; // fail early
        }

        ClassPath cpSource = ClassPathSupport.createClassPath(sourceRoots);

        // cleaning up compile classpatth; we need to get rid off all project's class file references in the classpath
        ClassPath cpCompile = ClassPath.getClassPath(sourceRoots[0], ClassPath.COMPILE);

        for (ClassPath.Entry entry : cpCompile.entries()) {
            SourceForBinaryQuery.Result rslt = SourceForBinaryQuery.findSourceRoots(entry.getURL());
            FileObject[] roots = rslt.getRoots();

            if ((roots == null) || (roots.length == 0)) {
                urlList.add(entry.getURL());
            }
        }

        cpCompile = ClassPathSupport.createClassPath(urlList.toArray(new URL[urlList.size()]));

        return ClasspathInfo.create(includeLibraries ? ClassPath.getClassPath(sourceRoots[0], ClassPath.BOOT) : cpEmpty,
                includeLibraries ? cpCompile : cpEmpty, includeSources ? cpSource : cpEmpty);
    }

    /**
     * @return The current main project or null if no project is main.
     */
    public static Project getMainProject() {
        return OpenProjects.getDefault().getMainProject();
    }

    public static Project[] getOpenedProjects() {
        return OpenProjects.getDefault().getOpenProjects();
    }

    public static Project[] getSortedProjects(Project[] projects) {
        ArrayList projectsArray = new ArrayList(projects.length);

        for (int i = 0; i < projects.length; i++) {
            projectsArray.add(projects[i]);
        }

        try {
            Collections.sort(projectsArray,
                    new Comparator() {

                        public int compare(Object o1, Object o2) {
                            Project p1 = (Project) o1;
                            Project p2 = (Project) o2;

                            return ProjectUtils.getInformation(p1).getDisplayName().toLowerCase().compareTo(ProjectUtils.getInformation(p2).getDisplayName().toLowerCase());
                        }
                    });
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, e.getMessage()); // just in case ProjectUtils doesn't provide expected information
        }

        ;

        projectsArray.toArray(projects);

        return projects;
    }

    public static boolean hasAction(Project project, String actionName) {
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);

        if (ap == null) {
            return false; // return false if no ActionProvider available
        }

        String[] actions = ap.getSupportedActions();

        for (int i = 0; i < actions.length; i++) {
            if ((actions[i] != null) && actionName.equals(actions[i])) {
                return true;
            }
        }

        return false;
    }

    public static FileObject getOrCreateBuildFolder(Project project, String buildDirProp) {
        FileObject buildDir = FileUtil.toFileObject(PropertyUtils.resolveFile(FileUtil.toFile(project.getProjectDirectory()),
                buildDirProp));

        if (buildDir == null) {
            try {
                // TODO: if buildDirProp is absolute, relativize via PropertyUtils
                buildDir = FileUtil.createFolder(project.getProjectDirectory(), buildDirProp);
            } catch (IOException e) {
                MessageFormat.format(NbBundle.getMessage(ProjectUtilities.class, "FailedCreateOutputFolderMsg"),
                        new Object[]{e.getMessage()                        }); // NOI18N

                ErrorManager.getDefault().notify(ErrorManager.ERROR, e);

                return null;
            }
        }

        return buildDir;
    }

    public static Properties getProjectProperties(final Project project) {
        final Properties props = new Properties();
        final FileObject propFile = project.getProjectDirectory().getFileObject("nbproject/project.properties"); // NOI18N
        if (propFile != null) {
            ProjectManager.mutex().readAccess(new Runnable() {

                public void run() {
                    InputStream in = null;
                    try {
                        in = propFile.getInputStream();
                        props.load(in);
                    } catch (IOException ex) {
                        LOGGER.finest("Could not load properties file: " + propFile.getPath()); // NOI18N
                    } finally {
                        if (in != null) {
                            try {
                                 in.close();
                            } catch (IOException ex) {
                                // ignore
                            }
                        }
                    }
                }
            });
        }
        return props;
    }

    public static String getProjectBuildScript(final Project project) {
        final FileObject buildFile = project.getProjectDirectory().getFileObject("build.xml"); //NOI18N
        RandomAccessFile file = null;
        byte[] data = null;

        try {
            file = new RandomAccessFile(FileUtil.toFile(buildFile), "r");
            data = new byte[(int) buildFile.getSize()];
            file.readFully(data);
        } catch (FileNotFoundException e2) {
            ProfilerLogger.log(e2);

            return null;
        } catch (IOException e2) {
            ProfilerLogger.log(e2);

            return null;
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e2) {
                    ProfilerLogger.log(e2);
                }
            }
        }

        try {
            return new String(data, "UTF-8" //NOI18N
                    ); // According to Issue 65557, build.xml uses UTF-8, not default encoding!
        } catch (UnsupportedEncodingException ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);

            return null;
        }
    }

    public static java.util.List<SimpleFilter> getProjectDefaultInstrFilters(Project project) {
        java.util.List<SimpleFilter> v = new ArrayList<SimpleFilter>();

        if (ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA).length > 0) {
            v.add(FILTER_PROJECT_ONLY);
        }

        if (hasSubprojects(project)) {
            v.add(FILTER_PROJECT_SUBPROJECTS_ONLY);
        }

        return v;
    }

    public static ClientUtils.SourceCodeSelection[] getProjectDefaultRoots(Project project, String[][] projectPackagesDescr) {
        computeProjectPackages(project, true, projectPackagesDescr);

        ClientUtils.SourceCodeSelection[] ret = new ClientUtils.SourceCodeSelection[projectPackagesDescr[1].length];

        for (int i = 0; i < projectPackagesDescr[1].length; i++) {
            if ("".equals(projectPackagesDescr[1][i])) { //NOI18N
                ret[i] = new ClientUtils.SourceCodeSelection("", "", ""); //NOI18N
            } else {
                ret[i] = new ClientUtils.SourceCodeSelection(projectPackagesDescr[1][i] + ".", "", ""); //NOI18N
            }
        }

        return ret;
    }

    public static Project getProjectForBuildScript(String fileName) {
        FileObject projectFO = FileUtil.toFileObject(new File(fileName));

        while (projectFO != null) {
            try {
                if (projectFO.isFolder()) {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Trying: " + projectFO); //NOI18N
                    }

                    Project p = ProjectManager.getDefault().findProject(projectFO);

                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Got: " + ((p != null) ? getProjectName(p) : null)); //NOI18N
                    }

                    if (p != null) {
                        return p;
                    }
                }

                projectFO = projectFO.getParent();
            } catch (IOException e) {
                ProfilerLogger.severe("Got: IOException : " + e.getMessage()); //NOI18N
            }
        }

        return null;
    }

    public static Icon getProjectIcon(Project project) {
        ProjectInformation info = project.getLookup().lookup(ProjectInformation.class);

        if (info == null) {
            return new ImageIcon();
        } else {
            return info.getIcon();
        }
    }

    public static String getProjectName(Project project) {
        ProjectInformation info = project.getLookup().lookup(ProjectInformation.class);

        return (info != null) ? info.getDisplayName() : "UNKNOWN";
    }

    /**
     * Provides a list of source roots for the given project.
     *
     * @param project The project
     * @return an array of FileObjects that are the source roots for this project
     */
    public static FileObject[] getSourceRoots(final Project project) {
        return getSourceRoots(project, true);
    }

    /**
     * Provides a list of source roots for the given project.
     *
     * @param project The project
     * @param traverse Include subprojects
     * @return an array of FileObjects that are the source roots for this project
     */
    public static FileObject[] getSourceRoots(final Project project, final boolean traverse) {
        Set<FileObject> set = new HashSet<FileObject>();
        Set<Project> projects = new HashSet<Project>();

        projects.add(project);
        getSourceRoots(project, traverse, projects, set);

        return set.toArray(new FileObject[set.size()]);
    }

    public static void fetchSubprojects(final Project project, final Set<Project> projects) {
        // process possible subprojects
        SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);

        if (spp != null) {
            for (Project p : spp.getSubprojects()) {
                if (projects.add(p)) {
                    fetchSubprojects(p, projects);
                }
            }
        }
    }

    public static SimpleFilter computeProjectOnlyInstrumentationFilter(Project project, SimpleFilter predefinedInstrFilter,
            String[][] projectPackagesDescr) {
        // TODO: projectPackagesDescr[1] should only contain packages from subprojects, currently contains also toplevel project packages
        if (FILTER_PROJECT_ONLY.equals(predefinedInstrFilter)) {
            computeProjectPackages(project, false, projectPackagesDescr);

            StringBuffer projectPackages = new StringBuffer();

            for (int i = 0; i < projectPackagesDescr[0].length; i++) {
                projectPackages.append("".equals(projectPackagesDescr[0][i]) ? getDefaultPackageClassNames(project)
                        : (projectPackagesDescr[0][i] + ". ")); //NOI18N
            }

            return new SimpleFilter(PROFILE_PROJECT_CLASSES_STRING, SimpleFilter.SIMPLE_FILTER_INCLUSIVE,
                    projectPackages.toString().trim());
        } else if (FILTER_PROJECT_SUBPROJECTS_ONLY.equals(predefinedInstrFilter)) {
            computeProjectPackages(project, true, projectPackagesDescr);

            StringBuffer projectPackages = new StringBuffer();

            for (int i = 0; i < projectPackagesDescr[1].length; i++) {
                projectPackages.append("".equals(projectPackagesDescr[1][i]) ? getDefaultPackageClassNames(project)
                        : (projectPackagesDescr[1][i] + ". ")); //NOI18N // TODO: default packages need to be processed also for subprojects!!!
            }

            return new SimpleFilter(PROFILE_PROJECT_SUBPROJECT_CLASSES_STRING, SimpleFilter.SIMPLE_FILTER_INCLUSIVE,
                    projectPackages.toString().trim());
        }

        return null;
    }

    public static String getDefaultPackageClassNames(Project project) {
        Collection<String> classNames = SourceUtils.getDefaultPackageClassNames(project);
        StringBuffer classNamesBuf = new StringBuffer();

        for (String className : classNames) {
            classNamesBuf.append(className).append(" "); //NOI18N
        }

        return classNamesBuf.toString();
    }

    public static void computeProjectPackages(final Project project, boolean subprojects, String[][] storage) {
        if ((storage == null) || (storage.length != 2)) {
            throw new IllegalArgumentException("Storage must be a non-null String[2][] array"); // NOI18N
        }

        if (storage[0] == null) {
            Collection<String> packages1 = new ArrayList<String>();

            for (FileObject root : getSourceRoots(project, false)) {
                addSubpackages(packages1, "", root); //NOI18N
            }

            storage[0] = packages1.toArray(new String[0]);
        }

        if (subprojects && (storage[1] == null)) {
            FileObject[] srcRoots2 = getSourceRoots(project, true); // TODO: should be computed based on already known srcRoots1
            ArrayList<String> packages2 = new ArrayList<String>();

            for (FileObject root : srcRoots2) {
                addSubpackages(packages2, "", root); //NOI18N
            }

            storage[1] = packages2.toArray(new String[0]);
        }
    }

    /**
     * Will find
     * Copied from JUnit module implementation in 4.1 and modified
     */
    public static FileObject findTestForFile(final FileObject selectedFO) {
        if ((selectedFO == null) || !selectedFO.getExt().equalsIgnoreCase("java")) {
            return null; // NOI18N
        }

        ClassPath cp = ClassPath.getClassPath(selectedFO, ClassPath.SOURCE);

        if (cp == null) {
            return null;
        }

        FileObject packageRoot = cp.findOwnerRoot(selectedFO);

        if (packageRoot == null) {
            return null; // not a file in the source dirs - e.g. generated class in web app
        }

        URL[] testRoots = UnitTestForSourceQuery.findUnitTests(packageRoot);
        FileObject fileToOpen = null;

        for (int j = 0; j < testRoots.length; j++) {
            fileToOpen = findUnitTestInTestRoot(cp, selectedFO, testRoots[j]);

            if (fileToOpen != null) {
                return fileToOpen;
            }
        }

        return null;
    }

    public static void invokeAction(Project project, String s) {
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);

        if (ap == null) {
            return; // fail early
        }

        ap.invokeAction(s, Lookup.getDefault());
    }

        // Returns true if the project contains any Java sources (does not check subprojects!)
    public static boolean isJavaProject(Project project) {
        if (project == null) return false;
        
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        return sourceGroups.length > 0;
    }
    
    private static void getSourceRoots(final Project project, final boolean traverse, Set<Project> projects, Set<FileObject> roots) {
        final Sources sources = ProjectUtils.getSources(project);

        for (SourceGroup sg : sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            roots.add(sg.getRootFolder());
        }

        if (traverse) {
            // process possible subprojects
            SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);

            if (spp != null) {
                for (Project p : spp.getSubprojects()) {
                    if (projects.add(p)) {
                        getSourceRoots(p, traverse, projects, roots);
                    }
                }
            }
        }
    }

    private static void addSubpackages(Collection<String> packages, String prefix, FileObject packageFO) {
        if (!packageFO.isFolder()) { // not a folder

            return;
        }

        FileObject[] children = packageFO.getChildren();

        // 1. check if there are java sources in this folder and if so, add to the list of packages
        if (!packages.contains(prefix)) { // already in there, skip this

            for (int i = 0; i < children.length; i++) {
                FileObject child = children[i];

                if (child.getExt().equals("java")) { //NOI18N
                    packages.add(prefix);

                    break;
                }
            }
        }

        // 2. recurse into subfolders
        for (int i = 0; i < children.length; i++) {
            FileObject child = children[i];

            if (child.isFolder()) {
                if ("".equals(prefix)) { //NOI18N
                    addSubpackages(packages, child.getName(), child);
                } else {
                    addSubpackages(packages, prefix + "." + child.getName(), child); //NOI18N
                }
            }
        }
    }

    private static boolean hasSubprojects(Project project) {
        SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);

        if (spp == null) {
            return false;
        }

        return spp.getSubprojects().size() > 0;
    }

    /**
     * Copied from JUnit module implementation in 4.1 and modified
     */
    private static FileObject findUnitTestInTestRoot(ClassPath cp, FileObject selectedFO, URL testRoot) {
        ClassPath testClassPath = null;

        if (testRoot == null) { //no tests, use sources instead
            testClassPath = cp;
        } else {
            try {
                List<PathResourceImplementation> cpItems = new ArrayList<PathResourceImplementation>();
                cpItems.add(ClassPathSupport.createResource(testRoot));
                testClassPath = ClassPathSupport.createClassPath(cpItems);
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                testClassPath = cp;
            }
        }

        String testName = getTestName(cp, selectedFO);

        return testClassPath.findResource(testName + ".java"); // NOI18N
    }

    /**
     * Copied from JUnit module implementation in 4.1 and modified
     */
    private static String getTestName(ClassPath cp, FileObject selectedFO) {
        String resource = cp.getResourceName(selectedFO, '/', false); //NOI18N
        String testName = null;

        if (selectedFO.isFolder()) {
            //find Suite for package
            testName = convertPackage2SuiteName(resource);
        } else {
            // find Test for class
            testName = convertClass2TestName(resource);
        }

        return testName;
    }

    /**
     * Copied from JUnit module implementation in 4.1 and modified
     * Hardcoded test name prefix/suffix.
     */
    private static String convertClass2TestName(String classFileName) {
        if ((classFileName == null) || "".equals(classFileName)) {
            return ""; //NOI18N
        }

        int index = classFileName.lastIndexOf('/'); //NOI18N
        String pkg = (index > -1) ? classFileName.substring(0, index) : ""; // NOI18N
        String clazz = (index > -1) ? classFileName.substring(index + 1) : classFileName;
        clazz = clazz.substring(0, 1).toUpperCase() + clazz.substring(1);

        if (pkg.length() > 0) {
            pkg += "/"; // NOI18N
        }

        return pkg + clazz + "Test"; // NOI18N
    }

    /**
     * Copied from JUnit module implementation in 4.1 and modified
     * Hardcoded test name prefix/suffix.
     */
    private static String convertPackage2SuiteName(String packageFileName) {
        if ((packageFileName == null) || "".equals(packageFileName)) {
            return ""; //NOI18N
        }

        int index = packageFileName.lastIndexOf('/'); //NOI18N
        String pkg = (index > -1) ? packageFileName.substring(index + 1) : packageFileName;
        pkg = pkg.substring(0, 1).toUpperCase() + pkg.substring(1);

        return packageFileName + "/" + pkg + "Test"; // NOI18N
    }
}
