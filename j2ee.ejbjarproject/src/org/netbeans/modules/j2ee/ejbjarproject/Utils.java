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

import java.io.File;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.src.ClassElement;
import org.openide.filesystems.FileObject;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

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
}
