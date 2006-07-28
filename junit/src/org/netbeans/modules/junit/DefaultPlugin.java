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

package org.netbeans.modules.junit;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.AnnotationType;
import org.netbeans.jmi.javamodel.ClassDefinition;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.Feature;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.junit.DefaultPlugin.CreationResults.SkippedClass;
import org.netbeans.modules.junit.TestCreator.TesteableResult;
import org.netbeans.modules.junit.plugin.JUnitPlugin;
import org.netbeans.modules.junit.plugin.JUnitPlugin.CreateTestParam;
import org.netbeans.modules.junit.plugin.JUnitPlugin.Location;
import org.netbeans.modules.junit.wizards.Utils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Default JUnit plugin.
 *
 * @author  Marian Petras
 */
public final class DefaultPlugin extends JUnitPlugin {
    
    /**
     *
     */
    protected Location getTestLocation(Location sourceLocation) {
        final FileObject foRoot;
        final Project project;
        final ClassPath srcCP, tstCP;
        final Utils utils;
        FileObject[] testRootsRaw, testRoots;
        
        final FileObject fo = sourceLocation.getFileObject();
        
        if (((project = FileOwnerQuery.getOwner(fo)) == null)
             || ((srcCP = ClassPath.getClassPath(fo, ClassPath.SOURCE)) == null)
             || ((foRoot = srcCP.findOwnerRoot(fo)) == null)
             || ((utils = new Utils(project)) == null)//side effect - assignment
             || ((testRootsRaw = utils.getTestFoldersRaw(foRoot)).length == 0)
             || ((testRoots = Utils.skipNulls(testRootsRaw)).length == 0)) {
            return null;
        }
        
        String baseResName = srcCP.getResourceName(fo, '/', false);
        String testResName = !fo.isFolder()
                             ? getTestResName(baseResName, fo.getExt())
                             : getSuiteResName(baseResName);
        assert testResName != null;
        
        List/*<FileObject>*/ testFiles = ClassPathSupport
                                         .createClassPath(testRoots)
                                         .findAllResources(testResName);
        if (testFiles.isEmpty()) {
            return null;            //PENDING - offer creation of new test class
        } else {
            return getOppositeLocation(sourceLocation,
                                       (List<FileObject>) testFiles,
                                       true);
        }
    }
    
    /**
     *
     */
    protected Location getTestedLocation(Location testLocation) {
        final FileObject foRoot;
        final Project project;
        final ClassPath srcCP, tstCP;
        final Utils utils;
        FileObject[] sourceRootsRaw, sourceRoots;
        
        final FileObject fo = testLocation.getFileObject();
        
        if (fo.isFolder()
             || ((project = FileOwnerQuery.getOwner(fo)) == null)
             || ((srcCP = ClassPath.getClassPath(fo, ClassPath.SOURCE)) == null)
             || ((foRoot = srcCP.findOwnerRoot(fo)) == null)
             || ((utils = new Utils(project)) == null)//side effect - assignment
             || ((sourceRootsRaw = utils.getSourceFoldersRaw(foRoot))
                 .length == 0)
             || ((sourceRoots = Utils.skipNulls(sourceRootsRaw)).length == 0)) {
            return null;
        }
        
        String baseResName = srcCP.getResourceName(fo, '/', false);
        String srcResName = getSrcResName(baseResName, fo.getExt());
        if (srcResName == null) {
            return null;     //if the selectedFO is not a test class (by name)
        }

        List/*<FileObject>*/ srcFiles = ClassPathSupport
                                           .createClassPath(sourceRoots)
                                           .findAllResources(srcResName);
        if (srcFiles.isEmpty()) {
            return null;
        } else {
            return getOppositeLocation(testLocation,
                                       (List<FileObject>) srcFiles,
                                       false);
        }
    }
    
