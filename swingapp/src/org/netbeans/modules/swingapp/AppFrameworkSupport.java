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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.swingapp;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;

/**
 * Utility class providing data and handling various operations related to the
 * Swing Application Framework.
 *
 * @author Tomas Pavek
 */
class AppFrameworkSupport {

    private static final String APPLICATION_RESOURCE_NAME = "org/jdesktop/application/Application.class"; // NOI18N

    private static final String SWINGAPP_ELEMENT = "swingapp"; // NOI18N
    private static final String SWINGAPP_NS = "http://www.netbeans.org/ns/form-swingapp/1"; // NOI18N
    private static final String APP_CLASS_ELEMENT = "application-class"; // NOI18N
    private static final String APP_CLASS_NAME_ATTR = "name"; // NOI18N

    // map to remember application class name for project of given source file
    private static Map<FileObject, String> appClassMap = new HashMap<FileObject, String>();
    
    /**
     * Checks if the given project uses the app framework. Technically it checks
     * the project classpath for Application class.
     * @param fileInProject some source file contained in the project
     * @return true if the project of given file uses app framework
     */
    static boolean isFrameworkEnabledProject(FileObject fileInProject) {
        String appclass = getApplicationClassName(fileInProject);
        boolean foundSwingAppLib = isFrameworkLibAvailable(fileInProject);
        if(foundSwingAppLib && appclass != null) { 
            return true;
        } else {
            return false;
        }
    }
    
    private static boolean isFrameworkLibAvailable(FileObject fileInProject) {
        ClassPath cp = ClassPath.getClassPath(fileInProject, ClassPath.EXECUTE);
        boolean foundSwingAppLib = (cp != null && cp.findResource(APPLICATION_RESOURCE_NAME) != null); // NOI18N
        return foundSwingAppLib;
    }

    static boolean isApplicationProject(Project project) {
        AuxiliaryConfiguration ac = project.getLookup().lookup(AuxiliaryConfiguration.class);
        return ac.getConfigurationFragment(SWINGAPP_ELEMENT, SWINGAPP_NS, true) != null;
        // [would be better to check for presence of valid application class in ac]
    }

    /**
     * Returns if the given project can use the app framework. Currently NBM
     * projects are not allowed to.
     * @param fileInProject some source file contained in the project
     * @return true if the project of given file can use app framework
     */
    static boolean projectCanUseFramework(FileObject fileInProject) {
        // not usable for NBM projects (maybe once it is in JDK)
        // hack: check project impl. class name
        Project p = FileOwnerQuery.getOwner(fileInProject);
        if (p != null && p.getClass().getName().startsWith("org.netbeans.modules.apisupport.") // NOI18N
                && p.getClass().getName().endsWith("Project")){ // NOI18N
            return false;
        }
        return true;
    }
    
    /**
     * Adds the app framework library to project classpath.
     * @param fileInProject some source file contained in the project
     */
    static boolean updateProjectClassPath(FileObject fileInProject) {
        Library lib = LibraryManager.getDefault().getLibrary("swing-app-framework"); // NOI18N
        try {
            ProjectClassPathModifier.addLibraries(new Library[] { lib }, fileInProject, ClassPath.EXECUTE);
            return true;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        }
    }

    static String getApplicationCode(FileObject srcFile) {
        String appClsName = getApplicationClassName(srcFile);
        return appClsName != null
            ? org.jdesktop.application.Application.class.getName() + ".getInstance(" + appClsName + ".class)" // NOI18N
            : null;
    }

    static String getActionMapCode(FileObject srcFile) {
        return getApplicationCode(srcFile) + ".getContext().getActionMap(" + srcFile.getName() + ".class, this)"; // NOI18N
    }

    /**
     * Finds the source file that represents the Application subclass of
     * the project. Returns the corresponding class name.
     * @param fileInProject some source file contained in the project
     * @return name of the application subclass for given project, or null
     */
    static String getApplicationClassName(FileObject fileInProject) {
        Project project = FileOwnerQuery.getOwner(fileInProject);
        AuxiliaryConfiguration ac = project.getLookup().lookup(AuxiliaryConfiguration.class);
        return getApplicationClassName(fileInProject, project, ac);
    }

    static String getApplicationClassName(Project project) {
        FileObject fileRep = getSourceRoot(project);
        if (fileRep != null) {
            AuxiliaryConfiguration ac = project.getLookup().lookup(AuxiliaryConfiguration.class);
            return getApplicationClassName(fileRep, project, ac);
        }
        return null;
    }

    private static String getApplicationClassName(FileObject fileInProject, Project project, AuxiliaryConfiguration ac) {
        String appClassName = null;
        org.w3c.dom.Element appEl = ac.getConfigurationFragment(SWINGAPP_ELEMENT, SWINGAPP_NS, true);
        boolean storedInProject = (appEl != null);
        boolean searched = false;

        if (storedInProject) { // get app class name stored in project config
            org.w3c.dom.Element clsEl = null;
            org.w3c.dom.NodeList children = appEl.getChildNodes();
            for (int i=0; i < children.getLength(); i++) {
                org.w3c.dom.Node n = children.item(i);
                if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                        && n.getNodeName().equals(APP_CLASS_ELEMENT)) {
                    clsEl = (org.w3c.dom.Element) n;
                    break;
                }
            }
            if (clsEl != null) {
                appClassName = clsEl.getAttribute(APP_CLASS_NAME_ATTR);
            }
        }

