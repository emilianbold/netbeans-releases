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
/*
 * TestUtil.java
 *
 * Created on October 17, 2000, 5:09 PM
 */
package org.netbeans.modules.junit;

import java.net.URL;
import java.util.*;
import java.util.Collections;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.junit.wizards.Utils;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.jmi.javamodel.*;
import org.openide.src.ClassElement;
import java.lang.reflect.Modifier;
import org.openide.util.Utilities;


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
     * &quot;<tt>org/netbeans/foo</tt>&quot; -&gt;
     * &quot;<tt>org/netbeans/foo/{suite-prefix}Foo{suite-suffix}</tt>&quot;
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
     * &quot;<tt>org/netbeans/Foo</tt>&quot;
     * -&gt; &quot;<tt>org/netbeans/{test-prefix}Foo{test-suffix}</tt>&quot;
     *
     * @param  classFileName  class filename in form of
     *                        &quot;<tt>org/netbeans/Foo</tt>&quot;,
     *                        i.e. without extension, no inner class
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
    static public JavaClass getMainJavaClass(Resource res) {
        // search for the main class  
        Iterator it = res.getChildren().iterator();
        String resName = fileToClassName(res.getName());
        if (resName != null) {
            while (it.hasNext()) {
                Element e = (Element)it.next();
                if (e instanceof JavaClass) {
                    if (((JavaClass)e).getName().equals(resName)) 
                        return (JavaClass)e;
                }
            }
        }

        return null;
    }    
    
    /**
     * Converts filename to the fully qualified name of the main class
     * residing in the file.<br />
     * For example: <tt>test/myapp/App.java</tt> --&gt; <tt>test.myapp.App</tt>
     *
     * @param  filename
     * @return  corresponding package name. Null if the input is not
     *          well formed.
     */
    static String fileToClassName(String fileName) {
        if (fileName.endsWith(".java")) {                               //NOI18N
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

    static public String createNewName(int i, Set usedNames) {
        String ret;
        do {
            ret = "p" + i++;
        } while (usedNames.contains(ret));
        return ret;
    }

    static public Parameter cloneParam(Parameter p, JavaModelPackage pkg, int order, Set usedNames) {
        String name = p.getName();
        if (name == null || name.length()==0 || usedNames.contains(name)) {
            name = createNewName(order, usedNames);
        } 
        usedNames.add(name);

        
        Parameter ret =
            pkg.getParameter().
            createParameter(name,
                            p.getAnnotations(), 
                            p.isFinal(),
                            null,
                            0,//p.getDimCount(),
                            p.isVarArg());
        ret.setType(p.getType());
        return ret;
    }

    public static List cloneParams(List params, JavaModelPackage pkg) {
        Iterator origParams = params.iterator();
        List newParams = new LinkedList();
        int o = 0; 
        HashSet usedNames = new HashSet(params.size()*2);
        while (origParams.hasNext()) {
            Parameter p = (Parameter)origParams.next();
            newParams.add(TestUtil.cloneParam(p, pkg, o++, usedNames));
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

        return collectFeatures(c, cls, modifiers, recursive, new LinkedList(), new HashSet());
    }
        



    private static List collectFeatures(JavaClass c, Class cls, 
                                   int modifiers, boolean recursive, 
                                   List list, Set visited ) 
    {

	if (!visited.add(c)) return list;
        // this class
        
        int mo = (c.isInterface()) ? Modifier.ABSTRACT : 0;
        Iterator it = TestUtil.filterFeatures(c, cls).iterator();
        while (it.hasNext()) {
            Feature m = (Feature)it.next();
            if (((m.getModifiers() | mo) & modifiers) == modifiers) {
                list.add(m);
            }
        }

        if (recursive) {
            // super
            JavaClass sup = c.getSuperClass();
            if (sup != null) collectFeatures(sup, cls, modifiers, recursive, list, visited);

            // interfaces
            Iterator ifaces = c.getInterfaces().iterator();
            while (ifaces.hasNext()) collectFeatures((JavaClass)ifaces.next(), cls,
                                                     modifiers, recursive, list, visited);
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

    /**
     * Finds <code>SourceGroup</code>s where a test for the given class
     * can be created (so that it can be found by the projects infrastructure
     * when a test for the class is to be opened or run).
     *
     * @param  fileObject  <code>FileObject</code> to find target
     *                     <code>SourceGroup</code>(s) for
     * @return  an array of objects - each of them can be either
     *          a <code>SourceGroup</code> for a possible target folder
     *          or simply a <code>FileObject</code> representing a possible
     *          target folder (if <code>SourceGroup</code>) for the folder
     *          was not found);
     *          the returned array may be empty but not <code>null</code>
     * @author  Marian Petras
     */
    public static Object[] getTestTargets(FileObject fileObject) {
        
        /* .) get project owning the given FileObject: */
        final Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return new Object[0];
        }
        
        SourceGroup sourceGroupOwner = findSourceGroupOwner(fileObject);
        if (sourceGroupOwner == null) {
            return new Object[0];
        }
        
        /* .) get URLs of target SourceGroup's roots: */
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(
                                       sourceGroupOwner.getRootFolder());
        if (rootURLs.length == 0) {
            return new Object[0];
        }
        
        /* .) convert the URLs to FileObjects: */
        boolean someSkipped = false;
        FileObject[] sourceRoots = new FileObject[rootURLs.length];
        for (int i = 0; i < rootURLs.length; i++) {
            if ((sourceRoots[i] = URLMapper.findFileObject(rootURLs[i]))
                    == null) {
                ErrorManager.getDefault().notify(
                        ErrorManager.INFORMATIONAL,
                        new IllegalStateException(
                           "No FileObject found for the following URL: "//NOI18N
                           + rootURLs[i]));
                someSkipped = true;
                continue;
            }
            if (FileOwnerQuery.getOwner(sourceRoots[i]) != project) {
                ErrorManager.getDefault().notify(
                        ErrorManager.INFORMATIONAL,
                        new IllegalStateException(
                    "Source root found by FileOwnerQuery points "       //NOI18N
                    + "to a different project for the following URL: "  //NOI18N
                    + rootURLs[i]));
                sourceRoots[i] = null;
                someSkipped = true;
                continue;
            }
        }
        
        if (someSkipped) {
            Object roots[] = skipNulls(sourceRoots);
            if (roots.length == 0) {
                return new Object[0];
            }
            sourceRoots = (FileObject[]) roots;
        }
        
        /* .) find SourceGroups corresponding to the FileObjects: */
        final Object[] targets = new Object[sourceRoots.length];
        Map map = getFileObject2SourceGroupMap(project);
        for (int i = 0; i < sourceRoots.length; i++) {
            Object srcGroup = map.get(sourceRoots[i]);
            targets[i] = srcGroup != null ? srcGroup : sourceRoots[i];
        }
        return targets;
    }
    
    /**
     * Finds a <code>SourceGroup</code> the given file belongs to.
     * Only Java <code>SourceGroup</code>s are taken into account.
     *
     * @param  file  <code>FileObject</code> whose owning
     *               <code>SourceGroup</code> to be found
     * @return  Java <code>SourceGroup</code> containing the given
     *          file; or <code>null</code> if no such
     *          <code>SourceGroup</code> was found
     * @author  Marian Petras
     */
    public static SourceGroup findSourceGroupOwner(FileObject file) {
        final Project project = FileOwnerQuery.getOwner(file);
        return findSourceGroupOwner(project, file);
    }
    
    /**
     * Finds a <code>SourceGroup</code> the given file belongs to.
     * Only Java <code>SourceGroup</code>s are taken into account. 
     *
     * @param project the <code>Project</code> the file belongs to
     * @param  file  <code>FileObject</code> whose owning
     *               <code>SourceGroup</code> to be found
     * @return  Java <code>SourceGroup</code> containing the given
     *          file; or <code>null</code> if no such
     *          <code>SourceGroup</code> was found
     */

    public static SourceGroup findSourceGroupOwner(Project project, FileObject file) {        
        final SourceGroup[] sourceGroups
                = new Utils(project).getJavaSourceGroups();
        for (int i = 0; i < sourceGroups.length; i++) {
            SourceGroup srcGroup = sourceGroups[i];
            FileObject root = srcGroup.getRootFolder();
            if (((file==root)||(FileUtil.isParentOf(root,file))) && 
                 srcGroup.contains(file)) {
                return srcGroup;
            }
        }
        return null;
    }
    
    /**
     * Finds all <code>SourceGroup</code>s of the given project
     * containing a class of the given name.
     *
     * @param  project  project to be searched for matching classes
     * @param  className  class name pattern
     * @return  unmodifiable collection of <code>SourceGroup</code>s
     *          which contain files corresponding to the given name
     *          (may be empty but not <code>null</code>)
     * @author  Marian Petras
     */
    public static Collection findSourceGroupOwners(
            final Project project,
            final String className) {
        final SourceGroup[] sourceGroups
                = new Utils(project).getJavaSourceGroups();
        if (sourceGroups.length == 0) {
            return Collections.EMPTY_LIST;
        }
        
        final String relativePath = className.replace('.', '/')
                                    + ".java";                          //NOI18N
        
        ArrayList result = new ArrayList(4);
        for (int i = 0; i < sourceGroups.length; i++) {
            SourceGroup srcGroup = sourceGroups[i];
            FileObject root = srcGroup.getRootFolder();
            FileObject file = root.getFileObject(relativePath);
            if (file != null && FileUtil.isParentOf(root, file)
                             && srcGroup.contains(file)) {
                result.add(srcGroup);
            }
        }
        if (result.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        result.trimToSize();
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Creates a copy of the given array, except that <code>null</code> objects
     * are omitted.
     * The length of the returned array is (<var>l</var> - <var>n</var>), where
     * <var>l</var> is length of the passed array and <var>n</var> is number
     * of <code>null</code> elements of the array. Order of
     * non-<code>null</code> elements is kept in the returned array.
     * The returned array is always a new array, even if the passed
     * array does not contain any <code>null</code> elements.
     *
     * @param  objs  array to copy
     * @return  array containing the same objects as the passed array, in the
     *          same order, just with <code>null</code> elements missing
     * @author  Marian Petras
     */
    public static Object[] skipNulls(final Object[] objs) {
        List resultList = new ArrayList(objs.length);
        
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] != null) {
                resultList.add(objs[i]);
            }
        }
        
        return resultList.isEmpty() ? new Object[0] : resultList.toArray();
    }
    
    /**
     * Creates a map from folders to <code>SourceGroup</code>s of a given
     * project.
     * The map allows to ascertian for a given folder
     * which <code>SourceGroup</code> it is a root folder of.
     *
     * @param  project  project whose <code>SourceGroup</code>s should be in the
     *                  returned map
     * @return  map from containing all <code>SourceGroup</code>s of a given
     *          project, having their root folders as keys
     * @author  Marian Petras
     */
    public static Map getFileObject2SourceGroupMap(Project project) {
        final SourceGroup[] sourceGroups
                = new Utils(project).getJavaSourceGroups();
        
        if (sourceGroups.length == 0) {
            return Collections.EMPTY_MAP;
        } else if (sourceGroups.length == 1) {
            return Collections.singletonMap(sourceGroups[0].getRootFolder(),
                                            sourceGroups[0]);
        } else {
            Map map = new HashMap(Math.round(sourceGroups.length * 1.4f + .5f),
                                  .75f);
            for (int i = 0; i < sourceGroups.length; i++) {
                map.put(sourceGroups[i].getRootFolder(),
                        sourceGroups[i]);
            }
            return map;
        }
    }

    // Nice copy of useful methods (Taken from JavaModule)
    public static boolean isValidPackageName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tukac = new StringTokenizer(str, ".");
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if ("".equals(token))
                return false;
            if (!Utilities.isJavaIdentifier(token))
                return false;
        }
        return true;
    }

    
}