    /**
     *
     */
    private static Location getOppositeLocation(
                                    final Location sourceLocation,
                                    final List<FileObject> candidateFiles,
                                    final boolean sourceToTest) {
        Feature element = sourceLocation.getJavaElement();
        if (element == null) {
            return new Location(candidateFiles.get(0), null);
        }

        assert (element instanceof Method)
               || (element instanceof JavaClass);
            
        JavaClass clazz;
        String oppoMethodName = null;
        String baseClassName, oppoClassName;
        String pkgName;

        if (element instanceof Method) {
            Method method = (Method) element;
            ClassDefinition classDef = method.getDeclaringClass();
            if (classDef instanceof JavaClass) {
                clazz = (JavaClass) classDef;
                String baseMethodName = method.getName();
                oppoMethodName = sourceToTest
                                 ? getTestMethodName(baseMethodName)
                                 : getSourceMethodName(baseMethodName);
            } else {
                clazz = null;
            }
        } else {
            clazz = (JavaClass) element;
        }
        
        if (clazz == null) {
            return new Location(candidateFiles.get(0), null);
        }
        
        baseClassName = clazz.getName();            //PENDING - inner classes!!!
        oppoClassName = sourceToTest
                        ? getTestClassName(baseClassName)
                        : getSourceClassName(baseClassName);
        
        if (oppoClassName == null) {
            return new Location((FileObject) candidateFiles.get(0), null);
        }
        
        FileObject foWithClass = null;
        FileObject foWithMethod = null;
        JavaClass theJavaClass = null;
        Method theMethod = null;

        for (FileObject fileObj : candidateFiles) {
            Resource resource = JavaModel.getResource(fileObj);
            assert resource != null;

            JavaClass javaClass = findJavaClass(resource, oppoClassName);
            if (javaClass != null) {
                if (foWithClass == null) {
                    foWithClass = fileObj;
                    theJavaClass = javaClass;
                }
                if (oppoMethodName != null) {
                    Method method = sourceToTest
                                    ? findTestMethod(javaClass,
                                                     oppoMethodName)
                                    : findSourceMethod(javaClass,
                                                       oppoMethodName);
                    if (method != null) {
                        theMethod = method;
                        foWithMethod = fileObj;
                        break;
                    }
                }
            }
        }
        
        if (foWithMethod != null) {
            return new Location(foWithMethod, theMethod);
        } else if (foWithClass != null) {
            return new Location(foWithClass, theJavaClass);
        } else {
            return new Location(candidateFiles.get(0), null);
        }
    }
    
    /**
     */
    private static String getTestResName(String baseResName, String ext) {
        StringBuilder buf
                = new StringBuilder(baseResName.length() + ext.length() + 10);
        buf.append(baseResName).append("Test");                         //NOI18N
        if (ext.length() != 0) {
            buf.append('.').append(ext);
        }
        return buf.toString();
    }
    
    /**
     */
    private static String getSuiteResName(String baseResName) {
        if (baseResName.length() == 0) {
            return JUnitSettings.getDefault().getRootSuiteClassName();
        }
        
        final String suiteSuffix = "Suite";                             //NOI18N

        String lastNamePart
                = baseResName.substring(baseResName.lastIndexOf('/') + 1);

        StringBuilder buf = new StringBuilder(baseResName.length()
                                              + lastNamePart.length()
                                              + suiteSuffix.length()
                                              + 6);
        buf.append(baseResName).append('/');
        buf.append(Character.toUpperCase(lastNamePart.charAt(0)))
           .append(lastNamePart.substring(1));
        buf.append(suiteSuffix);
        buf.append(".java");                                            //NOI18N

        return buf.toString();
    }
    
    /**
     */
    private static String getSrcResName(String testResName, String ext) {
        if (!testResName.endsWith("Test")) {                            //NOI18N
            return null;
        }
        
        StringBuilder buf
                = new StringBuilder(testResName.length() + ext.length());
        buf.append(testResName.substring(0, testResName.length() - 4));
        if (ext.length() != 0) {
            buf.append('.').append(ext);
        }
        return buf.toString();
    }
    
