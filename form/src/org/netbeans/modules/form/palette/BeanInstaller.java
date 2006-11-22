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

package org.netbeans.modules.form.palette;

import java.lang.ref.WeakReference;
import java.util.jar.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.Method;

import org.openide.*;
import org.openide.nodes.Node;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;

import org.netbeans.modules.form.project.*;

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
        final List beans = new LinkedList();
        final List unableToInstall = new LinkedList();                        
        for (int i=0; i < nodes.length; i++) {            
            DataObject dobj = (DataObject) nodes[i].getCookie(DataObject.class);
            if (dobj == null)
                continue;

            final FileObject fo = dobj.getPrimaryFile();            
            JavaClassHandler handler = new JavaClassHandler() {
                public void handle(ClassFile javaClass) {
                    ClassSource classSource = 
                            ClassPathUtils.getProjectClassSource(fo, javaClass.getName().getExternalName());
                    if (classSource == null) {
                        // Issue 47947
                        unableToInstall.add(javaClass.getName().getExternalName());
                    } else {
                        beans.add(classSource);
                    }                
                } 
            };            
            scanFileObject(fo.getParent(), fo, handler);
        }
        
        if (unableToInstall.size() > 0) {
            Iterator iter = unableToInstall.iterator();
            StringBuffer sb = new StringBuffer();
            while (iter.hasNext()) {
                sb.append(iter.next()+", "); // NOI18N
            }
            sb.delete(sb.length()-2, sb.length());
            String messageFormat = PaletteUtils.getBundleString("MSG_cannotInstallBeans"); // NOI18N
            String message = MessageFormat.format(messageFormat, new Object[] {sb.toString()});
            NotifyDescriptor nd = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notify(nd);
            if (beans.size() == 0) return;
        }

        if (beans.size() == 0) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(PaletteUtils.getBundleString("MSG_noBeansUnderNodes")); // NOI18N
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
                    Iterator it = beans.iterator();
                    while (it.hasNext()) {
                        ClassSource classSource = (ClassSource)it.next();
                        try {
                            PaletteItemDataObject.createFile(categoryFolder, classSource);
                            // TODO check the class if it can be loaded?
                        }
                        catch (java.io.IOException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
            Iterator it = entries.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                String key = (String)entry.getKey();
                if (!key.endsWith(".class")) // NOI18N
                    continue;

                String value = ((Attributes)entry.getValue()).getValue("Java-Bean"); // NOI18N
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
                if (foRoot != null && foRoot.isFolder()) {
                    scanFolderForBeans(foRoot, beans, roots[i].getAbsolutePath());                                            
                }                    
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
                    String[] cpTypes = new String[] { sourceType };
                    for (int i=0; i < beans.length; i++)
                        try {
                            PaletteItemDataObject.createFile(
                                categoryFolder,
                                new ClassSource(beans[i].classname,
                                                cpTypes,
                                                new String[] { beans[i].source} ));
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

    /** Recursive method scanning folders for classes (class files) that could
     * be JavaBeans. */
    private static void scanFolderForBeans(FileObject folder, final Map beans, final String root) {
        JavaClassHandler handler = new JavaClassHandler() {
            public void handle(ClassFile javaClass) {
                ItemInfo ii = new ItemInfo();
                ii.classname = javaClass.getName().getExternalName();
                ii.source = root;
                beans.put(ii.classname, ii);                            
            }
        };
                    
        FileObject[] files = folder.getChildren();
        for (int i=0; i < files.length; i++) {
            FileObject fo = files[i];
            if (fo.isFolder()) {
                scanFolderForBeans(fo, beans, root);
            }
            else try {
                if ("class".equals(fo.getExt()) // NOI18N
                     && (DataObject.find(fo) != null))
                {                   
                    scanFileObject(folder, fo, handler);
                }
            }
            catch (org.openide.loaders.DataObjectNotFoundException ex) {} // should not happen
        }
    }    
    
    private static void scanFileObject(FileObject folder, final FileObject fileObject, final JavaClassHandler handler) {          
        if ("class".equals(fileObject.getExt())) { // NOI18N
            processClassTypeElement(fileObject, handler);
        }
    }     
    
    private static void processClassTypeElement(FileObject classFO, JavaClassHandler handler) {
        try {
            // XXX rewrite this to use javax.lang.model.element.* as soon as JavaSource introduce .class files support
            ClassFile clazz = new ClassFile(classFO.getInputStream(), false);
            if (isDeclaredAsJavaBean(clazz)) {
                handler.handle(clazz);
            }
        } catch (IOException ex) {
            Logger.getLogger(BeanInstaller.class.getClass().getName()).
                    log(Level.SEVERE, classFO.toString(), ex);
        }
        
    }
        
    public static boolean isDeclaredAsJavaBean(ClassFile clazz) {
        int access = clazz.getAccess();
        
        if (!Modifier.isPublic(access) || Modifier.isAbstract(access) ||
                Modifier.isInterface(access) || clazz.isAnnotation() ||
                clazz.isEnum() || clazz.isSynthetic() ) {
            return false;
        }
        
        for (Object omethod : clazz.getMethods()) {
            Method method = (Method) omethod;
            if (method.isPublic() && !method.isAbstract() &&
                    method.getParameters().isEmpty() && "<init>".equals(method.getName())) { // NOI18N
                return true;
            }
        }
        return false;
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
    
    private interface JavaClassHandler {        
        public void handle(ClassFile javaClass);        
    }
    
}
