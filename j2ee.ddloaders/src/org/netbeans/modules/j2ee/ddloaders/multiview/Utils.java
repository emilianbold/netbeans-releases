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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.BrowseFolders;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.EntityNode;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.methodcontroller.EntityMethodController;
import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.ui.nodes.SourceNodes;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.src.ClassElement;
import org.openide.src.Identifier;
import org.openide.src.MethodElement;
import org.openide.src.MethodParameter;
import org.openide.src.SourceException;
import org.openide.src.Type;
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
     * Removes ampersand marking a mnemonic character from the given text.
     * This method uses {@link Mnemonics#findMnemonicAmpersand} to find
     * position of the marker.
     *
     * @param  text  text to remove the marker from
     * @return  given text with the marker removed;
     *          or the original <code>String</code> instance
     *          if the given text contains no marker
     */
    public static String removeMnemonicMarker(String text) {
        final int pos = org.openide.awt.Mnemonics.findMnemonicAmpersand(text);
        
        switch (pos) {
            case -1:
                return text;
            case 0:
                return text.substring(1);
            default:
                return (pos == text.length() - 1)
                       ? text.substring(0, pos)
                       : text.substring(0, pos) + text.substring(pos + 1);
        }
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

    public static void removeInterface(ClassElement classElement, String interfaceName) {
        Identifier[] interfaces = classElement.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaceName.equals(interfaces[i].getFullName())) {
                Identifier identifier = Identifier.create(interfaceName);
                removeInterface(classElement, identifier);
            }
        }
    }

    public static void removeInterface(ClassElement classElement, Identifier identifier) {
        try {
            classElement.removeInterface(identifier);
        } catch (SourceException ex) {
            Utils.notifyError(ex);
        }
    }

    public static void removeClass(ClassPath classPath, String className) {
        FileObject sourceFile = getSourceFile(classPath, className);
        if (sourceFile != null) {
            try {
                JavaDataObject.find(sourceFile).delete();
            } catch (DataObjectNotFoundException e) {
                notifyError(e);
            } catch (IOException e) {
                notifyError(e);
            }
        }
    }

    public static FileObject getPackageFile(ClassPath classPath, String packageName) {
        return classPath.findResource(packageToPath(packageName));
    }

    public static String packageToPath(String packageName) {
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

    public static EntityNode createEntityNode(FileObject ejbJarFile, ClassPath classPath, Entity entity) {
        EjbJar ejbJar;
        try {
            ejbJar = DDProvider.getDefault().getDDRoot(ejbJarFile);
        } catch (IOException e) {
            notifyError(e);
            return null;
        }
        return new EntityNode(entity, ejbJar, classPath, ejbJarFile);
    }

    public static ClassPath getSourceClassPath(FileObject ejbJarFile) {
        Sources sources = ProjectUtils.getSources(FileOwnerQuery.getOwner(ejbJarFile));
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        return ClassPathFactory.createClassPath(new ClassPathImpl(groups));
    }

    public static MethodElement getMethod(ClassElement interfaceElement, MethodElement method) {
        if (interfaceElement == null || method == null) {
            return null;
        } else {
            MethodParameter[] parameters = method.getParameters();
            Type[] paramTypes = new Type[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                paramTypes[i] = parameters[i].getType();
            }
            return interfaceElement.getMethod(method.getName(), paramTypes);
        }
    }

    public static void addMethod(ClassElement interfaceElement, MethodElement method) {
        addMethod(interfaceElement, method, false);
    }

    public static void addMethod(ClassElement interfaceElement, MethodElement method, boolean remote) {
        if (method != null) {
            addMethod(interfaceElement, method, remote, method.getModifiers());
        }
    }

    public static void addMethod(ClassElement interfaceElement, MethodElement method, boolean remote, int modifiers) {
        if (interfaceElement == null || method == null) {
            return;
        }
        if (getMethod(interfaceElement, method) != null) {
            return;
        }
        method = (MethodElement) method.clone();
        try {
            method.setModifiers(modifiers);
            if (remote) {
                addExceptionIfNecessary(method, RemoteException.class.getName());
            }
            interfaceElement.addMethod(method);
        } catch (SourceException e) {
            Utils.notifyError(e);
        }
    }

    private static void addExceptionIfNecessary(MethodElement me, String exceptionName) throws SourceException {
        Identifier[] exceptions = me.getExceptions();
        boolean containsException = false;
        for (int i = 0; i < exceptions.length; i++) {
            String curExName = exceptions[i].getFullName();
            containsException |= exceptionName.equals(curExName);
        }
        if (!containsException) {
            Identifier[] newEx = new Identifier[exceptions.length + 1];
            System.arraycopy(exceptions, 0, newEx, 0, exceptions.length);
            newEx[newEx.length - 1] = Identifier.create(exceptionName);
            me.setExceptions(newEx);
        }
    }

    public static void removeBusinessMethod(ClassElement interfaceElement, MethodElement method) {
        if (interfaceElement == null || method == null) {
            return;
        }
        MethodElement businessMethod = getMethod(interfaceElement, method);
        if (businessMethod == null) {
            return;
        }
        try {
            interfaceElement.removeMethod(businessMethod);
        } catch (SourceException e) {
            Utils.notifyError(e);
        }
    }

    public static ClassElement getClassElement(ClassPath classPath, String className) {
        return ClassElement.forName(className, getSourceFile(classPath, className));
    }

    public static JavaClass resolveJavaClass(String fullClassName) {
        return (JavaClass) JavaModel.getDefaultExtent().getType().resolve(fullClassName);
    }

    private static Lookup createClassRefactoringLookup(String fullClassName) {
        Node node = SourceNodes.getExplorerFactory().createClassNode(resolveJavaClass(fullClassName));
        InstanceContent ic = new InstanceContent();
        ic.add(node);
        return new AbstractLookup(ic);
    }

    public static void activateRenameClassUI(String fullClassName) {
        Lookup lookup = createClassRefactoringLookup(fullClassName);
        final Action action = RefactoringActionsFactory.renameAction().createContextAwareInstance(lookup);
        action.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
    }

    public static void activateMoveClassUI(String fullClassName) {
        Lookup lookup = createClassRefactoringLookup(fullClassName);
        final Action action = RefactoringActionsFactory.moveClassAction().createContextAwareInstance(lookup);
        action.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
    }

    public static String getMethodName(String fieldName, boolean get) {
        return EntityMethodController.getMethodName(fieldName, get);
    }

    public static void renameMethod(MethodElement method, Identifier identifier) {
        if (method != null) {
            try {
                method.setName(identifier);
            } catch (SourceException e) {
                notifyError(e);
            }
        }
    }

    public static void removeMethod(ClassElement classElement, MethodElement method) {
        if (classElement != null) {
            method = getMethod(classElement, method);
            if (method != null) {
                try {
                    classElement.removeMethod(method);
                } catch (SourceException e) {
                    Utils.notifyError(e);
                }
            }
        }
    }

    public static String getEjbDisplayName(Ejb ejb) {
        String name = ejb.getDefaultDisplayName();
        if (name == null) {
            name = ejb.getEjbName();
        }
        return name;
    }

    /**
     * Opens source of given class
     * @param ejbJarFile
     * @param classElement
     */
    public static void openEditorFor(FileObject ejbJarFile, ClassElement classElement) {
        if (classElement == null) {
            return;
        }
        FileObject sourceFile = getSourceFile(getSourceClassPath(ejbJarFile), classElement.getVMName());
        if (sourceFile != null) {
            DataObject javaDo;
            try {
                javaDo = DataObject.find(sourceFile);
            } catch (DataObjectNotFoundException e) {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(getBundleMessage("MSG_sourceNotFound")));
                return;
            }
            OpenCookie cookie = (OpenCookie) javaDo.getCookie(OpenCookie.class);
            if (cookie != null) {
                cookie.open();
            }
        }
    }

    /**
     * Make sure that the code will run in AWT dispatch thread
     * @param runnable
     */
    public static void runInAwtDispatchThread(Runnable runnable) {
        org.netbeans.modules.xml.multiview.Utils.runInAwtDispatchThread(runnable);
    }

    public static void changeParameterType(final MethodElement method, Type type) {
        if (method != null) {
            MethodParameter[] parameters = method.getParameters();
            parameters[0].setType(type);
            try {
                method.setParameters(parameters);
            } catch (SourceException e) {
                notifyError(e);
            }
        }
    }

    private static class ClassPathImpl implements ClassPathImplementation {

        private List resources = new LinkedList();

        private class PathResourceImpl implements PathResourceImplementation {

            URL[] roots;

            public PathResourceImpl(URL root) {
                this.roots = new URL[]{root};
            }

            public URL[] getRoots() {
                return roots;
            }

            public ClassPathImplementation getContent() {
                return ClassPathImpl.this;
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {
            }

            public void removePropertyChangeListener(PropertyChangeListener listener) {
            }
        }

        public ClassPathImpl(SourceGroup[] groups) {
            for (int i = 0; i < groups.length; i++) {
                SourceGroup group = groups[i];
                try {
                    resources.add(new PathResourceImpl(group.getRootFolder().getURL()));
                } catch (FileStateInvalidException e) {
                    notifyError(e);
                }
            }
        }

        public java.util.List /*<PathResourceImplementation>*/ getResources() {
            return resources;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
}
