/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.palette;

import java.lang.ref.WeakReference;
import java.util.jar.*;
import java.util.*;
import java.io.*;

import org.openide.*;
import org.openide.nodes.Node;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.cookies.SourceCookie;
import org.openide.src.*;
import org.netbeans.api.project.ant.*;
import org.netbeans.api.project.*;
import org.netbeans.api.java.queries.SourceForBinaryQuery;

/**
 * This class provides methods for installing new items to Palete.
 *
 * @author Tomas Pavek
 */

public final class BeanInstaller {

    private static WeakReference wizardRef;

    private BeanInstaller() {
    }

    // --------

    /** Installs beans from given source type. Lets the user choose the source,
     * the beans, and the target category in a wizard. */
    public static void installBeans(String sourceType) {
        AddToPaletteWizard wizard = getAddWizard();
        if (wizard.show(sourceType))
            createPaletteItems(sourceType,
                               wizard.getSelectedBeans(),
                               wizard.getSelectedCategory());
    }

    /** Installs beans represented by given nodes (selected by the user). Lets
     * the user choose the palette category. */
    public static void installBeans(Node[] nodes) {
        final Map beanMap = new HashMap();
        for (int i=0; i < nodes.length; i++) {
            SourceCookie source = (SourceCookie)
                                      nodes[i].getCookie(SourceCookie.class);
            DataObject dobj = (DataObject) nodes[i].getCookie(DataObject.class);
            if (source == null || dobj == null)
                continue;

            ClassElement[] cls = source.getSource().getClasses();
            for (int j=0; j < cls.length; j++)
                if (cls[j].getName().getName().equals(dobj.getName())
                    && cls[j].isDeclaredAsJavaBean())
                {
                    beanMap.put(cls[j].getName().getFullName(),
                                dobj.getPrimaryFile());
                    break;
                }
        }

        if (beanMap.size() == 0) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("No JavaBean found under selected nodes.");
            DialogDisplayer.getDefault().notify(nd);
            return;
        }

        String category = CategorySelector.selectCategory();
        if (category == null)
            return; // canceled by user

