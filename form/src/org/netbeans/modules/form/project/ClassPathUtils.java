/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.project;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.MessageFormat;

import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import org.netbeans.api.project.*;
import org.netbeans.api.project.ant.*;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;

/**
 * Utility methods related to classpath in projects.
 *
 * @author Tomas Pavek
 */

public class ClassPathUtils {

    private static Map<Project,FormClassLoader> loaders = new WeakHashMap<Project,FormClassLoader>();

    // class loading type - desired scope of classloader
    static final int UNSPECIFIED_CLASS = 0; // external class from user project's classpath only
    static final int SYSTEM_CLASS = 1; // class to be loaded by IDE system classloader (e.g. from a module)
    static final int SYSTEM_CLASS_WITH_PROJECT = 2; // class to be loaded from a module by classloader including also project classpath

    /**
     * Loads a class with a context of a project in mind (specified by arbitrary
     * file contained in the project). Typically the class is loaded from the
     * project's execution classpath unless it is a basic JDK class, or a class
     * registred as a support (system) class.
     */
    public static Class<?> loadClass(String name, FileObject fileInProject)
        throws ClassNotFoundException
    {
        return Class.forName(name, true, getFormClassLoader(fileInProject));
        // LinkageError left uncaught
    }

    public static boolean checkUserClass(String name, FileObject fileInProject) {
        ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.EXECUTE);
        if (classPath == null)
            return false;

