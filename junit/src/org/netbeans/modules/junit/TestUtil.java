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

import java.util.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Task;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.jmi.javamodel.*;
import org.openide.src.ClassElement;
import java.lang.reflect.Modifier;


/**
 *
 * @author  rmatous
 * @author  Marian Petras
 * @version 1.1
 */
public class TestUtil {
    static private final String JAVA_SOURCES_SUFFIX               = "java";
    static private final String JAVA_SOURCES_FULL_SUFFIX          = "." + JAVA_SOURCES_SUFFIX;
    private static final String JAVA_MIME_TYPE = "text/x-java";         //NOI18N

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
    public static String getTestClassFullName(String sourceClassName, String packageName) {
        StringBuffer name = new StringBuffer();

        if (packageName != null) {
            name.append(packageName.replace('.','/'));
            if (name.length() > 0) {
                name.append('/');
            }
        }
        name.append(getTestClassName(sourceClassName));
        return name.toString();


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
     * Show error message box. 
     */
    public static void notifyUser(String msg) {
        notifyUser(msg, NotifyDescriptor.ERROR_MESSAGE);
    }
    
    /**
     * Show message box of the specified severity. 
     */
    public static void notifyUser(String msg, int errorLevel) {
        NotifyDescriptor descr = new NotifyDescriptor.Message(msg, errorLevel);
        DialogDisplayer.getDefault().notify(descr);
    }


    
    // other misc methods

    static public FileObject getFileObjectFromNode(Node node) {
        DataObject      dO;
        DataFolder      df;
        
        dO = (DataObject) node.getCookie(DataObject.class);
        if (null != dO)
            return dO.getPrimaryFile();

        df = (DataFolder) node.getCookie(DataFolder.class);
        if (null != df)
            return df.getPrimaryFile();
        
        ClassElement ce = (ClassElement) node.getCookie(ClassElement.class);
        if (null != ce) {
            // find the parent DataObject, which node belongs to
            while (null != (node = node.getParentNode())) {
                if (null != (dO = (DataObject) node.getCookie(DataObject.class)))
                    return dO.getPrimaryFile();
            }
        }
        return null;
    }


    

        
    static boolean isClassTest(JavaClass jc) {
        return isClassImplementingTestInterface(jc);
    }
    
    // is JavaClass a Test class ?
    static boolean isClassImplementingTestInterface(JavaClass cls) {        
        
        JavaModelPackage pkg = (JavaModelPackage)cls.refImmediatePackage();  
        JavaClass testInterface = (JavaClass)pkg.getJavaClass().resolve("junit.framework.Test");
        
        return cls.isSubTypeOf(testInterface);
    }    
    
        
    
    // is class an exception


    static boolean isClassException(JavaClass cls) {
        JavaModelPackage pkg = (JavaModelPackage)cls.refImmediatePackage();
        ClassDefinition throwable = (ClassDefinition)pkg.getType().resolve("java.lang.Throwable");
        return cls.isSubTypeOf(throwable);
    }


    /**
     * Gets all top-level classes from file.
     * @param fo the <code>FileObject</code> to examine
     * @return Collection<JavaClass>, not null
     */ 
    static Collection getAllClassesFromFile(FileObject fo) {
        if (fo == null) {
            return Collections.EMPTY_LIST;
        }

        Iterator it = JavaModel.getResource(fo).getChildren().iterator();
        LinkedList ret = new LinkedList();

        while (it.hasNext()) {
            Element e = (Element)it.next();
            if (e instanceof JavaClass) {
                ret.add((JavaClass)e);
            }
        }

        return ret;
    }

    /**
     * Returns an object describing the main class of the specified
     * file object containing java source.
     *
     * @param  res <code>Resource</code> examine
     * @return  <code>JavaClass</code> with the data object's
     *          main class; or <code>null</code> if the class was not
     *          found (e.g. because of a broken source file)
     */
    static JavaClass getMainJavaClass(Resource res) {
        // search for the main class  
        Iterator it = res.getChildren().iterator();
        String resName = fileToClassName(res.getName());
        if (resName != null) {
            while (it.hasNext()) {
                Element e = (Element)it.next();
                if (e instanceof JavaClass) {
                    System.err.println(((JavaClass)e).getName());
                    System.err.println(resName);

                    if (((JavaClass)e).getName().equals(resName)) 
                        return (JavaClass)e;
                }
            }
        }

        return null;
    }    
    
    /**
     * Converts filename to the fully qualified name of the main class
     * residing in the file. <br>
     * For example : "test/myapp/App.java" --> "test.myapp.App"
     * @param filename
     * @return corresponding package name. Null if the input is not
     * well formed.
     */
    static String fileToClassName(String fileName) {
        if (fileName.endsWith(".java")) {
            return (fileName.substring(0, fileName.length()-5)).replace('/','.');
        } else
            return null;
    }

    /**
     * Returns full names of all primary Java classes
     * withing the specified folder (non-recursive).
     *
     * @param  packageFolder  folder to search
     * @param  classPath  classpath to be used for the search
     * @return  list of full names of all primary Java classes
     *          within the specified package
     */
    public static List getJavaFileNames(FileObject packageFolder, ClassPath classPath) {
        FileObject[] children = packageFolder.getChildren();
        if (children.length == 0) {
            return Collections.EMPTY_LIST;
        }
        
        List result = new ArrayList(children.length);
        for (int i = 0; i < children.length; i++) {
            FileObject child = children[i];
            if (child.isFolder() || child.isVirtual()
                    || !child.getMIMEType().equals(JAVA_MIME_TYPE)) {
                continue;
            }

            DataObject dataObject;
            try {
                dataObject = DataObject.find(child);
            } catch (DataObjectNotFoundException ex) {
                continue;
            }

            Resource rc = JavaModel.getResource(dataObject.getPrimaryFile());
            result.add(getMainJavaClass(rc).getName());
        }
        return result.isEmpty() ? Collections.EMPTY_LIST : result;
    }

    public static List filterFeatures(JavaClass cls, Class type) {
        LinkedList ret = new LinkedList();
        Iterator it = cls.getFeatures().iterator();

        while (it.hasNext()) {
            Feature f = (Feature)it.next();
            if (type.isAssignableFrom(f.getClass())) ret.add(f);
        }
        return ret;
    }

    public static Feature getFeatureByName(JavaClass src, Class cls, String name) {
        if (!Feature.class.isAssignableFrom(cls)) throw new IllegalArgumentException("cls is not Feature");
        
        Iterator it = src.getFeatures().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (cls.isAssignableFrom(o.getClass())) {
                Feature f = (Feature)o;
                if (f.getName().equals(name)) return f;
            }
        }
        return null;
    }