        final FileObject categoryFolder = PaletteUtils.getPaletteFolder()
                                                       .getFileObject(category);
        try {
            Repository.getDefault().getDefaultFileSystem().runAtomicAction(
            new FileSystem.AtomicAction () {
                public void run() {
                    Iterator it = beanMap.keySet().iterator();
                    while (it.hasNext()) {
                        String classname = (String) it.next();
                        FileObject fo = (FileObject) beanMap.get(classname);
                        String[] classpath = getProjectOutput(fo);
                        if (classpath != null) {
                            try {
                                PaletteItemDataObject.createFile(
                                    categoryFolder,
                                    classname,
                                    PaletteItem.PROJECT_SOURCE,
                                    classpath);
                                // TODO check the class if it can be loaded?
                            }
                            catch (java.io.IOException ex) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            }
                        }
                    }
                }
            });
        }
        catch (java.io.IOException ex) {} // should not happen
    }

    /** Finds available JavaBeans in given JAR files. Looks for beans
     * specified in the JAR manifest only.
     * @return list of ItemInfo */
    static List findJavaBeansInJar(File[] jarFiles) {
        Map beans = null;

        for (int i=0; i < jarFiles.length; i++) {
            Manifest manifest;
            try {
                manifest = new JarFile(jarFiles[i]).getManifest();
            }
            catch (java.io.IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                continue;
            }
            if (manifest == null)
                continue;

            String jarPath = jarFiles[i].getAbsolutePath();
            Map entries = manifest.getEntries();
            Iterator it = entries.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                if (!key.endsWith(".class")) // NOI18N
                    continue;

                String value = ((Attributes)entries.get(key)).getValue("Java-Bean"); // NOI18N
                if (!"True".equalsIgnoreCase(value)) // NOI18N
                    continue;

                String classname =  key.substring(0, key.length()-6) // cut off ".class"
                                    .replace('\\', '/').replace('/', '.');
                if (classname.startsWith(".")) // NOI18N
                    classname = classname.substring(1);

                ItemInfo ii = new ItemInfo();
                ii.classname = classname;
                ii.source = jarPath;

                if (beans == null)
                    beans = new HashMap(100);
                beans.put(ii.classname, ii);
            }
        }

        return beans != null ? new ArrayList(beans.values()) : null;
    }

    /** Collects all classes under given roots that could be used as JavaBeans.
     * This method is supposed to search in JAR files or folders containing
     * built classes.
     * @return list of ItemInfo */
    static List findJavaBeans(File[] roots) {
        Map beans = new HashMap(100);

        for (int i=0; i < roots.length; i++) {
            FileObject foRoot = FileUtil.toFileObject(roots[i]);
            if (foRoot != null) {
                if (FileUtil.isArchiveFile(foRoot))
                    foRoot = FileUtil.getArchiveRoot(foRoot);
                if (foRoot != null && foRoot.isFolder())
                    scanFolderForBeans(foRoot, beans, roots[i].getAbsolutePath());
            }
        }

        return new ArrayList(beans.values());
    }

    // --------
    // private methods

    /** Installs given beans (described by ItemInfo in array). */
    private static void createPaletteItems(final String sourceType,
                                           final ItemInfo[] beans,
                                           String category)
    {
        if (beans.length == 0)
            return;

        final FileObject categoryFolder =
            PaletteUtils.getPaletteFolder().getFileObject(category);
        if (categoryFolder == null)
            return;

        try {
            Repository.getDefault().getDefaultFileSystem().runAtomicAction(
            new FileSystem.AtomicAction () {
                public void run() {
                    for (int i=0; i < beans.length; i++)
                        try {
                            PaletteItemDataObject.createFile(
                                categoryFolder,
                                beans[i].classname,
                                sourceType,
                                new String[] { beans[i].source} );
                            // TODO check the class if it can be loaded?
                        }
                        catch (java.io.IOException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                }
            });
        }
        catch (java.io.IOException ex) {} // should not happen
    }

    /** @return paths to project output roots (JARs) that are produced for
     * given file (which might be a source, or even part of the output)
     */
    private static String[] getProjectOutput(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null)
            return null; // the file is not in any project

        // find the project output (presumably a JAR file) where the given
        // source file is compiled (packed) to
        AntArtifact[] artifacts =
            AntArtifactQuery.findArtifactsByType(project, "jar"); // NOI18N
        if (artifacts.length == 0)
            return null;

        for (int i=0; i < artifacts.length; i++) {
            File outputFile = new File(
                artifacts[i].getScriptLocation().getParent()
                + File.separator
                + artifacts[i].getArtifactLocation().getPath());
            try {
                java.net.URL outputURL = outputFile.toURI().toURL();
                if (FileUtil.isArchiveFile(outputURL))
                    outputURL = FileUtil.getArchiveRoot(outputURL);
                FileObject sourceRoots[] =
                    SourceForBinaryQuery.findSourceRoot(outputURL);
                for (int j=0; j < sourceRoots.length; j++)
                    if (FileUtil.isParentOf(sourceRoots[j], fo))
                        return new String[] { outputFile.getAbsolutePath() };
            }
            catch (java.net.MalformedURLException ex) {} // should not happen
        }

        // if no output found for given source file then the file might not be
        // a source file ... but a binary output file - in this case return
        // simply all project outputs as there is no good way to recognize
        // the right one (and j2se project has just one output anyway)
        if (!fo.getExt().equals("class")) // NOI18N
            return null;

        String[] outputs = new String[artifacts.length];
        for (int i=0; i < artifacts.length; i++) {
            File outputFile = new File(
                artifacts[i].getScriptLocation().getParent()
                + File.separator
                + artifacts[i].getArtifactLocation().getPath());
            outputs[i] = outputFile.getAbsolutePath();
        }
        return outputs;
    }

    /** Recursive method scanning folders for classes (class files) that could
     * be JavaBeans. */
    private static void scanFolderForBeans(FileObject folder, Map beans, String root) {
        DataObject dobj;
        SourceCookie source;

        FileObject[] files = folder.getChildren();
        for (int i=0; i < files.length; i++) {
            FileObject fo = files[i];
            if (fo.isFolder()) {
                scanFolderForBeans(fo, beans, root);
            }
            else try {
                if ("class".equals(fo.getExt()) // NOI18N
                     && (dobj = DataObject.find(fo)) != null
                     && (source = (SourceCookie)dobj.getCookie(SourceCookie.class)) != null)
                {
                    ClassElement[] cls = source.getSource().getClasses();
                    for (int j=0; j < cls.length; j++)
                        if (cls[j].getName().getName().equals(dobj.getName())
                            && cls[j].isDeclaredAsJavaBean())
                        {
                            ItemInfo ii = new ItemInfo();
                            ii.classname = cls[j].getName().getFullName();
                            ii.source = root;
                            beans.put(ii.classname, ii);
                            break;
                        }
                }
            }
            catch (org.openide.loaders.DataObjectNotFoundException ex) {} // should not happen
        }
    }

    private static AddToPaletteWizard getAddWizard() {
        AddToPaletteWizard wizard = null;
        if (wizardRef != null)
            wizard = (AddToPaletteWizard) wizardRef.get();
        if (wizard == null) {
            wizard = new AddToPaletteWizard();
            wizardRef = new WeakReference(wizard);
        }
        return wizard;
    }

    // --------

    static class ItemInfo implements Comparable {
        String classname;
        String source; // full file path or library name

        public int compareTo(Object o) {
            ItemInfo ii = (ItemInfo) o;
            int i;
            i = classname.lastIndexOf('.');
            String name1 = i >= 0 ? classname.substring(i+1) : classname;
            i = ii.classname.lastIndexOf('.');
            String name2 = i >= 0 ? ii.classname.substring(i+1) : ii.classname;
            return name1.compareTo(name2);
        }
    }
}