    /**
     */
    private static String getTestClassName(String baseClassName) {
        return baseClassName + "Test";                                  //NOI18N
    }
    
    /**
     */
    private static String getSourceClassName(String testClassName) {
        final String suffix = "Test";                                   //NOI18N
        final int suffixLen = suffix.length();
        
        return ((testClassName.length() > suffixLen)
                    && testClassName.endsWith(suffix))
               ? testClassName.substring(0, testClassName.length() - suffixLen)
               : null;
    }
    
    /**
     */
    private static String getSuiteName(String packageName) {
        if (packageName.length() == 0) {
            return JUnitSettings.getDefault().getRootSuiteClassName();
        }

        final String suiteSuffix = "Suite";                             //NOI18N

        String lastNamePart
                = packageName.substring(packageName.lastIndexOf('.') + 1);
        StringBuffer buf = new StringBuffer(packageName.length()
                                            + lastNamePart.length()
                                            + suiteSuffix.length()
                                            + 1);
        buf.append(packageName).append('.');
        buf.append(Character.toUpperCase(lastNamePart.charAt(0)))
           .append(lastNamePart.substring(1));
        buf.append(suiteSuffix);
        
        return buf.toString();
    }
    
    /**
     */
    private static String getTestMethodName(String baseMethodName) {
        final String prefix = "test";                                   //NOI18N
        final int prefixLen = prefix.length();
        
        StringBuffer buf = new StringBuffer(prefixLen
                                            + baseMethodName.length());
        buf.append(prefix).append(baseMethodName);
        buf.setCharAt(prefixLen,
                      Character.toUpperCase(baseMethodName.charAt(0)));
        return buf.toString();
    }
    
    /**
     */
    private static String getSourceMethodName(String testMethodName) {
        final String prefix = "test";                                   //NOI18N
        final int prefixLen = prefix.length();
        
        return ((testMethodName.length() > prefixLen)
                    && testMethodName.startsWith(prefix))
               ? new StringBuffer(testMethodName.length() - prefixLen)
                 .append(Character.toLowerCase(testMethodName.charAt(prefixLen)))
                 .append(testMethodName.substring(prefixLen + 1))
                 .toString()
               : null;
    }
    
    /**
     * Finds class of the given name in the given resource.
     *
     * @return  the found class, or <code>null</code> if the class was not found
     */
    private static JavaClass findJavaClass(Resource resource,
                                           String className) {
        for (Iterator/*<Element>*/ i = resource.getChildren().iterator();
                i.hasNext();
                ) {
            Element e = (Element) i.next();
            if ((e instanceof JavaClass)
                    && ((JavaClass) e).getName().equals(className)) {
                return (JavaClass) e;
            }
        }
        return null;
    }
    
    /**
     * Finds a no-arg method with void return type of the given name.
     *
     * @param  classDef  class to find the method in
     * @param  methodName  requested name of the method
     * @return  found method, or <code>null</code> if not found
     */
    private static Method findTestMethod(ClassDefinition classDef,
                                         String methodName) {
        
        Method method = classDef.getMethod(methodName,
                                           Collections.EMPTY_LIST,
                                           false);
        return (method != null)
               && Modifier.isPublic(method.getModifiers())
               && method.getTypeName().getName().equals("void")         //NOI18N
                      ? method
                      : null;
    }
    
    /**
     * Finds a method with of the given name.
     *
     * @param  classDef  class to find the method in
     * @param  methodName  requested name of the method
     * @return  found method, or <code>null</code> if not found
     */
    private static Method findSourceMethod(ClassDefinition classDef,
                                           String methodName) {
        List/*<Element>*/ classChildren = classDef.getChildren();
        
        if ((classChildren == null) || (classChildren.isEmpty())) {
            return null;
        }
        
        for (Iterator/*<Element>*/ i = classChildren.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof Method) {
                Method method = (Method) o;
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
        }
        return null;
    }
    