    public static JavaClass getClassBySimpleName(JavaClass cls, String name) {
        return cls.getInnerClass(name, false);
    }

    static public Parameter cloneParam(Parameter p, JavaModelPackage pkg) {
        Parameter ret =
            pkg.getParameter().
            createParameter(p.getName(), 
                            p.getAnnotations(), 
                            p.isFinal(),
                            null,
                            p.getDimCount(),
                            p.isVarArg());
        ret.setType(p.getType());
        return ret;
    }

    public static List cloneParams(List params, JavaModelPackage pkg) {
        Iterator origParams = params.iterator();
        List newParams = new LinkedList();
        while (origParams.hasNext()) {
            Parameter p = (Parameter)origParams.next();
            newParams.add(TestUtil.cloneParam(p, pkg));
        }
        return newParams;
    }


    /**
     * Gets collection of types of the parameters passed in in the
     * argument. The returned collection has the same size as the
     * input collection.
     * @param params List<Parameter>
     * @return List<Type> 
     */
    static public List getParameterTypes(List params) {
        List ret = new ArrayList(params.size());
        Iterator it = params.iterator();
        while (it.hasNext()) {
            ret.add(((Parameter)it.next()).getType());
        }
        return ret;
    }



    /**
     * Gets list of all features within the given class of the given
     * class and modifiers.
     * @param c the JavaClass to search
     * @param cls the Class to search for
     * @param modifiers the modifiers to search for
     * @param recursive if true, the search descents to superclasses
     *                  and interfaces
     * @return List of the collected Features
     */
    public static List collectFeatures(JavaClass c, Class cls, 
                                   int modifiers, boolean recursive) {

        return collectFeatures(c, cls, modifiers, recursive, new LinkedList());
    }
        



    private static List collectFeatures(JavaClass c, Class cls, 
                                   int modifiers, boolean recursive, 
                                   List list ) 
    {

        System.err.println("Collecting " + c.getName());

        // this class
        
        int mo = (c.isInterface()) ? Modifier.ABSTRACT : 0;
        Iterator it = TestUtil.filterFeatures(c, cls).iterator();
        while (it.hasNext()) {
            Feature m = (Feature)it.next();
            System.err.print("Method : " + m.getName() + " ,modif:" + m.getModifiers());
            if (((m.getModifiers() | mo) & modifiers) == modifiers) {
                System.err.println("...matches");
                list.add(m);
            } else System.err.println("...skipped");
        }

        if (recursive) {
            // super
            JavaClass sup = c.getSuperClass();
            if (sup != null) collectFeatures(sup, cls, modifiers, recursive, list);

            // interfaces
            Iterator ifaces = c.getInterfaces().iterator();
            while (ifaces.hasNext()) collectFeatures((JavaClass)ifaces.next(), cls,
                                                     modifiers, recursive, list);
        }

        return list;
    }

    public static boolean hasMainMethod(JavaClass cls) {

        JavaModelPackage pkg = (JavaModelPackage)cls.refImmediatePackage();  
        return cls.getMethod("main", 
                             Collections.singletonList(pkg.getArray().resolveArray(TestUtil.getStringType(pkg))),
                             false) != null;

    }
        
    public static Type getStringType(JavaModelPackage pkg) {
        return pkg.getType().resolve("java.lang.String");
    }

    public static TypeReference getTypeReference(JavaModelPackage pkg, String name) {
        return pkg.getMultipartId().createMultipartId(name, null, Collections.EMPTY_LIST);
    }

}
