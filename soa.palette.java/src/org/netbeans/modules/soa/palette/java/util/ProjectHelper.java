/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.soa.palette.java.util;

import org.netbeans.api.project.Project;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author gpatil
 */
public class ProjectHelper {
    public static Project getProject(JTextComponent comp) {
        JavaSource javaSource = JavaSource.forDocument(comp.getDocument());
        FileObject fo = javaSource.getFileObjects().iterator().next();
        return FileOwnerQuery.getOwner(fo);
    }

    public static FileObject getFileObject(JTextComponent comp) {
        JavaSource javaSource = JavaSource.forDocument(comp.getDocument());
        return javaSource.getFileObjects().iterator().next();
    }
    
    public static ClassLoader getClassLoader(Project prj){
        FileObject fo = prj.getProjectDirectory();
        String buildClassDir = ProjectHelper.getProjectProperty(prj, "build.classes.dir"); //NOI18N
        FileObject root = fo.getFileObject(buildClassDir); //NOI18N TODO use ${build.classes.dir} insteand "build/jar"
        SourceGroup[] sgs = ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);        
        ClassPath bldCls = ClassPathSupport.createClassPath(root);
        ClassPath compileClassPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.COMPILE);
        ClassPath sourceClassPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.SOURCE);
        ClassPath bootClassPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.BOOT);
        ClassPath classPath = ClassPathSupport.createProxyClassPath(
                new ClassPath[]{compileClassPath, bldCls, bootClassPath, sourceClassPath});
                                
        return classPath.getClassLoader(false);
    }

    public static ClassLoader getClassLoader(FileObject fo){
        ClassPath compileClassPath = ClassPath.getClassPath(fo, ClassPath.COMPILE);
        ClassPath sourceClassPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        ClassPath bootClassPath = ClassPath.getClassPath(fo, ClassPath.BOOT);
        ClassPath classPath = ClassPathSupport.createProxyClassPath(
                new ClassPath[]{compileClassPath, sourceClassPath, bootClassPath});
        return classPath.getClassLoader(false);
    }
      
    
    public static AntProjectHelper getAntProjectHelper(Project project) {
        try {
            Method getAntProjectHelperMethod = project.getClass().getMethod(
                    "getAntProjectHelper"); //NOI18N
            if (getAntProjectHelperMethod != null) {
                AntProjectHelper helper = (AntProjectHelper) 
                        getAntProjectHelperMethod.invoke(project);

                return helper;
            }
        } catch (NoSuchMethodException nme) {
            Exceptions.printStackTrace(nme);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }
    
    public static ReferenceHelper getReferenceHelper(Project project) {
        try {
            Method getReferenceHelperMethod = project.getClass().getMethod(
                    "getReferenceHelper"); //NOI18N
            if (getReferenceHelperMethod != null) {
                ReferenceHelper helper = (ReferenceHelper) 
                        getReferenceHelperMethod.invoke(project);

                return helper;
            }
        } catch (NoSuchMethodException nme) {
            Exceptions.printStackTrace(nme);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }    
    
    private static String getProperty(Project prj, String filePath, 
            String name) {
        AntProjectHelper aph = getAntProjectHelper(prj);
        EditableProperties ep = aph.getProperties(filePath);
        String str = null;
        String value = ep.getProperty(name);
        if (value != null) {
            PropertyEvaluator pe = aph.getStandardPropertyEvaluator();
            str = pe.evaluate(value);
        }
        return str;
    }

    private static void saveProperty(Project prj, String filePath, String name, 
            String value) {
        AntProjectHelper aph = getAntProjectHelper(prj);
        EditableProperties ep = aph.getProperties(filePath);
        if (value != null) {
            ep.put(name, value);
            aph.putProperties(filePath, ep);
        }
    }

    public static String getProjectProperty(Project prj, String prop) {
        return getProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, prop);
    }
    
    public static void saveProjectProperty(Project prj, String prop, 
            String value) {
        saveProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, prop, 
                value);
    }    
    
    private static String slashify(String path) {
        if (path.endsWith(File.separator)) {
            return path;
        } else {
            return path + File.separatorChar;
        }
    }

    public static String relativizeFile(File basedir, File file) {
        if (basedir.isFile()) {
            String msg = NbBundle.getMessage(ProjectHelper.class, "MSG_AgrCanNotBeNull"); //NOI18N            
            throw new IllegalArgumentException(msg + basedir); // NOI18N
        }
        if (basedir.equals(file)) {
            return "."; // NOI18N
        }
        StringBuffer b = new StringBuffer();
        File base = basedir;
        String filepath = file.getAbsolutePath();
        while (!filepath.startsWith(slashify(base.getAbsolutePath()))) {
            base = base.getParentFile();
            if (base == null) {
                return null;
            }
            if (base.equals(file)) {
                b.append(".."); // NOI18N
                return b.toString();
            }
            b.append("../"); // NOI18N
        }
        URI u = base.toURI().relativize(file.toURI());
        assert !u.isAbsolute() : u + " from " + basedir + " and " + file + " with common root " + base;
        b.append(u.getPath());
        if (b.charAt(b.length() - 1) == '/') {
            // file is an existing directory and file.toURI ends in /
            // we do not want the trailing slash
            b.setLength(b.length() - 1);
        }
        return b.toString();
    }
 
    public static boolean isAbsolutePath(File file){
        return file.isAbsolute();
    }
    
    public static String Absolute2RelativePathStr(File base, File absPath){
        String relPath = null;
        if (isAbsolutePath(absPath)){
            relPath = relativizeFile(base, absPath);
        } else {
            relPath = absPath.getPath();
        }
        
        return relPath;
    }
    
    public static File Relative2AbsolutePath(File base, String relPath){
        File relPathFile = new File(relPath);
        File absPath = null;
        if (!isAbsolutePath(relPathFile)){
            absPath = new File(base, relPath);
        } else {
            absPath = relPathFile;
        }
        
        return absPath;
    }       
}
