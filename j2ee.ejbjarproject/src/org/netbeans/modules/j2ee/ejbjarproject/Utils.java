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
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.CMPFieldNode;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.EntityNode;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.methodcontroller.EntityMethodController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.src.ClassElement;

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
        EjbJarProject enterpriseProject = (EjbJarProject) FileOwnerQuery.getOwner(ejbJarFile);
        ClassPath classPath = enterpriseProject.getEjbModule().getJavaSources();
        FileObject src = classPath.findResource(className.replace('.', '/') + ".java");
        return ClassElement.forName(className, src);
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
}