    /**
     * Creates test classes for given source classes.
     * 
     * @param filesToTest  source files for which test classes should be
     *                      created
     * @param targetRoot   root folder of the target source root
     * @param params  parameters of creating test class
     * @return created test files
     */
    protected FileObject[] createTests(
                                final FileObject[] filesToTest,
                                final FileObject targetRoot,
                                final Map<CreateTestParam, Object> params) {
        final boolean emptyTest =
                (filesToTest == null) || (filesToTest.length == 0);
        //XXX: Not documented, that filesToTest may be <null>
        final boolean singleClass = !emptyTest
                                    && (filesToTest.length == 1)
                                    && filesToTest[0].isData();
        final String testClassName =
                emptyTest || singleClass
                ? (String) params.get(CreateTestParam.CLASS_NAME)
                : null;
        final ClassPath testClassPath = ClassPathSupport.createClassPath(
                                           new FileObject[] {targetRoot});

        final DataObject doTestTempl
                = loadTestTemplate("PROP_testClassTemplate");           //NOI18N
        if (doTestTempl == null) {
            return null;
        }

        final DataObject doSuiteTempl;
        if (!emptyTest && !singleClass) {
            doSuiteTempl = loadTestTemplate("PROP_testSuiteTemplate");  //NOI18N
            if (doSuiteTempl == null) {
                return null;
            }
        } else {
            doSuiteTempl = null;
        }

        ProgressIndicator progress = new ProgressIndicator();
        progress.show();

        String msg = NbBundle.getMessage(
                    CreateTestAction.class,
                    "MSG_StatusBar_CreateTest_Begin");                  //NOI18N
        progress.displayStatusText(msg);

        // results will be accumulated here
        CreationResults results;
        final TestCreator testCreator = new TestCreator(params);
        try {
            if (emptyTest) {
                assert testClassName != null;
                
                results = new CreationResults(1);
                try {
                    DataFolder targetFolderDataObj
                            = DataFolder.findFolder(targetRoot);    //XXX: not documented that in case that if filesToTest is <null>, the target root param works as a target folder
                    DataObject testDataObj = doTestTempl.createFromTemplate(
                                            targetFolderDataObj, testClassName);

                    /* fill in setup etc. according to dialog settings */
                    FileObject foSource = testDataObj.getPrimaryFile();
                    Resource srcRc = JavaModel.getResource(foSource);
                    JavaClass cls = TestUtil.getMainJavaClass(srcRc);
                    new TestCreator(params).createEmptyTest(srcRc, cls);
                    
                    results.addCreated(testDataObj);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            } else if (singleClass) {
                assert testClassName != null;
                try {
                    results = createSingleTest(
                            testCreator,
                            testClassPath,
                            filesToTest[0],
                            testClassName,
                            doTestTempl,
                            null,              //parent suite
                            progress,
                            false);            //do not skip any classes
                } catch (CreationError ex) {
                    ErrorManager.getDefault().notify(ex);
                    results = new CreationResults(1);
                }
            } else {
                results = new CreationResults();

                // go through all nodes
                for (FileObject fileToTest : filesToTest) {
                    try {
                        results.combine(createTests(testCreator,
                                                    fileToTest,
                                                    testClassPath,
                                                    doTestTempl,
                                                    doSuiteTempl,
                                                    null,
                                                    progress));
                    } catch (CreationError e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }
        } finally {
            progress.hide();
        }

        final Set<SkippedClass> skipped = results.getSkipped();
        final Set<DataObject> created = results.getCreated();
        if (!skipped.isEmpty()) {
            // something was skipped
            String message;
            if (skipped.size() == 1) {
                // one class? report it
                SkippedClass skippedClass = skipped.iterator().next();

                message = NbBundle.getMessage(
                        DefaultPlugin.class,
                        "MSG_skipped_class",                            //NOI18N
                        skippedClass.cls.getName(),
                        strReason(skippedClass.reason, "COMMA", "AND"));//NOI18N
            } else {
                // more classes, report a general error
                // combine the results
                TesteableResult reason = TesteableResult.OK;
                for (SkippedClass sc : skipped) {
                    reason = TesteableResult.combine(reason, sc.reason);
                }

                message = NbBundle.getMessage(
                        DefaultPlugin.class,
                        "MSG_skipped_classes",                          //NOI18N
                        strReason(reason, "COMMA", "OR"));              //NOI18N
            }
            TestUtil.notifyUser(message, NotifyDescriptor.INFORMATION_MESSAGE);

        }
        if (created.isEmpty()) {
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    TestUtil.notifyUser(
                            NbBundle.getMessage(
                                    DefaultPlugin.class,
                                    "MSG_No_test_created"),     //NOI18N
                            NotifyDescriptor.INFORMATION_MESSAGE);
                }
            });
        }
        
        FileObject[] createdFiles;
        if (created.isEmpty()) {
            createdFiles = null;
        } else {
            createdFiles = new FileObject[created.size()];
            int i = 0;
            for (DataObject dObj : created) {
                createdFiles[i++] = dObj.getPrimaryFile();
            }
        }
        return createdFiles;
    }
    
