/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * TestUtil.java
 *
 * Created on October 17, 2000, 5:09 PM
 */
package org.netbeans.modules.junit;
import org.openide.*;
import org.openide.filesystems.*;
import org.openide.execution.*;
import org.openide.src.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import java.lang.reflect.*;
import java.util.*;
/**
 *
 * @author  rmatous
 * @version 1.0
 */
class TestUtil extends Object {
    static private final String JAVA_EXT                        = "java";
    static private final String CLS_SUFFIX                      = "Test";
    static private final String CLS_FULL_SUFFIX                 = CLS_SUFFIX + "." + JAVA_EXT;
    static private final String SUITE_SUFFIX                    = "Suite";
    static private final String SUITE_FULL_SUFFIX               = SUITE_SUFFIX + "." + JAVA_EXT;
    static private final String SUITE_ROOT_NAME                 = "Root";
    
    static public String getTestClassFullName(FileObject foSourceFile) {
        return foSourceFile.getPackageName('/') + CLS_FULL_SUFFIX;
    }
    static public String getTestClassName(FileObject foSourceFile) {
        return foSourceFile.getName() + CLS_SUFFIX;
    }
    
    static public String getTestSuitFullName(FileObject foPackage) {
        StringBuffer name = new StringBuffer();
        
        name.append(foPackage.getPackageName('/'));
        if (name.length() != 0)
            name.append("/");
        
        if (foPackage.getName().length() == 0)
            name.append(SUITE_ROOT_NAME);
        else {
            name.append(foPackage.getName().substring(0, 1).toUpperCase());
            name.append(foPackage.getName().substring(1));
        }
        name.append(SUITE_FULL_SUFFIX);
        return name.toString();
    }
    static public String getTestSuitName(FileObject foPackage) {
        StringBuffer name = new StringBuffer();
        
        if (foPackage.getName().length() == 0)
            name.append(SUITE_ROOT_NAME);
        else {
            name.append(foPackage.getName().substring(0, 1).toUpperCase());
            name.append(foPackage.getName().substring(1));
        }
        name.append(SUITE_SUFFIX);
        return  name.toString();
    }
    
    static public FileObject getFileObjectFromNode(Node node) {
        ClassElement    ce;
        DataObject      dO;
        DataFolder      df;
        
        dO = (DataObject) node.getCookie(DataObject.class);
        if (null != dO)
            return dO.getPrimaryFile();
        df = (DataFolder) node.getCookie(DataFolder.class);
        if (null != df)
            return df.getPrimaryFile();
        
        ce = (ClassElement) node.getCookie(ClassElement.class);
        if (null != ce) {
            // find the parent DataObject, which node belongs to
            while (null != (node = node.getParentNode())) {
                if (null != (dO = (DataObject) node.getCookie(DataObject.class)))
                    return dO.getPrimaryFile();
            }
        }
        return null;
    }
    
    static public boolean isTestClassFile(String packageName) {
        return packageName.endsWith(CLS_SUFFIX);
    }
    
    static public boolean isTestSuiteFile(String packageName) {
        return packageName.endsWith(SUITE_SUFFIX);
    }
   static Executor findExecutor (Class executorClass) {
	for (Enumeration execs = Executor.getDefault().executors();
	     execs.hasMoreElements();) {
	    Executor exec = (Executor)execs.nextElement();
	    if (executorClass.isInstance(exec))
		return exec;
	}
	return null;
    }
    static void invokeMain (String className) throws Exception {
        invokeMain (className,null);
    }    
    
    static void invokeMain (String className,String[] params) throws Exception {
        Class  cls = Class.forName(className);        
        invokeMain (cls,params);
    }
    static void invokeMain (Class cls,String[] params) throws Exception {
        Method mth = cls.getDeclaredMethod("main",new Class[] {String[].class});
        try {
            mth.invoke(null,new Object[] {params});                        
        } catch (InvocationTargetException ite) {
            if (ite.getTargetException () instanceof java.lang.ThreadDeath) return;
            if (ite.getTargetException () instanceof java.lang.SecurityException) return;            
            //System.err.println(ite..printStackTrace());
            throw ite;
        }        
    }
    
    static void invokeMainInExecutor (final String className) throws Exception {
        Runnable runTestTask = new Runnable (){
            public void run () {
                try {
                    invokeMain (className);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        TopManager.getDefault().getExecutionEngine ().execute(className,runTestTask,null);
    }
}
    