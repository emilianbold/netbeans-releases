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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.modules.java.j2seproject.ui.customizer.MainClassChooser;
import org.netbeans.modules.javacore.ClassIndex;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Miscellaneous utilities for the j2seproject module.
 * @author  Jiri Rechtacek
 */
public class J2SEProjectUtil {
    private J2SEProjectUtil () {}
    
    /**
     * Returns the property value evaluated by J2SEProject's PropertyEvaluator.
     *
     * @param p project
     * @param value of property
     * @return evaluated value of given property or null if the property not set or
     * if the project doesn't provide AntProjectHelper
     */    
    public static Object getEvaluatedProperty(Project p, String value) {
        if (value == null) {
            return null;
        }
        J2SEProject j2seprj = (J2SEProject) p.getLookup().lookup(J2SEProject.class);
        if (j2seprj != null) {
            return j2seprj.evaluator().evaluate(value);
        } else {
            return null;
        }
    }
    
    /** Check if the given file object represents a source with the main method.
     * 
     * @param fo source
     * @return true if the source contains the main method
     */
    public static boolean hasMainMethod(FileObject fo) {
        // support for unit testing
        if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue ();
        }
        if (fo == null) {
            // ??? maybe better should be thrown IAE
            return false;
        }
        
        boolean has = false;
        JavaModel.getJavaRepository ().beginTrans (false);
        
        try {
            JavaModel.setClassPath(fo);
            Resource res = JavaModel.getResource (fo);
            assert res != null : "Resource found for FileObject " + fo;
            has = !res.getMain().isEmpty();
        } finally {
            JavaModel.getJavaRepository ().endTrans ();
        }
        return has;
    }

    /** Returns list of FQN of classes contains the main method.
     * 
     * @param roots the classpath roots of source to start find
     * @return list of names of classes, e.g, [sample.project1.Hello, sample.project.app.MainApp]
     */
    public static List/*String*/ getMainClasses (FileObject[] roots) {
        List result = new ArrayList ();
        for (int i=0; i<roots.length; i++) {
            getMainClasses(roots[i], result);
        }
        return result;
    }
    
    /** Returns list of FQN of classes contains the main method.
     * 
     * @param root the root of source to start find
     * @param addInto list of names of classes, e.g, [sample.project1.Hello, sample.project.app.MainApp]
     */
    private static void getMainClasses (FileObject root, List/*<String>*/ addInto) {
        JavaModel.getJavaRepository ().beginTrans (false);
        try {
            JavaModelPackage mofPackage = JavaModel.getJavaExtent(root);
            ClassIndex index = ClassIndex.getIndex (mofPackage);
            //Resource[] res = index.findResourcesForIdentifier ("main"); // NOI18N
            Collection col = index.findResourcesForIdent ("main"); // NOI18N
            Object[] arr = col.toArray ();

            if (arr == null) {
                // no main classes
                return;
            }

            for (int i = 0; i < arr.length; i++) {
                Resource res = (Resource)arr[i];
                Iterator mainIt=res.getMain().iterator();
                
                while (mainIt.hasNext()) {
                    JavaClass jcls=(JavaClass)mainIt.next();
                    
                    addInto.add(jcls.getName());
                }
            }
        } finally {
            JavaModel.getJavaRepository ().endTrans (false);
        }        
    }
    
    /** Returns if the given class name exists under the sources root and
     * it's a main class.
     * 
     * @param className FQN of class
     * @param roots roots of sources
     * @return true if the class name exists and it's a main class
     */
    public static boolean isMainClass(String className, FileObject[] roots) {
        return isMainClass (className, ClassPathSupport.createClassPath(roots));        
    }
    
    
    public static boolean isMainClass (String className, ClassPath cp) {
        // support for unit testing
        if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue ();
        }
        JavaModel.getJavaRepository ().beginTrans (false);
        boolean isMain = false;        
        try {
            Type clazz;
            
            JavaModel.setClassPath (cp);
            clazz=JavaModel.getDefaultExtent().getType().resolve(className);
            if (clazz != null) {
                isMain =  clazz.getResource().getMain().contains(clazz);
            }
        } finally {
            JavaModel.getJavaRepository ().endTrans ();
        }
        return isMain;
    }
    
    
    /**
     * Creates an URL of a classpath or sourcepath root
     * For the existing directory it returns the URL obtained from {@link File#toUri()}
     * For archive file it returns an URL of the root of the archive file
     * For non existing directory it fixes the ending '/'
     * @param root the file of a root
     * @param offset a path relative to the root file or null (eg. src/ for jar:file:///lib.jar!/src/)" 
     * @return an URL of the root
     * @throws MalformedURLException if the URL cannot be created
     */
    public static URL getRootURL (File root, String offset) throws MalformedURLException {
        URL url = root.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        } else if (!root.exists()) {
            url = new URL(url.toExternalForm() + "/"); // NOI18N
        }
        if (offset != null) {
            assert offset.endsWith("/");    //NOI18N
            url = new URL(url.toExternalForm() + offset); // NOI18N
        }
        return url;
    }
}
