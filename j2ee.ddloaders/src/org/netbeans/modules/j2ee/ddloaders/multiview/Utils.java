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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.BrowseFolders;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
//import org.netbeans.modules.java.JavaDataObject;
//import org.netbeans.modules.java.ui.nodes.SourceNodes;
//import org.netbeans.modules.javacore.api.JavaModel;
//import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
//import org.netbeans.spi.java.classpath.ClassPathFactory;
//import org.netbeans.spi.java.classpath.ClassPathImplementation;
//import org.netbeans.spi.java.classpath.PathResourceImplementation;
//import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import org.netbeans.api.java.classpath.ClassPath;

/**
 * @author pfiala
 */
public class Utils {

    public static final String ICON_BASE_DD_VALID =
            "org/netbeans/modules/j2ee/ddloaders/resources/DDValidIcon"; // NOI18N
    public static final String ICON_BASE_DD_INVALID =
            "org/netbeans/modules/j2ee/ddloaders/resources/DDInvalidIcon"; // NOI18N
    public static final String ICON_BASE_EJB_MODULE_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/EjbModuleNodeIcon"; // NOI18N
    public static final String ICON_BASE_ENTERPRISE_JAVA_BEANS_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/EjbContainerNodeIcon"; // NOI18N
    public static final String ICON_BASE_SESSION_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/SessionNodeIcon"; // NOI18N
    public static final String ICON_BASE_ENTITY_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/EntityNodeIcon"; // NOI18N
    public static final String ICON_BASE_MESSAGE_DRIVEN_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/MessageNodeIcon"; // NOI18N
    public static final String ICON_BASE_MISC_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/MiscNodeIcon"; // NOI18N

    private static BrowseFolders.FileObjectFilter imageFileFilter = new BrowseFolders.FileObjectFilter() {
        public boolean accept(FileObject fileObject) {
            return fileObject.getMIMEType().startsWith("image/"); // NOI18N
        }
    };

    public static String browseIcon(EjbJarMultiViewDataObject dataObject) {
        FileObject fileObject = org.netbeans.modules.j2ee.ddloaders.multiview.ui.BrowseFolders.showDialog(
                dataObject.getSourceGroups(), imageFileFilter);
        String relativePath;
        if (fileObject != null) {
            FileObject projectDirectory = dataObject.getProjectDirectory();
            relativePath = FileUtil.getRelativePath(projectDirectory, fileObject);
        } else {
            relativePath = null;
        }
        return relativePath;
    }

    public static Color getErrorColor() {
        // inspired by org.openide.WizardDescriptor
        Color c = UIManager.getColor("nb.errorForeground"); //NOI18N
        return c == null ? new Color(89, 79, 191) : c;
    }

    public static JTree findTreeComponent(Component component) {
        if (component instanceof JTree) {
            return (JTree) component;
        }
        if (component instanceof Container) {
            Component[] components = ((Container) component).getComponents();
            for (int i = 0; i < components.length; i++) {
                JTree tree = findTreeComponent(components[i]);
                if (tree != null) {
                    return tree;
                }
            }
        }
        return null;
    }

    public static void scrollToVisible(JComponent component) {
        org.netbeans.modules.xml.multiview.Utils.scrollToVisible(component);
    }

    public static String getBundleMessage(String messageId) {
        return NbBundle.getMessage(Utils.class, messageId);
    }

    public static String getBundleMessage(String messageId, Object param1) {
        return NbBundle.getMessage(Utils.class, messageId, param1);
    }

    public static String getBundleMessage(String messageId, Object param1, Object param2) {
        return NbBundle.getMessage(Utils.class, messageId, param1, param2);
    }

    public static String getBundleMessage(String messageId, Object param1, Object param2, Object param3) {
        return NbBundle.getMessage(Utils.class, messageId, param1, param2, param3);
    }

    public static boolean isJavaIdentifier(String id) {
        return Utilities.isJavaIdentifier(id);
    }

    /**
     * Returns true, if the passed string can be used as a qualified identifier.
     * it does not check for semantic, only for syntax.
     * The function returns true for any sequence of identifiers separated by
     * dots.
     */
    public static boolean isValidPackageName(String packageName) {
        String[] strings = packageName.split("[.]");  // NOI18N
        if (strings.length == 0) {
            return false;
        }
        for (int i = 0; i < strings.length; i++) {
            if (!isJavaIdentifier(strings[i])) {
                return false;
            }
        }
        return packageName.charAt(packageName.length() - 1) != '.';
    }

