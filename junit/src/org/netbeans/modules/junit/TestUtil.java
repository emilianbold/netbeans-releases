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
    static private final String JAVA_SOURCES_SUFFIX               = "java";
    static private final String JAVA_SOURCES_FULL_SUFFIX          = "." + JAVA_SOURCES_SUFFIX;



    static private String getTestClassSuffix() {
        return JUnitSettings.getDefault().getTestClassNameSuffix();
    }
    
    static private String getTestClassPrefix() {
        return JUnitSettings.getDefault().getTestClassNamePrefix();
    }
    
    static private String getTestSuiteSuffix() {
        return JUnitSettings.getDefault().getSuiteClassNameSuffix();
    }
    
    static private String getTestSuitePrefix() {
        return JUnitSettings.getDefault().getSuiteClassNamePrefix();
    }
    
    static private String getRootSuiteName() {
        return JUnitSettings.getDefault().getRootSuiteClassName();
    }
    
    static private String getRootSuiteNameFullSuffix() {
        return  getRootSuiteName() + JAVA_SOURCES_FULL_SUFFIX;
    }    
    
    static private String getTestSuiteFullSuffix() {
        return getTestSuiteSuffix() + JAVA_SOURCES_FULL_SUFFIX;
    }

    //
    // test class names    
    //
    
    static public String getTestClassFullName(FileObject foSourceFile) {
        FileObject packageFileObject = foSourceFile.getParent();
        StringBuffer name = new StringBuffer();
        if (packageFileObject != null) {
            name.append(packageFileObject.getPackageName('/'));
            if ( name.length() != 0 ) {
                // only when the package is not root
                name.append('/');
            }
        }
        //System.err.println("TestUtil.getTestClassFullName() foSourceFile="+foSourceFile);
        //System.err.println("TestUtil.getTestClassFullName() packageName="+name);
        name.append(getTestClassName(foSourceFile));
        name.append(JAVA_SOURCES_FULL_SUFFIX);
        //System.err.println("TestUtil.getTestClassFullName() result = "+name);
        return name.toString();
    }
    
    
    static public String getTestClassName(FileObject foSourceFile) {
        return getTestClassName(foSourceFile.getName());
    }
    
    static public String getTestClassName(String sourceClassName) {
        return getTestClassPrefix() + sourceClassName + getTestClassSuffix();
    }
        
    static public boolean isTestClassFile(String packageName) {
        // definitely not that easy !!!! -- need to work on it
        //System.err.println("TestUtil.isTestClassFile packageName = "+packageName);
        return packageName.endsWith(getTestClassSuffix());
    }    
    
    //
    // suite class names
    //
    
    static public String getTestSuiteFullName(FileObject foPackage) {
        StringBuffer name = new StringBuffer();
        
        name.append(foPackage.getPackageName('/'));
        if (name.length() != 0)
            name.append("/");
        
        if (foPackage.getName().length() == 0)
            name.append(getRootSuiteNameFullSuffix());
        else {
            name.append(getTestSuitePrefix());
            name.append(foPackage.getName().substring(0, 1).toUpperCase());
            name.append(foPackage.getName().substring(1));
            name.append(getTestSuiteFullSuffix());
        } 
        return name.toString();
    }
    
    static public String getTestSuiteName(FileObject foPackage) {
        StringBuffer name = new StringBuffer();
        
        if (foPackage.getName().length() == 0)
            name.append(getRootSuiteName());
        else {
            name.append(getTestSuitePrefix());
            name.append(foPackage.getName().substring(0, 1).toUpperCase());
            name.append(foPackage.getName().substring(1));
            name.append(getTestSuiteSuffix());
        }        
        return  name.toString();
    }


    
    static public boolean isTestSuiteFile(String packageName) {
        // again need to work on that
        //System.err.println("TestUtil.isTestSuiteFile packageName = "+packageName);
        return packageName.endsWith(getTestSuiteSuffix());
    }    
    
    // other misc methods
    
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
        ExecutionEngine.getDefault().execute(className,runTestTask,null);
    }
    
    static boolean isSupportedFileSystem(FileSystem fileSystem) {
        FileSystemCapability capability = fileSystem.getCapability();
    /*    
        System.err.println("FS: "+fileSystem.getDisplayName());
        System.err.println("isValid():"+fileSystem.isValid());
        System.err.println("! - isDefault():"+fileSystem.isDefault());
        System.err.println("! - isReadOnly():"+fileSystem.isReadOnly());
        System.err.println("! - capability.capableOf(capability.DOC):"+capability.capableOf(capability.DOC));
      */  
        return (fileSystem.isValid() && 
                !fileSystem.isDefault() &&
                !fileSystem.isReadOnly() &&
                !capability.capableOf(capability.DOC));
    }
}
    