        if (appClassName == null) {
            if (appClassMap.containsKey(fileInProject)) {
                appClassName = appClassMap.get(fileInProject);
            } else {
                appClassName = findApplicationClass(fileInProject);
                searched = true;
            }
        }

        if (appClassName != null && !searched) { // verify cached class name
            ClassPath cp = ClassPath.getClassPath(fileInProject, ClassPath.SOURCE);
            if (cp.findResource(appClassName.replace('.', '/') + ".java") == null) { // NOI18N
                appClassName = findApplicationClass(fileInProject);
                searched = true;
            }
        }

        if (searched) { // possibly update project and cache
            if (storedInProject) {
                org.w3c.dom.Document xml = XMLUtil.createDocument(SWINGAPP_ELEMENT, SWINGAPP_NS, null, null);
                appEl = xml.createElementNS(SWINGAPP_NS, SWINGAPP_ELEMENT);
                if (appClassName != null) {
                    org.w3c.dom.Element clsEl = xml.createElement(APP_CLASS_ELEMENT);
                    clsEl.setAttribute(APP_CLASS_NAME_ATTR, appClassName);
                    appEl.appendChild(clsEl);
                }
                ac.putConfigurationFragment(appEl, true);
                try {
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (!fileInProject.isFolder() && (!storedInProject || appClassName == null)) {
                appClassMap.put(fileInProject, appClassName);
            }
        }
        return appClassName;
    }

    /**
     * @return corresponding class name for given source file
     */
    static String getClassNameForFile(FileObject fo) {
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        return cp.getResourceName(fo, '.', false);
    }

    static FileObject getFileForClass(FileObject fileInProject, String className) {
        ClassPath cp = ClassPath.getClassPath(fileInProject, ClassPath.SOURCE);
        return cp.findResource(className.replace('.', '/') + ".java"); // NOI18N
    }

    static ClassPath getSourcePath(Project project) {
        FileObject root = getSourceRoot(project);
        return root != null ? ClassPath.getClassPath(root, ClassPath.SOURCE) : null;
    }

    private static FileObject getSourceRoot(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        for (SourceGroup g : sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            return g.getRootFolder();
        }
        return null;
    }

    /**
     * Should be called when a source file is closed to clear associated cached
     * data.
     */
    static void fileClosed(FileObject srcFile) {
        appClassMap.remove(srcFile);
    }
    
    // -----
    // scanning project sources

    private static String findApplicationClass(FileObject fileInProject) {
        if (isFrameworkLibAvailable(fileInProject)) {
            ClassPath cp = ClassPath.getClassPath(fileInProject, ClassPath.SOURCE);
            return scanFolderForApplication(cp.findOwnerRoot(fileInProject));
        } else {
            return null;
        }
    }

    private static String scanFolderForApplication(FileObject folder) {
        List<FileObject> folders = null;
        for (FileObject fo : folder.getChildren()) {
            if (fo.isFolder()) { // dive into subfolders after scanning files
                if (folders == null) {
                    folders = new LinkedList<FileObject>();
                }
                folders.add(fo);
            } else if (fo.getExt().equalsIgnoreCase("java")) { // NOI18N
                String appClassName = getAppClassNameFromFile(fo);
                if (appClassName != null) {
                    return appClassName;
                }
            }
        }
        if (folders != null) {
            for (FileObject fo : folders) {
                String appClassName = scanFolderForApplication(fo);
                if (appClassName != null) {
                    return appClassName;
                }
            }
        }
        return null;
    }

    private static String getAppClassNameFromFile(FileObject fo) {
        final String fileName = fo.getName();
        final String[] result = new String[1];
        JavaSource js = JavaSource.forFileObject(fo);
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {
                public void cancel() {
                }
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    for (Tree t: controller.getCompilationUnit().getTypeDecls()) {
                        if (t.getKind() == Tree.Kind.CLASS) {
                            ClassTree classT = (ClassTree) t;
                            if (fileName.equals(classT.getSimpleName().toString())) {
                                Tree superT = classT.getExtendsClause();
                                if (superT != null) {
                                    Trees trees = controller.getTrees();
                                    TreePath superTPath = trees.getPath(controller.getCompilationUnit(), superT);
                                    Element superEl = trees.getElement(superTPath);
                                    if (superEl != null && superEl.getKind() == ElementKind.CLASS) {
                                        String superClassName = ((TypeElement)superEl).getQualifiedName().toString();
                                        for (String appCls : getKnownAppClassNames()) {
                                            if (appCls.equals(superClassName)) {
                                                TreePath classTPath = trees.getPath(controller.getCompilationUnit(), classT);
                                                TypeElement classEl = (TypeElement) trees.getElement(classTPath);
                                                result[0] = classEl.getQualifiedName().toString();
                                                return;
                                            }
                                        }
                                        // TODO need a general way to recognize Application subclass
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return result[0];
    }

    private static List<String> knownAppClassNames;
    private static List<String> getKnownAppClassNames() {
        if (knownAppClassNames == null) {
            knownAppClassNames = new LinkedList<String>();
            knownAppClassNames.add(org.jdesktop.application.Application.class.getName());
            knownAppClassNames.add(org.jdesktop.application.SingleFrameApplication.class.getName());
        }
        return knownAppClassNames;
    }
}