    public static void removeClass(ClassPath classPath, String className) {
        FileObject sourceFile = getSourceFile(classPath, className);
        if (sourceFile != null) {
//            try {
////                JavaDataObject.find(sourceFile).delete();
//            } catch (DataObjectNotFoundException e) {
//                notifyError(e);
//            } catch (IOException e) {
//                notifyError(e);
//            }
        }
    }

    public static FileObject getPackageFile(ClassPath classPath, String packageName) {
        return classPath.findResource(packageToPath(packageName));
    }

    private static String packageToPath(String packageName) {
        return packageName.replace('.', '/');
    }

    public static String getPackage(String ejbClass) {
        final int i = ejbClass.lastIndexOf('.');
        if (i < 0) {
            return "";
        } else {
            return ejbClass.substring(0, i);
        }

    }

    public static void notifyError(Exception ex) {
        NotifyDescriptor ndd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(ndd);
    }

    public static FileObject getSourceFile(ClassPath classPath, String className) {
        return classPath.findResource(packageToPath(className) + ".java");
    }

//    public static Node createEntityNode(FileObject ejbJarFile, ClassPath classPath, Entity entity) {
//        //todo:
//        //classPath = getSourceClassPath(ejbJarFile);
//        EjbJar ejbJar;
//        try {
//            ejbJar = DDProvider.getDefault().getDDRoot(ejbJarFile);
//        } catch (IOException e) {
//            notifyError(e);
//            return null;
//        }
//        return J2eeProjectView.getEjbNodesFactory().createEntityNode (entity, ejbJar, classPath, ejbJarFile);
//    }

//    public static ClassPath getSourceClassPath(FileObject ejbJarFile) {
//        Sources sources = ProjectUtils.getSources(FileOwnerQuery.getOwner(ejbJarFile));
//        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
//        ClassPath srcClassPath = ClassPathFactory.createClassPath(new ClassPathImpl(groups));
//        ClassPath bootClassPath = ClassPath.getClassPath(ejbJarFile, ClassPath.BOOT);
//        return ClassPathSupport.createProxyClassPath(new ClassPath[]{srcClassPath, bootClassPath});
//    }

//    public static Method getMethod(JavaClass javaClass, Method method) {
//        if (javaClass == null || method == null) {
//            return null;
//        } else {
//            List parameters = new LinkedList();
//            for (Iterator it = method.getParameters().iterator(); it.hasNext();) {
//                parameters.add(((Parameter) it.next()).getType());
//            }
//            return javaClass.getMethod(method.getName(), parameters, false);
//        }
//    }

//    public static void addMethod(JavaClass javaClass, Method prototype) {
//        addMethod(javaClass, prototype, false);
//    }

//    public static void addMethod(JavaClass javaClass, Method prototype, boolean remote) {
//        if (prototype != null) {
//            addMethod(javaClass, prototype, remote, prototype.getModifiers());
//        }
//    }
//
//    public static void addMethod(JavaClass interfaceClass, Method prototype, boolean remote, int modifiers) {
//        if (interfaceClass == null || prototype == null) {
//            return;
//        }
//        if (getMethod(interfaceClass, prototype) != null) {
//            return;
//        }
//        beginJmiTransaction(true);
//        boolean rollback = true;
//        try {
//            Method method = JMIUtils.createMethod(interfaceClass);
//            method.setName(prototype.getName());
//            Type type = prototype.getType();
//            if (type != null) {
//                method.setType(JMIUtils.resolveType(type.getName()));
//            }
//            JMIUtils.replaceParameters(method, prototype.getParameters());
//            method.setModifiers(modifiers);
//            if (remote) {
//                JMIUtils.addException(method, RemoteException.class.getName());
//            }
//            for (Iterator it = prototype.getExceptionNames().iterator(); it.hasNext();) {
//                MultipartId mpId= (MultipartId) it.next();
//                String exceptionName = mpId.getName();
//                if (!"RemoteException".equals(exceptionName) && !"java.rmi.RemoteException".equals(exceptionName)) {
//                    JMIUtils.addException(method, exceptionName);
//                }
//            }
//            getContents(interfaceClass).add(method);
//            rollback = false;
//        } finally {
//            endJmiTransaction(rollback);
//        }
//    }
//
//    public static List getContents(JavaClass javaClass) {
//        return ((JavaClass) JMIUtils.resolveType(javaClass.getName())).getContents();
//    }
//
//    public static void removeMethod(JavaClass javaClass, Method method) {
//        if (javaClass == null || method == null) {
//            return;
//        }
//        beginJmiTransaction(true);
//        boolean rollback = true;
//        try {
//            getContents(javaClass).remove(getMethod(javaClass, method));
//            rollback = false;
//        } finally {
//            endJmiTransaction(rollback);
//        }
//    }
//
//    private static Lookup createClassRefactoringLookup(String fullClassName) {
//        Node node = SourceNodes.getExplorerFactory().createClassNode((JavaClass) JMIUtils.resolveType(fullClassName));
//        InstanceContent ic = new InstanceContent();
//        ic.add(node);
//        return new AbstractLookup(ic);
//    }
//
//    public static void activateRenameClassUI(String fullClassName) {
//        Lookup lookup = createClassRefactoringLookup(fullClassName);
//        final Action action = RefactoringActionsFactory.renameAction().createContextAwareInstance(lookup);
//        action.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
//    }
//
//    public static void activateMoveClassUI(String fullClassName) {
//        Lookup lookup = createClassRefactoringLookup(fullClassName);
//        final Action action = RefactoringActionsFactory.moveClassAction().createContextAwareInstance(lookup);
//        action.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
//    }
//
//    public static void renameMethod(Method method, String name) {
//        if (method != null) {
//            method.setName(name);
//        }
//    }

