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

import java.io.File;
import java.io.IOException;
import org.openide.*;
import org.openide.filesystems.*;
import org.openide.execution.*;
import org.openide.src.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.Task;
import java.lang.reflect.*;
import org.openide.cookies.*;

import java.util.*;
import org.openide.util.NbBundle;

/**
 *
 * @author  rmatous
 * @author  Marian Petras
 * @version 1.1
 */
public class TestUtil {
    static private final String JAVA_SOURCES_SUFFIX               = "java";
    static private final String JAVA_SOURCES_FULL_SUFFIX          = "." + JAVA_SOURCES_SUFFIX;


    /**
     * Finds localized variants of a file.
     *
     * @param  lib  file to found localized variants for
     * @return  non-empty list of found <code>File</code>s;
     *          or <code>null</code> if no localized variant was found
     */
    public static List findLocalizedLibs(File lib) {
        String libDir = lib.getParent();
        String libNameExt = lib.getName();
        String libName;
        String extName;
        int dotIndex = libNameExt.indexOf('.');
        if (dotIndex != -1) {
            libName = libNameExt.substring(0, dotIndex);
            extName = libNameExt.substring(dotIndex + 1);
        } else {
            libName = libNameExt;
            extName = null;
        }

        List locLibs = new ArrayList(4);
        for (Iterator i = NbBundle.getLocalizingSuffixes(); i.hasNext(); ) {
            String suffix = (String) i.next();
            StringBuffer buf = new StringBuffer(
                    libDir != null ? libDir.length() : 0
                    + libNameExt.length()
                    + 25);
            if (libDir != null) {
                buf.append(libDir)
                   .append(File.separatorChar)
                   .append("locale")                                    //NOI18N
                   .append(File.separatorChar);
            }
            buf.append(libName).append(suffix);
            if (extName != null) {
                buf.append('.').append(extName);
            }
            String locLibName = buf.toString();
            File locLib = new File(locLibName);
            if (locLib.exists() && locLib.isFile()) {
                locLibs.add(locLib);
            }
        }

        return locLibs.isEmpty() ? null : locLibs;
    }

    /**
     * Creates a canonical file from a given file, using
     * {@link File#getCanonicalFile(File)}. In case of a failure, tries
     * to absolutize it.
     * If the file could not be canonicalized, logs a message
     * instead of throwing <code>java.io.IOException</code>.
     *
     * @param  file  file to find a canonical path for
     * @return  canonical form of the file,
     *          or an absolute form of the file if canonicalization failed
     */
    public static File canonicalize(File file) {
        boolean canonicalized = false;
        boolean absolutized = false;
        try {
            return file.getCanonicalFile();
        } catch (IOException ex) {
            ErrorManager.getDefault().log(
                    ErrorManager.WARNING,
                    "Could not canonicalize file "                      //NOI18N
                            + file.getPath() + '.');
            return file.getAbsoluteFile();
        }
    }

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
    
    static public String getTestClassFullName(ClassElement ce) {
        StringBuffer name = new StringBuffer();
        String packageName = ce.getName().getQualifier();
        if (packageName != null) {
            name.append(packageName.replace('.','/'));
            if (name.length() > 0) {
                name.append('/');
            }
        }
        name.append(getTestClassName(ce));
        name.append(JAVA_SOURCES_FULL_SUFFIX);
        return name.toString();
    }
    
    static public String getTestClassName(ClassElement ce) {
        return getTestClassName(ce.getName().getName());
    }
    
