/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * TestUtil.java
 *
 * Created on October 17, 2000, 5:09 PM
 */
package org.netbeans.modules.junit;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SourceCookie;
import org.openide.src.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Task;

/**
 *
 * @author  rmatous
 * @author  Marian Petras
 * @version 1.1
 */
public class TestUtil {
    static private final String JAVA_SOURCES_SUFFIX               = "java";
    static private final String JAVA_SOURCES_FULL_SUFFIX          = "." + JAVA_SOURCES_SUFFIX;


    static private String getTestClassSuffix() {
        return JUnitSettings.TEST_CLASSNAME_SUFFIX;
    }
    
    static private String getTestClassPrefix() {
        return JUnitSettings.TEST_CLASSNAME_PREFIX;
    }
    
    static private String getTestSuiteSuffix() {
        return JUnitSettings.SUITE_CLASSNAME_SUFFIX;
    }
    
    static private String getTestSuitePrefix() {
        return JUnitSettings.SUITE_CLASSNAME_PREFIX;
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
        return name.toString();
    }
    
    private static String getTestClassName(ClassElement ce) {
        return getTestClassName(ce.getName().getName());
    }
    
    public static String getTestClassName(String sourceClassName) {
        return getTestClassPrefix() + sourceClassName + getTestClassSuffix();
    }
        
    
    //
    // suite class names
    //
    
    
    /**
     * Converts given package filename to test suite filename, e.g.
     * "org/netbeans/foo" -> "org/netbeans/foo/{suite-prefix}Foo{suite-suffix}"
     * @param packageFileName package filename in form of "org/netbeans/foo"
     */
    public static String convertPackage2SuiteName(String packageFileName) {
        if (packageFileName.length() == 0) {
            return getRootSuiteName();
        } else {
            int index = packageFileName.lastIndexOf('/');
            String pkg = index > -1 ? packageFileName.substring(index+1) : packageFileName;
            pkg = pkg.substring(0, 1).toUpperCase() + pkg.substring(1);
            return packageFileName + "/" + getTestSuitePrefix()+pkg+getTestSuiteSuffix();
        }
    }
    
    
    /**
     * Converts given class filename to test filename, e.g.
     * "org/netbeans/Foo" -> "org/netbeans/{test-prefix}Foo{test-suffix}"
     * @param classFileName class filename in form of "org/netbeans/Foo",
     *     i.e. without extension, no inner class
     */
    public static String convertClass2TestName(String classFileName) {
        int index = classFileName.lastIndexOf('/');
        String pkg = index > -1 ? classFileName.substring(0, index) : "";
        String clazz = index > -1 ? classFileName.substring(index+1) : classFileName;
        clazz = clazz.substring(0, 1).toUpperCase() + clazz.substring(1);
        if (pkg.length() > 0) {
            pkg += "/";
        }
        return pkg + getTestClassPrefix()+clazz+getTestClassSuffix();
    }

    /**
     * Show error message box. User from OpenTestAction and CreateTestAction
     * to notify user about problems during action execution.
     */
    public static void notifyUser(String msg) {
        NotifyDescriptor descr = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(descr);
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
    

    // make sure the class element is parsed, so it cannot return
    // wrong results
    private static void parseClassElement(ClassElement ce) {
        SourceElement se = ce.getSource();
        Task parser = se.prepare();
        parser.waitFinished();
    }
    
    
    private static boolean anyInterfaceImplementsName(FileObject ctx, Identifier[] interfaces, String name) {
        for (int i=0; i < interfaces.length; i++) {
            if (interfaces[i].getFullName().equals(name)) {
                return true;
            }
        }
        // hmm, it does not seem to 
        // let's try parent interfaces (if any)
        for (int i=0; i < interfaces.length; i++) {
            ClassElement interfaceClassElement = ClassElement.forName(interfaces[i].getFullName(), ctx);
            if (interfaceClassElement != null) {
                // make sure this class element is parsed
                parseClassElement(interfaceClassElement);
                Identifier[] parentInterfaces = interfaceClassElement.getInterfaces();
                if (parentInterfaces != null) {
                    boolean result = anyInterfaceImplementsName(ctx, parentInterfaces, name);
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
    
        
    static boolean isClassElementTest(FileObject ctx, ClassElement ce) {
        return isClassElementImplementingTestInterface(ctx, ce);
    }
    
    // is ClassElement a Test class ?
    static boolean isClassElementImplementingTestInterface(FileObject ctx, ClassElement ce) {        
        
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
                classElement = ClassElement.forName(superClassName, ctx);
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
            if (anyInterfaceImplementsName(ctx, interfaces,"junit.framework.Test")) {
                // we found it
                return true;
            }
            // otherwise continue in our search
        }
        // not implemented (or class has no superclass)
        return false;
    }    
    
    
    // is class an exception
    static boolean isClassElementException(FileObject ctx, ClassElement ce) {
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
                classElement = ClassElement.forName(superClassName, ctx);
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
    
    static ClassElement[] getAllClassElementsFromDataObject(DataObject dO) {
        if (dO == null) {
            return new ClassElement[0];
        }
        SourceCookie    sc;
        SourceElement   se;

        sc = (SourceCookie) dO.getCookie(SourceCookie.class);
        se = sc.getSource();
        return se.getAllClasses();
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
        if (dO == null) {
            return null;
        }
        SourceCookie    sc;
        SourceElement   se;
        sc = (SourceCookie) dO.getCookie(SourceCookie.class);
        if (sc == null) {
            return null;
        }
        se = sc.getSource();
        return se.getClass(Identifier.create(dO.getPrimaryFile().getName()));
    }    
    
}