        String fileName = name.replace('.', '/').concat(".class"); // NOI18N
        return classPath.findResource(fileName) != null;
    }

    private static FormClassLoader getFormClassLoader(FileObject fileInProject) {
        Project p = FileOwnerQuery.getOwner(fileInProject);
        FormClassLoader fcl = loaders.get(p);
        ClassLoader existingProjectCL = fcl != null ? fcl.getProjectClassLoader() : null;
        ClassLoader newProjectCL = ProjectClassLoader.getUpToDateClassLoader(
                                     fileInProject, existingProjectCL);
        if (fcl == null || newProjectCL != existingProjectCL) {
            fcl = new FormClassLoader(newProjectCL);
            loaders.put(p, fcl);
        }
        return fcl;
    }
    
    // Don't use - public only because of FormLAF
    public static ClassLoader getProjectClassLoader(FileObject fileInProject) {
        return getFormClassLoader(fileInProject).getProjectClassLoader();
    }

    static int getClassLoadingType(String className) {
        int i = className.lastIndexOf("[L"); // NOI18N
        if (i != -1)
            className = className.substring(i+2, className.length()-1);
        if (isClassLoaderType(className, SYSTEM_CLASS))
            return SYSTEM_CLASS;
        if (isClassLoaderType(className, SYSTEM_CLASS_WITH_PROJECT))
            return SYSTEM_CLASS_WITH_PROJECT;
        return UNSPECIFIED_CLASS;
    }

    /** Loads class from classpath described by ClassSource object.
     * @return loaded class, null if class name in ClassSource is null
     */
    public static Class loadClass(ClassSource classSource)
        throws ClassNotFoundException
    {
        String className = classSource.getClassName();
        if (className == null)
            return null;

        ClassLoader loader = null;
        int cpRootCount = classSource.getCPRootCount();

        if (cpRootCount == 0) {
            // for loading JDK classes
            loader = Lookup.getDefault().lookup(ClassLoader.class);
        }
        else try {
            List<URL> urlList = new ArrayList<URL>();
            for (int i=0; i < cpRootCount; i++) {
                String type = classSource.getCPRootType(i);
                String name = classSource.getCPRootName(i);

                if (ClassSource.JAR_SOURCE.equals(type)) {
                    File jarFile = new File(name);
                    urlList.add(FileUtil.getArchiveRoot(jarFile.toURI().toURL()));
                }
                else if (ClassSource.LIBRARY_SOURCE.equals(type)) {
                    Library lib = LibraryManager.getDefault().getLibrary(name);
                    if (lib != null) {
                        List content = lib.getContent("classpath"); // NOI18N
                        for (Iterator it=content.iterator(); it.hasNext(); ) {
                            URL rootURL = (URL) it.next();
                            if (FileUtil.isArchiveFile(rootURL))
                                rootURL = FileUtil.getArchiveRoot(rootURL);
                            urlList.add(rootURL);
                        }
                    }
                }
                else if (ClassSource.PROJECT_SOURCE.equals(type)) {
                    File outputFile = new File(name);
                    URL rootURL = FileUtil.getArchiveRoot(outputFile.toURI().toURL());
                    if (FileUtil.isArchiveFile(rootURL))
                        rootURL = FileUtil.getArchiveRoot(rootURL);
                    urlList.add(rootURL);
                }
            }

            if (urlList.size() > 0) {
                URL[] roots = new URL[urlList.size()];
                urlList.toArray(roots);
                loader = ClassPathSupport.createClassPath(roots).getClassLoader(true);
            }
            else return null;
        }
        catch (Exception ex) { // could not construct the classpath
            IllegalArgumentException iae = new IllegalArgumentException();
            ErrorManager.getDefault().annotate(iae, ex);
            throw iae;
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return loader.loadClass(classSource.getClassName());
    }

    /** Creates ClassSource object corresponding to project output classpath.
     * @param fileInProject FileObject being source (.java) or output (.class)
     *        file in a project
     * @param classname String name of class for which the ClassSource is
     *        created
     */
    public static ClassSource getProjectClassSource(FileObject fileInProject,
                                                    String classname)
    {
        Project project = FileOwnerQuery.getOwner(fileInProject);
        if (project == null)
            return null; // the file is not in any project

        // find the project output (presumably a JAR file) where the given
        // source file is compiled (packed) to
        AntArtifact[] artifacts =
            AntArtifactQuery.findArtifactsByType(project, "jar"); // NOI18N
        if (artifacts.length == 0)
            return null; // there is no project output

        String[] outputs = null;

        for (int i=0; i < artifacts.length; i++) {
            URI scriptLocation = artifacts[i].getScriptLocation().toURI();
            URI[] artifactLocations = artifacts[i].getArtifactLocations();
            for (int k=0; k < artifactLocations.length; k++) {
                File outputFile = new File(scriptLocation.resolve(artifactLocations[k]).normalize());

                URL outputURL;
                try {
                    outputURL = outputFile.toURI().toURL();
                }
                catch (MalformedURLException ex) { // should not happen
                    continue;
                }

                if (FileUtil.isArchiveFile(outputURL))
                    outputURL = FileUtil.getArchiveRoot(outputURL);
                FileObject sourceRoots[] =
                    SourceForBinaryQuery.findSourceRoots(outputURL).getRoots();
                for (int j=0; j < sourceRoots.length; j++)
                    if (FileUtil.isParentOf(sourceRoots[j], fileInProject)) {
                        outputs = new String[] { outputFile.getAbsolutePath() };
                        break;
                    }
                if (outputs != null)
                    break;
            }
        }

        if (outputs == null) {
            // no output found for given source file - the file might not be
            // a source file ... but a binary output file - in this case return
            // simply all project outputs as there is no good way to recognize
            // the right one (and j2se project has just one output anyway)

            if (!fileInProject.getExt().equals("class")) // NOI18N
                return null; // not interested in other than .class binary files

            List<String> outputList = new ArrayList<String>(artifacts.length);
            for (int i=0; i < artifacts.length; i++) {
                URI[] artifactLocations = artifacts[i].getArtifactLocations();
                for (int j=0; j < artifactLocations.length; j++) {
                    File outputFile = new File(
                        artifacts[i].getScriptLocation().getParent()
                        + File.separator
                        + artifactLocations[j].getPath());
                    outputList.add(outputFile.getAbsolutePath());
            }
            }
            outputs = outputList.toArray(new String[outputList.size()]);
        }

        String[] types = new String[outputs.length];
        for (int i=0; i < types.length; i++)
            types[i] = ClassSource.PROJECT_SOURCE;

        return new ClassSource(classname, types, outputs);
    }
    
    public static boolean isOnClassPath(FileObject fileInProject, String className) {
        String resourceName = className.replace('.', '/') + ".class"; // NOI18N
        ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.EXECUTE);
        if (classPath == null)
            return false;

        return classPath.findResource(resourceName) != null;
    }

    public static boolean isJava6ProjectPlatform(FileObject fileInProject) {
        ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.BOOT);
        if (classPath == null)
            return false;

        return classPath.findResource("javax/swing/GroupLayout.class") != null; // NOI18N
    }

    /** Updates project'c classpath with entries from ClassSource object.
     */
    public static boolean updateProject(FileObject fileInProject,
                                        ClassSource classSource)
        throws IOException
    {
        if (classSource.getCPRootCount() == 0)
            return false; // nothing to add to project

        Project project = FileOwnerQuery.getOwner(fileInProject);
	if(project==null)
	    return false;
	
        for (int i=0, n=classSource.getCPRootCount(); i < n; i++) {
            String type = classSource.getCPRootType(i);
            String name = classSource.getCPRootName(i);

            if (ClassSource.JAR_SOURCE.equals(type)) {
                FileObject jarFile = FileUtil.toFileObject(new File(name));
                URL url = URLMapper.findURL(FileUtil.getArchiveRoot(jarFile), URLMapper.EXTERNAL);
                ProjectClassPathModifier.addRoots(new URL[] {url}, fileInProject, ClassPath.COMPILE);
            }
            else if (ClassSource.LIBRARY_SOURCE.equals(type)) {
                Library lib = LibraryManager.getDefault().getLibrary(name);
                ProjectClassPathModifier.addLibraries(new Library[] {lib}, fileInProject, ClassPath.COMPILE);
            }
            else if (ClassSource.PROJECT_SOURCE.equals(type)) {
                File jarFile = new File(name);
                AntArtifact artifact =
                    AntArtifactQuery.findArtifactFromFile(jarFile);
                if (artifact.getProject() != project) {
                    URI[] locs = artifact.getArtifactLocations();
                    ProjectClassPathModifier.addAntArtifacts(new AntArtifact[] {artifact}, locs, fileInProject, ClassPath.COMPILE);
                }
            }
        }

        return true;
    }

    /** Provides description for ClassSource object usable e.g. for error
     * messages.
     */
    public static String getClassSourceDescription(ClassSource classSource) {
        if (classSource == null || classSource.getCPRootCount() == 0) {
            String className = classSource.getClassName();
            if (className != null) {
                if (className.startsWith("javax.") // NOI18N
                        || className.startsWith("java.")) // NOI18N
                    return getBundleString("MSG_StandardJDKSource"); // NOI18N
                if (className.startsWith("org.netbeans.")) // NOI18N
                    return getBundleString("MSG_NetBeansSource"); // NOI18N
            }
        }
        else {
            String type = classSource.getCPRootType(0);
            String name = classSource.getCPRootName(0);

            if (ClassSource.JAR_SOURCE.equals(type)) {
                return MessageFormat.format(
                    getBundleString("FMT_JarSource"), // NOI18N
                    new Object[] { name });
            }
            else if (ClassSource.LIBRARY_SOURCE.equals(type)) {
                Library lib = LibraryManager.getDefault().getLibrary(name);
                return MessageFormat.format(
                    getBundleString("FMT_LibrarySource"), // NOI18N
                    new Object[] { lib != null ? lib.getDisplayName() : name });
            }
            else if (ClassSource.PROJECT_SOURCE.equals(type)) {
                try {
                    Project project = FileOwnerQuery.getOwner(new File(name).toURI());
                    return MessageFormat.format(
                          getBundleString("FMT_ProjectSource"), // NOI18N
                          new Object[] { project == null ? name :
                                         project.getProjectDirectory().getPath()
                                           .replace('/', File.separatorChar) });
                }
                catch (Exception ex) {} // ignore
            }
        }

        return getBundleString("MSG_UnspecifiedSource"); // NOI18N
    }

    static String getBundleString(String key) {
        return NbBundle.getBundle(ClassPathUtils.class).getString(key);
    }

    // -----
    // Registered class patterns for class loader type

    private static FileObject patternSystemFolder;
    private static FileObject patternSystemWithProjectFolder;

    private static List patternsSystem;
    private static List patternsSystemWithProject;

    private static final String CL_LAYER_BASE = "org-netbeans-modules-form/classloader/"; // NOI18N
    private static final String CL_SYSTEM_CLASS = "system"; // NOI18N
    private static final String CL_SYSTEM_CLASS_WITH_PROJECT = "system_with_project"; // NOI18N

    private static boolean isClassLoaderType(String className, int clType) {
        List list = getClassPatterns(clType);
        if (list == null)
            return false;

        Iterator it = list.iterator();
        while (it.hasNext()) {
            ClassPattern cp = (ClassPattern) it.next();
            switch (cp.type) {
                case (ClassPattern.CLASS):
                    if (className.equals(cp.name))
                        return true;
                    break;
                case (ClassPattern.PACKAGE):
                    if (className.startsWith(cp.name) && (className.lastIndexOf('.') <= cp.name.length()))
                        return true;
                    break;
                case (ClassPattern.PACKAGE_AND_SUBPACKAGES):
                    if (className.startsWith(cp.name))
                        return true;
                    break;
            }
        }
        return false;
    }

    private static List getClassPatterns(int clType) {
        List list = null;
        switch (clType) {
            case SYSTEM_CLASS:
                list = patternsSystem;
                if (list == null) {
                    list = loadClassPatterns(getClassPatternsFolder(clType));
                    patternsSystem = list;
                }
                break;
            case SYSTEM_CLASS_WITH_PROJECT:
                list = patternsSystemWithProject;
                if (list == null) {
                    list = loadClassPatterns(getClassPatternsFolder(clType));
                    patternsSystemWithProject = list;
                }
                break;
        }
        return list;
    }

    private static FileObject getClassPatternsFolder(int clType) {
        FileObject folder = null;
        switch (clType) {
            case SYSTEM_CLASS:
                folder = patternSystemFolder;
                if (folder == null) {
                    folder = getClassPatternsFolder(CL_SYSTEM_CLASS);
                    if (folder == null)
                        return null;
                    // in case of any change in files make all the patterns reload
                    folder.addFileChangeListener(new FileChangeAdapter() {
                        @Override
                        public void fileDataCreated(FileEvent ev) {
                            patternsSystem = null;
                            loaders.clear();
                        }
                        @Override
                        public void fileDeleted(FileEvent ev) {
                            patternsSystem = null;
                            if (ev.getFile() == patternSystemFolder) {
                                patternSystemFolder.removeFileChangeListener(this);
                                patternSystemFolder = null;
                            }
                            loaders.clear();
                        }
                    });
                    patternSystemFolder = folder;
                }
                break;
            case SYSTEM_CLASS_WITH_PROJECT:
                folder = patternSystemWithProjectFolder;
                if (folder == null) {
                    folder = getClassPatternsFolder(CL_SYSTEM_CLASS_WITH_PROJECT);
                    if (folder == null)
                        return null;
                    // in case of any change in files make all the patterns reload
                    folder.addFileChangeListener(new FileChangeAdapter() {
                        @Override
                        public void fileDataCreated(FileEvent ev) {
                            patternsSystemWithProject = null;
                            loaders.clear();
                        }
                        @Override
                        public void fileDeleted(FileEvent ev) {
                            patternsSystemWithProject = null;
                            if (ev.getFile() == patternSystemFolder) {
                                patternSystemWithProjectFolder.removeFileChangeListener(this);
                                patternSystemWithProjectFolder = null;
                            }
                            loaders.clear();
                        }
                    });
                    patternSystemWithProjectFolder = folder;
                }
                break;
        }
        return folder;
    }

    private static FileObject getClassPatternsFolder(String folderName) {
        FileObject folder = null;
        if (folderName != null) {
            try {
                folder = Repository.getDefault().getDefaultFileSystem()
                             .findResource(CL_LAYER_BASE + folderName); // NOI18N
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return folder;
    }

    private static List<ClassPattern> loadClassPatterns(FileObject folder) {
        List<ClassPattern> list = new ArrayList<ClassPattern>();
        if (folder == null)
            return list;

        FileObject[] files = folder.getChildren();
        for (int i=0; i < files.length; i++) {
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(files[i].getInputStream()));
                String line = r.readLine();
                while (line != null) {
                    if (!line.equals("")) { // NOI18N
                        ClassPattern cp;
                        if (line.endsWith("**")) { // NOI18N
                            cp = new ClassPattern(line.substring(0, line.length()-2),
                                                  ClassPattern.PACKAGE_AND_SUBPACKAGES);
                        }
                        else if (line.endsWith("*")) { // NOI18N
                            cp = new ClassPattern(line.substring(0, line.length()-1),
                                                  ClassPattern.PACKAGE);
                        }
                        else {
                            cp = new ClassPattern(line, ClassPattern.CLASS);
                        }
                        list.add(cp);
                    }
                    line = r.readLine();
                }
            }
            catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return list;
    }

    private static class ClassPattern {
        static final int CLASS = 0;
        static final int PACKAGE = 1;
        static final int PACKAGE_AND_SUBPACKAGES = 2;
        String name;
        int type;
        
        ClassPattern(String name, int type) {
            this.name = name;
            this.type = type;
        }
    }
}