    /**
     *
     */
    public DataObject createSuiteTest(
                                final FileObject targetRootFolder,
                                final FileObject targetFolder,
                                final String suiteName,
                                final Map<CreateTestParam, Object> params) {
        TestCreator testCreator = new TestCreator(params);
        ClassPath testClassPath = ClassPathSupport.createClassPath(
                new FileObject[] {targetRootFolder});
        List testClassNames = TestUtil.getJavaFileNames(targetFolder,
                                                        testClassPath);
        
        final DataObject doSuiteTempl
                = loadTestTemplate("PROP_testSuiteTemplate");           //NOI18N
        if (doSuiteTempl == null) {
            return null;
        }
        
        DataObject suiteDataObj;
        try {
            return createSuiteTest(testCreator,
                                   testClassPath,
                                   targetFolder,
                                   suiteName,
                                   new LinkedList(testClassNames),
                                   doSuiteTempl,
                                   null,            //parent suite
                                   null);           //progress indicator
        } catch (CreationError ex) {
            return null;
        }
    }
    
    /**
     *
     */
    private static DataObject createSuiteTest(
            final TestCreator testCreator,
            ClassPath testClassPath,
            FileObject folder,
            String suiteName,
            LinkedList suite,
            DataObject doSuiteT,
            LinkedList parentSuite,
            ProgressIndicator progress) throws CreationError {

        // find correct package name
        FileObject fo = folder;
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        assert cp != null : "SOURCE classpath was not found for " + fo; //NOI18N
        if (cp == null) {
            return null;
        }
        String pkg = cp.getResourceName(fo, '/', false);
        String dotPkg = pkg.replace('/', '.');
        String fullSuiteName = (suiteName != null)
                               ? pkg + '/' + suiteName
                               : TestUtil.convertPackage2SuiteName(pkg);

        try {
            // find the suite class,
            // if it exists or create one from active template
            DataObject doTarget = getTestClass(testClassPath,
                                               fullSuiteName,
                                               doSuiteT);

            // generate the test suite for all listed test classes
            Collection targetClasses = TestUtil.getAllClassesFromFile(
                                               doTarget.getPrimaryFile());

            Iterator tcit = targetClasses.iterator();
            while (tcit.hasNext()) {
                JavaClass targetClass = (JavaClass)tcit.next();

                if (progress != null) {
                    progress.setMessage(
                           getCreatingMsg(targetClass.getName()), false);
                }

                try {
                    testCreator.createTestSuite(suite, dotPkg, targetClass);
                    save(doTarget);
                } catch (Exception e) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR,
                                                  e.toString());
                    return null;
                }

                // add the suite class to the list of members of the parent
                if (null != parentSuite) {
                    parentSuite.add(targetClass.getName());
                }
            }
            return doTarget;
        } catch (IOException ioe) {
            throw new CreationError(ioe);
        }
    }

    /**
     *
     */
    private static CreationResults createTests(
                final TestCreator testCreator,
                final FileObject fileObj,
                final ClassPath testClassPath,
                DataObject doTestT,
                DataObject doSuiteT,
                LinkedList parentSuite,
                ProgressIndicator progress) throws CreationError {

        if (fileObj.isFolder()) {
            // create test for all direct subnodes of the folder
            CreationResults results = new CreationResults();

            LinkedList  mySuite = new LinkedList(); // List<String>
            progress.setMessage(getScanningMsg(fileObj.getName()), false);

            for (FileObject childFileObj : fileObj.getChildren()) {

                if (progress.isCanceled()) {
                    results.setAbborted();
                    break;
                }

                results.combine(createTests(testCreator,
                                            childFileObj,
                                            testClassPath,
                                            doTestT,
                                            doSuiteT,
                                            mySuite,
                                            progress));
                if (results.isAbborted()) {
                    break;
                }
            }

            // if everything went ok, and the option is enabled,
            // create a suite for the folder .
            if (!results.isAbborted()
                    && ((0 < mySuite.size())
                        & (JUnitSettings.getDefault()
                           .isGenerateSuiteClasses()))) {
                createSuiteTest(testCreator,
                                testClassPath,
                                fileObj,
                                (String) null,
                                mySuite,
                                doSuiteT,
                                parentSuite,
                                progress);
            }

            return results;
        } else {
            // is not folder, create test for the fileObject of the node
            if (fileObj.isData()
                    && !("java".equals(fileObj.getExt()))) {       //NOI18N
                return CreationResults.EMPTY;
            } else {
                return createSingleTest(testCreator,
                                        testClassPath,
                                        fileObj,
                                        null,      //use the default clsname
                                        doTestT,
                                        parentSuite,
                                        progress,
                                        true);
            }
        }
    }

    /**
     *
     */
    private static CreationResults createSingleTest(
            final TestCreator testCreator,
            ClassPath testClassPath,
            FileObject foSource,
            String testClassName,
            DataObject doTestT,
            LinkedList parentSuite,
            ProgressIndicator progress,
            boolean skipNonTestable) throws CreationError {

        // create tests for all classes in the source
        Resource srcRc = JavaModel.getResource(foSource);
        String packageName = (testClassName == null)
                             ? srcRc.getPackageName()
                             : null;            //will be built if necessary
        List srcChildren = srcRc.getChildren();
        CreationResults result = new CreationResults(srcChildren.size());

        /* used only if (testClassName != null): */
        boolean defClassProcessed = false;

        Iterator scit = srcChildren.iterator();
        while (scit.hasNext()) {
            Element el = (Element) scit.next();
            if (!(el instanceof JavaClass) || (el instanceof AnnotationType)) {
                continue;
            }

            JavaClass theClass = (JavaClass) el;

            TestCreator.TesteableResult testeable;
            if (skipNonTestable && (testeable = testCreator.isClassTestable(theClass)).isFailed()) {
                if (progress != null) {
                    // ignoring because untestable
                    progress.setMessage(
                           getIgnoringMsg(theClass.getName(), testeable.toString()), false);
                    result.addSkipped(theClass, testeable);
                }
                continue;
            }

            // find the test class, if it exists or create one
            // from active template
            try {
                String testResourceName;
                String srcClassNameShort = theClass.getSimpleName();
                if (testClassName == null) {
                    testResourceName = TestUtil.getTestClassFullName(
                            srcClassNameShort,
                            packageName);
                } else if (!defClassProcessed
                          && srcClassNameShort.equals(foSource.getName())) {
                    /* main Java class: */
                    testResourceName = testClassName.replace('.', '/');
                    defClassProcessed = true;
                } else {
                    if (packageName == null) {
                        packageName = packageName(testClassName);
                    }
                    testResourceName = TestUtil.getTestClassFullName(
                            srcClassNameShort,
                            packageName);
                }

                /* find or create the test class DataObject: */
                DataObject doTarget = getTestClass(testClassPath,
                                                   testResourceName,
                                                   doTestT);

                // generate the test of current node
                Resource tgtRc = JavaModel.getResource(
                        doTarget.getPrimaryFile());
                JavaClass targetClass = TestUtil.getMainJavaClass(tgtRc);

                if (targetClass != null) {
                    if (progress != null) {
                        progress.setMessage(
                             getCreatingMsg(targetClass.getName()), false);
                    }

                    testCreator.createTestClass(srcRc,
                                                theClass,
                                                tgtRc,
                                                targetClass);
                    save(doTarget);
                    result.addCreated(doTarget);
                    // add the test class to the parent's suite
                    if (null != parentSuite) {
                        parentSuite.add(targetClass.getName());
                    }
                }
            } catch (IOException ioe) {
                throw new CreationError(ioe);
            }
        }

        return result;

    }

    /**
     *
     */
    private static String packageName(String fullName) {
        int i = fullName.lastIndexOf('.');
        return fullName.substring(0, i > 0 ? i : 0);
    }

    /**
     *
     */
    private static DataObject getTestClass(
            ClassPath cp,
            String testClassName,
            DataObject doTemplate) throws DataObjectNotFoundException,
                                          IOException {
        FileObject fo = cp.findResource(testClassName + ".java");       //NOI18N
        if (fo != null) {
            return DataObject.find(fo);
        } else {
            // test file does not exist yet so create it:
            assert cp.getRoots().length == 1;
            FileObject root = cp.getRoots()[0];
            int index = testClassName.lastIndexOf('/');
            String pkg = index > -1 ? testClassName.substring(0, index)
                                    : "";                           //NOI18N
            String clazz = index > -1 ? testClassName.substring(index+1)
                                      : testClassName;

            // create package if it does not exist
            if (pkg.length() > 0) {
                root = FileUtil.createFolder(root, pkg);
            }
            // instantiate template into the package
            return doTemplate.createFromTemplate(
                    DataFolder.findFolder(root),
                    clazz);
        }
    }

    /**
     *
     */
    private static void save(DataObject dO) throws IOException {
        SaveCookie sc = (SaveCookie) dO.getCookie(SaveCookie.class);
        if (null != sc)
            sc.save();
    }
        
    /**
     * Loads a test template.
     * If the template loading fails, displays an error message.
     *
     * @param  templateID  bundle key identifying the template type
     * @return  loaded template, or <code>null</code> if the template
     *          could not be loaded
     */
    private static DataObject loadTestTemplate(String templateID) {
        // get the Test class template
        String path = NbBundle.getMessage(DefaultPlugin.class,
                                          templateID);
        try {
            FileObject fo = Repository.getDefault().getDefaultFileSystem()
                            .findResource(path);
            if (fo == null) {
                noTemplateMessage(path);
                return null;
            }
            return DataObject.find(fo);
        }
        catch (DataObjectNotFoundException e) {
            noTemplateMessage(path);
            return null;
        }
    }
        
    /**
     *
     */
    private static void noTemplateMessage(String temp) {
        String msg = NbBundle.getMessage(
                CreateTestAction.class,
                "MSG_template_not_found",                           //NOI18N
                temp);
        NotifyDescriptor descr = new NotifyDescriptor.Message(
                msg,
                NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(descr);
    }

    /**
     * A helper method to create the reason string from a result
     * and two message bundle keys that indicate the separators to be used instead
     * of "," and " and " in a connected reason like: 
     * "abstract, package-private and without testeable methods".
     * <p>
     * The values of the keys are expected to be framed by two extra characters
     * (e.g. as in " and "), which are stripped off. These characters serve to 
     * preserve the spaces in the properties file.
     *
     * @param reason the TestCreator.TesteableResult to represent
     * @param commaKey bundle key for the connective to be used instead of ", "
     * @param andKey   bundle key for the connective to be used instead of "and"
     * @return String composed of the reasons contained in
     *         <code>reason</code> separated by the values of commaKey and
     *         andKey.
     */
    private static String strReason(TestCreator.TesteableResult reason, String commaKey, String andKey) {
        String strComma = NbBundle.getMessage(CreateTestAction.class,commaKey);
        String strAnd = NbBundle.getMessage(CreateTestAction.class,andKey);
        String strReason = reason.getReason( // string representation of the reasons
                        strComma.substring(1, strComma.length()-1),
                        strAnd.substring(1, strAnd.length()-1));

        return strReason;

    }

    /**
     *
     */
    private static String getCreatingMsg(String className) {
        return NbBundle.getMessage(
                DefaultPlugin.class,
                "FMT_generator_status_creating",                        //NOI18N
                className);
    }

    /**
     *
     */
    private static String getScanningMsg(String sourceName) {
        return NbBundle.getMessage(
                DefaultPlugin.class,
                "FMT_generator_status_scanning",                        //NOI18N
                sourceName);
    }

    /**
     *
     */
    private static String getIgnoringMsg(String sourceName, String reason) {
        return NbBundle.getMessage(
                DefaultPlugin.class,
                "FMT_generator_status_ignoring",                        //NOI18N
                sourceName);
    }

    
    /**
     * Error thrown by failed test creation.
     */
    private static final class CreationError extends Exception {
        CreationError() {};
        CreationError(Throwable cause) {
            super(cause);
        }
    };

    
    /**
     * Utility class representing the results of a test creation
     * process. It gatheres all tests (as DataObject) created and all
     * classes (as JavaClasses) for which no test was created.
     */
    static final class CreationResults {
        static final CreationResults EMPTY = new CreationResults();

        /**
         * Class for holding skipped java class together with the reason
         * why it was skipped.
         */
        static final class SkippedClass {
            final JavaClass cls;
            final TestCreator.TesteableResult reason;
            SkippedClass(JavaClass cls,
                                TestCreator.TesteableResult reason) {
                this.cls = cls;
                this.reason = reason;
            }
        }

        Set<DataObject> created; // Set< createdTest : DataObject >
        Set<SkippedClass> skipped;
        boolean abborted = false;

        CreationResults() { this(20);}

        CreationResults(int expectedSize) {
            created = new HashSet<DataObject>(expectedSize * 2, 0.5f);
            skipped = new HashSet<SkippedClass>(expectedSize * 2, 0.5f);
        }

        void setAbborted() {
            abborted = true;
        }

        /**
         * Returns true if the process of creation was abborted. The
         * result contains the results gathered so far.
         */
        boolean isAbborted() {
            return abborted;
        }


        /**
         * Adds a new entry to the set of created tests.
         * @return true if it was added, false if it was present before
         */
        boolean addCreated(DataObject test) {
            return created.add(test);
        }

        /**
         * Adds a new <code>JavaClass</code> to the collection of
         * skipped classes.
         * @return true if it was added, false if it was present before
         */
        boolean addSkipped(JavaClass c, TestCreator.TesteableResult reason) {
            return skipped.add(new SkippedClass(c, reason));
        }

        /**
         * Returns a set of classes that were skipped in the process.
         * @return Set<SkippedClass>
         */
        Set<SkippedClass> getSkipped() {
            return skipped;
        }

        /**
         * Returns a set of test data objects created.
         * @return Set<DataObject>
         */
        Set<DataObject> getCreated() {
            return created;
        }

        /**
         * Combines two results into one. If any of the results is an
         * abborted result, the combination is also abborted. The
         * collections of created and skipped classes are unified.
         * @param rhs the other CreationResult to combine into this
         */
        void combine(CreationResults rhs) {
            if (rhs.abborted) {
                this.abborted = true;
            }

            this.created.addAll(rhs.created);
            this.skipped.addAll(rhs.skipped);
        }

    }

}
