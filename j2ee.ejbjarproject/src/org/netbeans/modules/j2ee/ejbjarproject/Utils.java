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

package org.netbeans.modules.j2ee.ejbjarproject;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.CMPFieldNode;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.EntityNode;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.EntityAndSessionGenerator;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.EjbGenerationUtil;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.session.SessionGenerator;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.entity.EntityGenerator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.src.ClassElement;
import org.openide.src.Identifier;
import org.openide.src.SourceException;

import java.io.File;
import java.io.IOException;

public class Utils {

    public static String toClasspathString(File[] classpathEntries) {
        if (classpathEntries == null) {
            return "";
        }
        StringBuffer classpath = new StringBuffer();
        for (int i = 0; i < classpathEntries.length; i++) {
            classpath.append(classpathEntries[i].getAbsolutePath());
            if (i + 1 < classpathEntries.length) {
                classpath.append(":"); // NOI18N
            }
        }
        return classpath.toString();
    }

    public static void notifyError(Exception ex) {
        NotifyDescriptor ndd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(ndd);
    }

    public static ClassElement getBeanClass(FileObject ejbJarFile, final Ejb ejb) {
        String ejbClassName = ejb.getEjbClass();
        return getClassElement(ejbJarFile, ejbClassName);
    }

    public static ClassElement getClassElement(FileObject ejbJarFile, String className) {
        if (className == null) {
            return null;
        }
        FileObject src = getSourceFile(ejbJarFile, className);
        return ClassElement.forName(className, src);
    }

    public static FileObject getSourceFile(FileObject ejbJarFile, String className) {
        return findSourceResource(ejbJarFile, className.replace('.', '/') + ".java");
    }

    public static FileObject getPackageFile(FileObject ejbJarFile, String packageName) {
        return findSourceResource(ejbJarFile, packageName.replace('.', '/'));
    }

    private static FileObject findSourceResource(FileObject ejbJarFile, String resourceName) {
        EjbJarProject enterpriseProject = (EjbJarProject) FileOwnerQuery.getOwner(ejbJarFile);
        ClassPath classPath = enterpriseProject.getEjbModule().getJavaSources();
        return classPath.findResource(resourceName);
    }

    public static EntityNode createEntityNode(FileObject ejbJarFile, Entity entity) {
        EjbJar ejbJar = null;
        try {
            ejbJar = DDProvider.getDefault().getDDRoot(ejbJarFile);
        } catch (IOException e) {
            notifyError(e);
            return null;
        }
        EjbJarProject enterpriseProject = (EjbJarProject) FileOwnerQuery.getOwner(ejbJarFile);
        EjbJarProvider ejbModule = enterpriseProject.getEjbModule();
        ClassPath classPath = ejbModule.getJavaSources();
        return new EntityNode(entity, ejbJar, classPath, ejbJarFile);
    }

    public static CMPFieldNode createFieldNode(FileObject ejbJarFile, Entity entity, CmpField field) {
        ClassElement beanClass = getBeanClass(ejbJarFile, entity);
        EntityMethodController ec = (EntityMethodController) EntityMethodController.createFromClass(beanClass);
        return new CMPFieldNode(field, ec, ejbJarFile);
    }

    public static void removeInterface(ClassElement beanClass, String interfaceName) {
        Identifier[] interfaces = beanClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaceName.equals(interfaces[i].getFullName())) {
                try {
                    beanClass.removeInterface(Identifier.create(interfaceName));
                } catch (SourceException ex) {
                    notifyError(ex);
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

    public static String getPackage(String fullClassName) {
        return fullClassName.substring(0, fullClassName.lastIndexOf('.'));
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
                componentInterfaceName = generator.generateLocal(packageName, packageFile, componentInterfaceName, ejbName);
                String homeInterfaceName = EjbGenerationUtil.getLocalHomeName(packageName, ejbName);
                homeInterfaceName = generator.generateLocalHome(packageName, packageFile, homeInterfaceName, componentInterfaceName, ejbName);
                ejb.setLocal(componentInterfaceName);
                ejb.setLocalHome(homeInterfaceName);
                businessInterfaceName = EjbGenerationUtil.getLocalBusinessInterfaceName(packageName, ejbName);
            } else {
                componentInterfaceName = EjbGenerationUtil.getRemoteName(packageName, ejbName);
                componentInterfaceName = generator.generateRemote(packageName, packageFile, componentInterfaceName, ejbName);
                String homeInterfaceName = EjbGenerationUtil.getHomeName(packageName, ejbName);
                homeInterfaceName = generator.generateHome(packageName, packageFile, homeInterfaceName, componentInterfaceName, ejbName);
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
}