    static public String getTestClassFullName(FileObject foSourceFile) {
        FileObject packageFileObject = foSourceFile.getParent();
        StringBuffer name = new StringBuffer();
        if (packageFileObject != null) {
            name.append(packageFileObject.getPackageName('/'));
            if ( name.length() > 0 ) {
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
        boolean result = true;
        if (getTestClassPrefix() != null) {
            result &= packageName.startsWith(getTestClassPrefix());
        }
        if (getTestClassSuffix() != null) {
            result &= packageName.endsWith(getTestClassSuffix());
        }
        return result;
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
    
    
    // fullname has to be in /my/package/stuff/class.java format
    static public String getPackageNameFromFullName(String fullName) {
        int lastIndex = fullName.lastIndexOf('/');
        if (lastIndex != -1) {
            return fullName.substring(0,lastIndex);
        } else {
            return fullName;
        }
    }
    
    // fullname has to be in /my/package/stuff/class.java format
    static public String getShortClassNameFromFullName(String fullName) {
        int lastIndex = fullName.lastIndexOf('/');
        if (lastIndex != -1) {
            return fullName.substring(lastIndex);
        } else {
            return fullName;
        }
    }
    
    static public String stripExtensionFromFullName(String fullName) {
        int lastIndex = fullName.lastIndexOf('.');
        if (lastIndex != -1) {
            return fullName.substring(0,lastIndex);
        } else {
            return fullName;
        }
    }


    
    static public boolean isTestSuiteFile(String packageName) {
        boolean result = true;
        if (getTestSuitePrefix() != null) {
            result &= packageName.startsWith(getTestSuitePrefix());
        }
        if (getTestSuiteSuffix() != null) {
            result &= packageName.endsWith(getTestSuiteSuffix());
        }
        return result;
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
    

    // make sure the class element is parsed, so it cannot return
    // wrong results
    static void parseClassElement(ClassElement ce) {
        //System.err.println("$$$$$$$ Parsing class element :"+ce.getVMName());
        SourceElement se = ce.getSource();
        //int sourceStatus = se.getStatus();
        //if ((sourceStatus != SourceElement.STATUS_OK)&(sourceStatus != SourceElement.STATUS_ERROR)) {
            //System.err.println("$$$$$ Parsing ....");
            Task parser = se.prepare();
            parser.waitFinished();
        //} 
        if (se.getStatus() == SourceElement.STATUS_OK) {
            //System.err.println("$$$$$$ SourceElement is OK ....");
        } else {
            //System.err.println("$!!!!! SourceElement is in status:"+se.getStatus());
        }
    }
    
    
    static boolean anyInterfaceImplementsName(Identifier[] interfaces, String name) {
        for (int i=0; i < interfaces.length; i++) {
            if (interfaces[i].getFullName().equals(name)) {
                return true;
            }
        }
        // hmm, it does not seem to 
        // let's try parent interfaces (if any)
        for (int i=0; i < interfaces.length; i++) {
            ClassElement interfaceClassElement = ClassElement.forName(interfaces[i].getFullName());
            if (interfaceClassElement != null) {
                // make sure this class element is parsed
                parseClassElement(interfaceClassElement);
                Identifier[] parentInterfaces = interfaceClassElement.getInterfaces();
                if (parentInterfaces != null) {
                    boolean result = anyInterfaceImplementsName(parentInterfaces, name);
                    if (result == true) {
                       // great - we found it
                       return true;
                    } 
                    // otherwise continue
                }
            }
        }
        // hmm, this branch does not seem to implement the interface name
        return false;
    }
    
        
    static boolean isClassElementTest(ClassElement ce) {
        return isClassElementImplementingTestInterface(ce);
    }
    
    // is ClassElement a Test class ?
    static boolean isClassElementImplementingTestInterface(ClassElement ce) {        
        
        boolean result = false;
        ClassElement classElement = ce;
        while (classElement != null) {
            //System.err.println("############### Tested ClassElement:"+classElement.getVMName());
            // make sure it is correctly parsed
            parseClassElement(classElement);
            Identifier superClass = classElement.getSuperclass();
            //System.err.println("Tested superClass :"+superClass);
            Identifier[] interfaces = classElement.getInterfaces();            
            // check the supperclass (if available)            
            if (superClass != null) {
                String superClassName = superClass.getFullName();                
                //System.err.println("Tested ClassElement superclassFullName:"+superClassName);
                // shortcut !!!
                classElement = ClassElement.forName(superClassName);                
                if (classElement != null) {                    
                    //System.err.println("!! Tested SuperClassElement.getVMName()"+classElement.getVMName());
                    parseClassElement(classElement);
                    if ("junit.framework.TestCase".equals(classElement.getVMName())) {
                        return true;
                    }
                } else {
                    //System.err.println("!!! superClassElement is null !!!!!");
                }
            } else {
                // no super class - go on
                //System.err.println("No superclass");
                classElement = null;
            }
            
            // now check the interfaces            
            if (anyInterfaceImplementsName(interfaces,"junit.framework.Test")) {
                // we found it
                return true;
            }
            // otherwise continue in our search
        }
        // not implemented (or class has no superclass)
        return false;
    }    
    
    
    // is class an exception
    static boolean isClassElementException(ClassElement ce) {
        boolean result = false;
        ClassElement classElement = ce;
        while (classElement != null) {
            parseClassElement(classElement);
            Identifier superClass = classElement.getSuperclass();
            Identifier[] interfaces = classElement.getInterfaces();            
            // check the supperclass (if available)            
            if (superClass != null) {
                String superClassName = superClass.getFullName();                
                // shortcut !!!
                classElement = ClassElement.forName(superClassName);                
                if (classElement != null) {                    
                    //System.err.println("!! Tested SuperClassElement.getVMName()"+classElement.getVMName());
                    parseClassElement(classElement);
                    if ("java.lang.Throwable".equals(classElement.getVMName())) {
                        return true;
                    }
                } else {
                    //System.err.println("!!! superClassElement is null !!!!!");
                }
            } else {
                // no super class - go on
                //System.err.println("No superclass");
                classElement = null;
            }            
        }
        // not implemented (or class has no superclass)
        return false;
        
    }
    
    
    static ClassElement[] getAllClassElementsFromFileObject(FileObject fo) throws DataObjectNotFoundException {
        return getAllClassElementsFromDataObject(DataObject.find(fo));
    }
    
    
    static ClassElement[] getAllClassElementsFromDataObject(DataObject dO) {
        SourceCookie    sc;
        SourceElement   se;

        sc = (SourceCookie) dO.getCookie(SourceCookie.class);
        se = sc.getSource();
        return se.getAllClasses();
    }

    static ClassElement getClassElementFromFileObject(FileObject fo) throws DataObjectNotFoundException {
        return getClassElementFromDataObject(DataObject.find(fo));
    }    
    
    /**
     * Returns an object describing the main class of the specified
     * Java data object.
     *
     * @param  dO  data object to examine
     * @return  <code>ClassElement</code> describing the data object's
     *          main class; or <code>null</code> if the class element was not
     *          found (e.g. because of a broken data object's source file)
     */
    static ClassElement getClassElementFromDataObject(DataObject dO) {
        SourceCookie    sc;
        SourceElement   se;
        sc = (SourceCookie) dO.getCookie(SourceCookie.class);
        if (sc == null) {
            return null;
        }
        se = sc.getSource();
        return se.getClass(Identifier.create(dO.getPrimaryFile().getName()));
    }    
    
    static ClassElement getClassElementCookie(DataObject doTarget, String name) {
        return (ClassElement) doTarget.getNodeDelegate().getChildren().findChild(name).getCookie(ClassElement.class);
    }    
    
}
    
