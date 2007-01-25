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

package org.netbeans.modules.j2ee.ejbcore;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * This class contains common functionality for code generation
 * @author Chris Webster
 * @author Martin Adamek
 */
public class EjbGenerationUtil {

    private static final String[] EJB_NAME_CONTEXTS = new String[] {
                EnterpriseBeans.SESSION,
                EnterpriseBeans.ENTITY,
                EnterpriseBeans.MESSAGE_DRIVEN
    };
    
    public static String getFullClassName(String pkg, String className) {
        return (pkg==null||pkg.length()==0)?className:pkg+"."+className; //NOI18N
    }
    
    public static String getBaseName(String fullClassName) {
        return fullClassName.substring(fullClassName.lastIndexOf('.')+1); //NOI18N
    }
    
    public static String[] getPackages(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set<String> pkgs = new TreeSet<String>();
        for (int i = 0; i < groups.length; i++) {
            findPackages(groups[i].getRootFolder(),"", pkgs);
        }
        return pkgs.toArray(new String[pkgs.size()]);
    }
    
    private static void findPackages (FileObject root, String curPkg, Set<String> pkgs) {
        for (FileObject kid : root.getChildren()) {
	        String name = curPkg + (curPkg.length() != 0 ? "." : "") + kid.getName();
            pkgs.add (name);
	        findPackages (kid, name, pkgs);
        }
    }
    
    public static boolean isEjbNameInDD(String ejbName, EjbJar ejbJar) {
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        Object ejb = null;
        if (beans != null) {
            for (int i = 0; i < EJB_NAME_CONTEXTS.length; i++) {
                ejb = beans.findBeanByName(EJB_NAME_CONTEXTS[i], Ejb.EJB_NAME, ejbName);
                if (ejb != null) {
                    break;
                }
            }
        }
        return beans != null && ejb != null;
    }
    
    public static FileObject getPackageFileObject(SourceGroup location, String pkgName, Project project) {
        String relativePkgName = pkgName.replace('.', '/');
        FileObject fileObject = null;
        fileObject = location.getRootFolder().getFileObject(relativePkgName);
        if (fileObject != null) {
            return fileObject;
        } else {
            File rootFile = FileUtil.toFile(location.getRootFolder());
            File pkg = new File(rootFile,relativePkgName);
            pkg.mkdirs();
            fileObject = location.getRootFolder().getFileObject(relativePkgName);
        }
        return fileObject;
    }

    public static String getSelectedPackageName(FileObject targetFolder) {
        Project project = FileOwnerQuery.getOwner(targetFolder);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = FileUtil.getRelativePath(groups [i].getRootFolder(), targetFolder);
        }
        if (packageName != null) {
            packageName = packageName.replaceAll("/", ".");
        }
        return packageName+"";
    }

}
