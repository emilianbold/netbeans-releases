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
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;

/**
 * Utility methods related to classpath in projects.
 *
 * @author Tomas Pavek
 */

public class ClassPathUtils {

    /** Loads class from project classpath. Project is specified by arbitrary
     * file contained in the project.
     */
    public static Class loadClass(String name, FileObject fileInProject)
        throws ClassNotFoundException
    {
        ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.EXECUTE);
        if (classPath == null) {
            throw new ClassNotFoundException(getBundleString("MSG_NullClassPath")); // NOI18N
        }
        return classPath.getClassLoader(true).loadClass(name);
        // LinkageError left uncaught
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
            loader = (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class);
        }
        else try {
            List urlList = new ArrayList();
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

            ArrayList outputList = new ArrayList(artifacts.length);
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
            outputs = (String[])outputList.toArray(new String[0]);
        }

        String[] types = new String[outputs.length];
        for (int i=0; i < types.length; i++)
            types[i] = ClassSource.PROJECT_SOURCE;

        return new ClassSource(classname, types, outputs);
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
        ProjectClassPathExtender projectClassPath = (ProjectClassPathExtender)
            project.getLookup().lookup(ProjectClassPathExtender.class);
        if (projectClassPath == null)
            return false; // not a project with classpath

        for (int i=0, n=classSource.getCPRootCount(); i < n; i++) {
            String type = classSource.getCPRootType(i);
            String name = classSource.getCPRootName(i);

            if (ClassSource.JAR_SOURCE.equals(type)) {
                FileObject jarFile = FileUtil.toFileObject(new File(name));
                projectClassPath.addArchiveFile(jarFile);
            }
            else if (ClassSource.LIBRARY_SOURCE.equals(type)) {
                Library lib = LibraryManager.getDefault().getLibrary(name);
                projectClassPath.addLibrary(lib);
            }
            else if (ClassSource.PROJECT_SOURCE.equals(type)) {
                File jarFile = new File(name);
                AntArtifact artifact =
                    AntArtifactQuery.findArtifactFromFile(jarFile);
                if (artifact.getProject() != project) {
                    URI[] locs = artifact.getArtifactLocations();
                    for (int y=0; y<locs.length; y++ ) {
                        projectClassPath.addAntArtifact(artifact, locs[y]);
                    }
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
}
