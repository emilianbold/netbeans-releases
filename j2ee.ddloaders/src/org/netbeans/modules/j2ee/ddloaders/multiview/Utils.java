/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.BrowseFolders;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.EjbGenerationUtil;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.EntityAndSessionGenerator;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.entity.EntityGenerator;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.session.SessionGenerator;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.CMPFieldNode;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.EntityNode;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.methodcontroller.EntityMethodController;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.src.ClassElement;
import org.openide.src.Identifier;
import org.openide.src.MethodElement;
import org.openide.src.MethodParameter;
import org.openide.src.SourceException;
import org.openide.src.Type;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.StringTokenizer;

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
        if (packageName.length() > 0 && packageName.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tokenizer = new StringTokenizer(packageName, "."); // NOI18N
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.length() == 0) {
                return false;
            }
            if (!isJavaIdentifier(token)) {
                return false;
            }
        }
        return true;
    }

    public static void removeInterface(ClassElement beanClass, String interfaceName) {
        Identifier[] interfaces = beanClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaceName.equals(interfaces[i].getFullName())) {
                try {
                    beanClass.removeInterface(Identifier.create(interfaceName));
                } catch (SourceException ex) {
                    Utils.notifyError(ex);
                }
            }
        }
    }

    public static void removeClassFile(FileObject ejbJarFile, String className) {
        FileObject sourceFile = getSourceFile(ejbJarFile, className);
        if (sourceFile != null) {
            try {
                sourceFile.delete();
            } catch (IOException e) {
                notifyError(e);
            }
        }
    }

    public static FileObject getPackageFile(FileObject ejbJarFile, String packageName) {
        return findSourceResource(ejbJarFile, packageName.replace('.', '/'));
    }

    public static String getPackage(String ejbClass) {
        return ejbClass.substring(0, ejbClass.lastIndexOf('.'));
    }

    public static void addInterfaces(FileObject ejbJarFile, EntityAndSession ejb, boolean local) {
        EntityAndSessionGenerator generator;
        if (ejb instanceof Entity) {
            generator = new EntityGenerator();
        } else {
            generator = new SessionGenerator();
        }
        String packageName = getPackage(ejb.getEjbClass());
        FileObject packageFile = getPackageFile(ejbJarFile, packageName);
        String ejbName = ejb.getEjbName();
        try {
            String componentInterfaceName;
            String businessInterfaceName;
            if (local) {
                componentInterfaceName = EjbGenerationUtil.getLocalName(packageName, ejbName);
                componentInterfaceName =
                        generator.generateLocal(packageName, packageFile, componentInterfaceName, ejbName);
                String homeInterfaceName = EjbGenerationUtil.getLocalHomeName(packageName, ejbName);
                homeInterfaceName =
                        generator.generateLocalHome(packageName, packageFile, homeInterfaceName,
                                componentInterfaceName, ejbName);
                ejb.setLocal(componentInterfaceName);
                ejb.setLocalHome(homeInterfaceName);
                businessInterfaceName = EjbGenerationUtil.getLocalBusinessInterfaceName(packageName, ejbName);
            } else {
                componentInterfaceName = EjbGenerationUtil.getRemoteName(packageName, ejbName);
                componentInterfaceName =
                        generator.generateRemote(packageName, packageFile, componentInterfaceName, ejbName);
                String homeInterfaceName = EjbGenerationUtil.getHomeName(packageName, ejbName);
                homeInterfaceName =
                        generator.generateHome(packageName, packageFile, homeInterfaceName, componentInterfaceName,
                                ejbName);
                ejb.setRemote(componentInterfaceName);
                ejb.setHome(homeInterfaceName);
                businessInterfaceName = EjbGenerationUtil.getBusinessInterfaceName(packageName, ejbName);
            }
            ClassElement beanClass = getBeanClass(ejbJarFile, ejb);
            Identifier[] interfaces = beanClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                if (businessInterfaceName.equals(interfaces[i].getFullName())) {
                    ClassElement componentInterface = getClassElement(ejbJarFile, componentInterfaceName);
                    componentInterface.addInterface(Identifier.create(businessInterfaceName));
                    SaveCookie sc = (SaveCookie) componentInterface.getCookie(SaveCookie.class);
                    if (sc != null) {
                        sc.save();
                    }
                    return;
                }
            }
            generator.generateBusinessInterfaces(packageName, packageFile, businessInterfaceName, ejbName,
                    ejb.getEjbClass(), componentInterfaceName);
        } catch (IOException e) {
            notifyError(e);
        } catch (SourceException e) {
            notifyError(e);
        }
    }

    public static void notifyError(Exception ex) {
        NotifyDescriptor ndd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(ndd);
    }

    public static FileObject findSourceResource(FileObject ejbJarFile, String resourceName) {
        return getSourceClassPath(ejbJarFile).findResource(resourceName);
    }

    public static void activateRenameClassUI(String fullClassName) {
        JavaClass javaClass = (JavaClass) JavaModel.getDefaultExtent().getType().resolve(fullClassName);
    }

    public static void activateMoveClassUI(FileObject ejbJarFile, String fullClassName, SourceGroup sourceGroup) {
        JavaClass javaClass = (JavaClass) JavaModel.getDefaultExtent().getType().resolve(fullClassName);
        MoveClassAction moveClassAction = new MoveClassAction();
        moveClassAction.init(ejbJarFile, javaClass, sourceGroup);
        moveClassAction.performAction(null);
    }

    public static ClassElement getBeanClass(FileObject ejbJarFile, final Ejb ejb) {
        String ejbClassName = ejb.getEjbClass();
        return getClassElement(ejbJarFile, ejbClassName);
    }

    public static CMPFieldNode createFieldNode(FileObject ejbJarFile, Entity entity, CmpField field) {
        ClassElement beanClass = getBeanClass(ejbJarFile, entity);
        EntityMethodController ec = (EntityMethodController) EntityMethodController.createFromClass(beanClass);
        return new CMPFieldNode(field, ec, ejbJarFile);
    }

    public static FileObject getSourceFile(FileObject ejbJarFile, String className) {
        return findSourceResource(ejbJarFile, className.replace('.', '/') + ".java");
    }

    public static EntityNode createEntityNode(FileObject ejbJarFile, Entity entity) {
        EjbJar ejbJar = null;
        try {
            ejbJar = DDProvider.getDefault().getDDRoot(ejbJarFile);
        } catch (IOException e) {
            notifyError(e);
            return null;
        }
        return new EntityNode(entity, ejbJar, getSourceClassPath(ejbJarFile), ejbJarFile);
    }

    public static ClassPath getSourceClassPath(FileObject ejbJarFile) {
        EjbJarProject enterpriseProject = (EjbJarProject) FileOwnerQuery.getOwner(ejbJarFile);
        return enterpriseProject.getEjbModule().getJavaSources();
    }

    public static MethodElement getBusinessMethod(ClassElement interfaceElement, MethodElement method) {
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

    public static MethodElement getSetterMethod(ClassElement beanClass, String fieldName, MethodElement getterMethod) {
        return getterMethod == null ?
                null : EntityMethodController.getSetterMethod(beanClass, fieldName, getterMethod);
    }

    public static MethodElement getGetterMethod(ClassElement beanClass, String fieldName) {
        return EntityMethodController.getGetterMethod(beanClass, fieldName);
    }

    protected static ClassElement getBusinessInterface(String compInterfaceName, FileObject ejbJarFile,
            ClassElement beanClass) {
        ClassElement compInterface = getClassElement(ejbJarFile, compInterfaceName);
        if (compInterface == null) {
            return null;
        }
        // get method interfaces
        java.util.List compInterfaces = new LinkedList(Arrays.asList(compInterface.getInterfaces()));

        // look for common candidates
        compInterfaces.retainAll(Arrays.asList(beanClass.getInterfaces()));

        if (compInterfaces.isEmpty()) {
            return compInterface;
        }

        ClassElement business = getClassElement(ejbJarFile, compInterfaces.get(0).toString());
        return business == null ? compInterface : business;
    }

    public static ClassElement getClassElement(FileObject ejbJarFile, String className) {
        if (className == null) {
            return null;
        }
        FileObject src = getSourceFile(ejbJarFile, className);
        ClassElement classElement = ClassElement.forName(className, src);
        return classElement;
    }
}
