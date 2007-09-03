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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.lang.ref.WeakReference;
import java.util.jar.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
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
        final List<ClassSource> beans = new LinkedList<ClassSource>();
        final List<String> unableToInstall = new LinkedList<String>();
        final List<String> noBeans = new LinkedList<String>();
        for (int i=0; i < nodes.length; i++) {            
            DataObject dobj = nodes[i].getCookie(DataObject.class);
            if (dobj == null)
                continue;

            final FileObject fo = dobj.getPrimaryFile();            
            JavaClassHandler handler = new JavaClassHandler() {
                public void handle(String className, String problem) {
                    if (problem == null) {
                        ClassSource classSource = 
                                ClassPathUtils.getProjectClassSource(fo, className);
                        if (classSource == null) {
                            // Issue 47947
                            unableToInstall.add(className);
                        } else {
                            beans.add(classSource);
                        }
                    } else {
                        noBeans.add(className);
                        noBeans.add(problem);
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

        String message = null;
        if (beans.size() == 0) {
            message = PaletteUtils.getBundleString("MSG_noBeansUnderNodes"); // NOI18N
        }
        if (noBeans.size() != 0) {
            Iterator<String> iter = noBeans.iterator();
            while (iter.hasNext()) {
                String className = iter.next();
                String format = iter.next();
                String msg = MessageFormat.format(format, className);
                if (message != null) {
                    message += '\n';
                } else {
                    message = ""; // NOI18N
                }
                message += msg;
            }
        }
        if (message != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notify(nd);
        }
        if (beans.size() == 0) return;

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
        Map<String,ItemInfo> beans = null;

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
                    beans = new HashMap<String,ItemInfo>(100);
                beans.put(ii.classname, ii);
            }
        }

        return beans != null ? new ArrayList<ItemInfo>(beans.values()) : null;
    }

    /** Collects all classes under given roots that could be used as JavaBeans.
     * This method is supposed to search in JAR files or folders containing
     * built classes.
     * @return list of ItemInfo */
    static List findJavaBeans(File[] roots) {
        Map<String,ItemInfo> beans = new HashMap<String,ItemInfo>(100);

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

        return new ArrayList<ItemInfo>(beans.values());
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
    private static void scanFolderForBeans(FileObject folder, final Map<String,ItemInfo> beans, final String root) {
        JavaClassHandler handler = new JavaClassHandler() {
            public void handle(String className, String problem) {
                if (problem == null) {
                    ItemInfo ii = new ItemInfo();
                    ii.classname = className;
                    ii.source = root;
                    beans.put(ii.classname, ii);
                }
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
            processClassFile(fileObject, handler);
        } else if ("java".equals(fileObject.getExt())) { // NOI18N
            processJavaFile(fileObject, handler);
        }
    }     
    
    /**
     * finds bean's FQN if there is any.
     * @param file file to search a bean
     * @return null or the fqn 
     */
    public static String findJavaBeanName(FileObject file) {
        final String[] fqn = new String[1];
        scanFileObject(null, file, new JavaClassHandler() {
            public void handle(String className, String problem) {
                if (problem == null) {
                    fqn[0] = className;
                }
            }
        });
        return fqn[0];
    }
    
    private static void processJavaFile(final FileObject javaFO, final JavaClassHandler handler) {
        try {
            JavaSource js = JavaSource.forFileObject(javaFO);
            js.runUserActionTask(new CancellableTask<CompilationController>() {
                public void cancel() {
                }

                public void run(CompilationController ctrl) throws Exception {
                    ctrl.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement clazz = findClass(ctrl, javaFO.getName());
                    if (clazz != null) {
                        handler.handle(clazz.getQualifiedName().toString(), isDeclaredAsJavaBean(clazz));
                    }
                }
            }, true);
        } catch (IOException ex) {
            Logger.getLogger(BeanInstaller.class.getClass().getName()).
                    log(Level.SEVERE, javaFO.toString(), ex);
        }
    }
    
    private static TypeElement findClass(CompilationController ctrl, String className) {
        for (Tree decl : ctrl.getCompilationUnit().getTypeDecls()) {
            if (className.equals(((ClassTree) decl).getSimpleName().toString())) {
                TreePath path = ctrl.getTrees().getPath(ctrl.getCompilationUnit(), decl);
                TypeElement clazz = (TypeElement) ctrl.getTrees().getElement(path);
                return clazz;
            }
        }
        return null;
    }
    
    private static void processClassFile(FileObject classFO, JavaClassHandler handler) {
        try {
            // XXX rewrite this to use javax.lang.model.element.* as soon as JavaSource introduce .class files support
            InputStream is = null;
            ClassFile clazz;
            try {
                is = classFO.getInputStream();
                clazz = new ClassFile(is, false);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            if (clazz != null) {
                handler.handle(clazz.getName().getExternalName(), isDeclaredAsJavaBean(clazz));
            }
        } catch (IOException ex) {
            Logger.getLogger(BeanInstaller.class.getClass().getName()).
                    log(Level.SEVERE, classFO.toString(), ex);
        }
        
    }
        
    public static String isDeclaredAsJavaBean(TypeElement clazz) {
        if (ElementKind.CLASS != clazz.getKind()) {
            return PaletteUtils.getBundleString("MSG_notAClass"); // NOI18N
        }

        Set<javax.lang.model.element.Modifier> mods = clazz.getModifiers();
        if (mods.contains(javax.lang.model.element.Modifier.ABSTRACT)) {
            return PaletteUtils.getBundleString("MSG_abstractClass"); // NOI18N
        }

        if (!mods.contains(javax.lang.model.element.Modifier.PUBLIC)) {
            return PaletteUtils.getBundleString("MSG_notPublic"); // NOI18N
        }
        
        for (Element member : clazz.getEnclosedElements()) {
            mods = member.getModifiers();
            if (ElementKind.CONSTRUCTOR == member.getKind() &&
                    mods.contains(javax.lang.model.element.Modifier.PUBLIC) &&
                    ((ExecutableElement) member).getParameters().isEmpty()) {
                return null;
            }
        }
        
        return PaletteUtils.getBundleString("MSG_noPublicConstructor"); // NOI18N
    }
    
    public static String isDeclaredAsJavaBean(ClassFile clazz) {
        int access = clazz.getAccess();
        
        if (Modifier.isInterface(access) || clazz.isAnnotation() ||
                clazz.isEnum() || clazz.isSynthetic()) {
            return PaletteUtils.getBundleString("MSG_notAClass"); // NOI18N
        }
        
        if (Modifier.isAbstract(access)) {
            return PaletteUtils.getBundleString("MSG_abstractClass"); // NOI18N
        }
        
        if (!Modifier.isPublic(access)) {
            return PaletteUtils.getBundleString("MSG_notPublic"); // NOI18N
        }

        for (Object omethod : clazz.getMethods()) {
            Method method = (Method) omethod;
            if (method.isPublic() && method.getParameters().isEmpty() &&
                    "<init>".equals(method.getName())) { // NOI18N
                return null;
            }
        }
        return PaletteUtils.getBundleString("MSG_noPublicConstructor"); // NOI18N
    }
    
    private static AddToPaletteWizard getAddWizard() {
        AddToPaletteWizard wizard = null;
        if (wizardRef != null)
            wizard = (AddToPaletteWizard) wizardRef.get();
        if (wizard == null) {
            wizard = new AddToPaletteWizard();
            wizardRef = new WeakReference<AddToPaletteWizard>(wizard);
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
        public void handle(String className, String problem);        
    }
    
}