    public static String getEjbDisplayName(Ejb ejb) {
        String name = ejb.getDefaultDisplayName();
        if (name == null) {
            name = ejb.getEjbName();
            if (name == null) {
                name = " ";  // NOI18N
            }
        }
        return name;
    }

//    /**
//     * Opens source of given class
//     * @param ejbJarFile
//     * @param javaClass
//     */
//    public static void openEditorFor(FileObject ejbJarFile, JavaClass javaClass) {
//        if (javaClass == null) {
//            return;
//        }
//        FileObject sourceFile = getSourceFile(getSourceClassPath(ejbJarFile), javaClass.getName());
//        if (sourceFile != null) {
//            DataObject javaDo;
//            try {
//                javaDo = DataObject.find(sourceFile);
//            } catch (DataObjectNotFoundException e) {
//                DialogDisplayer.getDefault().notify(
//                        new NotifyDescriptor.Message(getBundleMessage("MSG_sourceNotFound")));
//                return;
//            }
//            OpenCookie cookie = (OpenCookie) javaDo.getCookie(OpenCookie.class);
//            if (cookie != null) {
//                cookie.open();
//            }
//        }
//    }

    /**
     * Make sure that the code will run in AWT dispatch thread
     * @param runnable
     */
    public static void runInAwtDispatchThread(Runnable runnable) {
        org.netbeans.modules.xml.multiview.Utils.runInAwtDispatchThread(runnable);
    }

//    public static void changeParameterType(final Method method, Type type) {
//        if (method != null) {
//            Parameter parameter = (Parameter) method.getParameters().get(0);
//            parameter.setType(type);
//        }
//    }
//
//    public static void beginJmiTransaction(boolean writeAccess) {
//        JavaModel.getJavaRepository().beginTrans(writeAccess);
//    }
//
//    public static void endJmiTransaction(boolean rollback) {
//        JavaModel.getJavaRepository().endTrans(rollback);
//    }

//    private static class ClassPathImpl implements ClassPathImplementation {
//
//        private List resources = new LinkedList();
//
//        private class PathResourceImpl implements PathResourceImplementation {
//
//            URL[] roots;
//
//            public PathResourceImpl(URL root) {
//                this.roots = new URL[]{root};
//            }
//
//            public URL[] getRoots() {
//                return roots;
//            }
//
//            public ClassPathImplementation getContent() {
//                return ClassPathImpl.this;
//            }
//
//            public void addPropertyChangeListener(PropertyChangeListener listener) {
//            }
//
//            public void removePropertyChangeListener(PropertyChangeListener listener) {
//            }
//        }
//
//        public ClassPathImpl(SourceGroup[] groups) {
//            for (int i = 0; i < groups.length; i++) {
//                SourceGroup group = groups[i];
//                try {
//                    resources.add(new PathResourceImpl(group.getRootFolder().getURL()));
//                } catch (FileStateInvalidException e) {
//                    notifyError(e);
//                }
//            }
//        }
//
//        public java.util.List /*<PathResourceImplementation>*/ getResources() {
//            return resources;
//        }
//
//        public void addPropertyChangeListener(PropertyChangeListener listener) {
//        }
//
//        public void removePropertyChangeListener(PropertyChangeListener listener) {
//        }
//    }
}
