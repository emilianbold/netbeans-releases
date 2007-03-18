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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.visualweb.insync.java.JavaClass;
import org.netbeans.modules.visualweb.insync.java.JavaUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

final class FacesRefactoringUtils {
    
    private static final String JSP_MIME_TYPE = "text/x-jsp"; // NOI18N
    private static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N
    private static final List<String> specialFolderNames = 
        Collections.unmodifiableList(Arrays.asList(
            new String[] {
                    "META-INF"   // NOI18N
                    ,"WEB-INF"   // NOI18N
                    ,"resources" // NOI18N
            }
        ));
       
    static boolean isJavaFile(FileObject f) {
        return JAVA_MIME_TYPE.equals(f.getMIMEType()); //NOI18N
    }
    
    static boolean isJspFile(FileObject f) {
        return JSP_MIME_TYPE.equals(f.getMIMEType()); //NOI18N
    }
    
    static boolean isSpecialFolderName(String folderName) {
        return specialFolderNames.contains(folderName);
    }
    
    static boolean isVisualWebJspFile(FileObject ffileObject) {
        // Is it a Jsp file
        if (isFileUnderDocumentRoot(ffileObject)) {
            if (isJspFile(ffileObject)) {            
                FileObject javaFileObject = FacesModel.getJavaForJsp(ffileObject);
                if (javaFileObject != null) {
                    return true;
                }                
            }
        }
        return false;
    }
    
    static boolean isFileUnderDocumentRoot(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p==null) {
            return false;
        }
        // Is project a Jsf project
        if (JsfProjectUtils.isJsfProject(p)) {
            FileObject documentRoot = JsfProjectUtils.getDocumentRoot(p);
            if (documentRoot != null) {
                return FileUtil.isParentOf(documentRoot, fo);
            }
        }
        return false;
    }
    
    static boolean isFileUnderPageBeanRoot(FileObject fo) {
        if (isFileInJsfProject(fo)) {
            Project p = FileOwnerQuery.getOwner(fo);
            if (p==null) {
                return false;
            }
            FileObject pageBeanRoot = JsfProjectUtils.getPageBeanRoot(p);
            if (pageBeanRoot != null) {
                return FileUtil.isParentOf(pageBeanRoot, fo);
            }
        }
        return false;
    }
    
    static boolean isFileInJsfProject(FileObject f) {
        // Any project owner?
        Project p = FileOwnerQuery.getOwner(f);            
        if (p==null) {
            return false;
        }
        return JsfProjectUtils.isJsfProject(p);
    }
       
    static boolean isOpenProject(Project p) {
        // Any project owner?
        Project[] opened = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i<opened.length; i++) {
            // Is project open
            if (p==opened[i]) {                
                return true;
            }
        }
        return false;
    }
    
    static boolean isJavaFileObjectOfInterest(FileObject fo) {
        if (isFileUnderPageBeanRoot(fo)) {
            if (isOnSourceClasspath(fo)) {
                if (isJavaFile(fo)) {
                    FacesModel facesModel = FacesModel.getFacesModel(fo);
                    if (facesModel != null && !facesModel.isBusted()) {
                        JavaUnit javaUnit = facesModel.getJavaUnit();
                        if (javaUnit != null) {
                            JavaClass javaClass = javaUnit.getJavaClass();
                            for (String className : FacesModel.managedBeanNames) {
                                if (javaClass.isSubTypeOf(className)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    static boolean isOnSourceClasspath(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p==null) return false;
        Project[] opened = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i<opened.length; i++) {
            if (p==opened[i]) {
                SourceGroup[] gr = ProjectUtils.getSources(p).getSourceGroups("java"); // NOI18N
                for (int j = 0; j < gr.length; j++) {
                    if (fo==gr[j].getRootFolder()) return true;
                    if (FileUtil.isParentOf(gr[j].getRootFolder(), fo))
                        return true;
                }
                return false;
            }
        }
        return false;
    }

    public static boolean isClasspathRoot(FileObject fo) {
        return fo.equals(ClassPath.getClassPath(fo, ClassPath.SOURCE).findOwnerRoot(fo));
    }
    
    public static String getPackageName(FileObject folder) {
        return ClassPath.getClassPath(
                folder, ClassPath.SOURCE)
                .getResourceName(folder, '.', false);
    }
        
    static String getName(Dictionary dict) {
        if (dict==null) 
            return null;
        return (String) dict.get("name"); //NOI18N
    }
}
