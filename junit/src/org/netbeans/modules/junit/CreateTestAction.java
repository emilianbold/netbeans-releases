/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.jmi.javamodel.*;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;



/** Action sensitive to some cookie that does something useful.
 *
 * @author  vstejskal, David Konecny
 * @author  Marian Petras
 * @author  Ondrej Rypacek
 */
public class CreateTestAction extends TestAction {
        
        public CreateTestAction() {
            putValue("noIconInMenu", Boolean.TRUE);                     //NOI18N
        }
        
        /* public members */
        public String getName() {
            return NbBundle.getMessage(CreateTestAction.class,
                                       "LBL_Action_CreateTest");        //NOI18N
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(CreateTestAction.class);
        }
        
        
        
        protected void initialize() {
            super.initialize();
            putProperty(Action.SHORT_DESCRIPTION,
                        NbBundle.getMessage(CreateTestAction.class,
                                            "HINT_Action_CreateTest")); //NOI18N
        }
        
        protected String iconResource() {
            return "org/netbeans/modules/junit/resources/"              //NOI18N
                   + "CreateTestActionIcon.gif";                        //NOI18N
        }
        

        
        private static void noTemplateMessage(String temp) {
            String msg = NbBundle.getMessage(
                    CreateTestAction.class,
                    "MSG_template_not_found",                           //NOI18N
                    temp);
            NotifyDescriptor descr = new Message(
                    msg,
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(descr);
        }
        

        /**
         * Checks that the selection of nodes the dialog is invoked on is valid. 
         * @return String message describing the problem found or null, if the
         *         selection is ok
         */
         private static String checkNodesValidity(Node[] nodes) {
             FileObject [] files = getFiles(nodes);
             
             Project project = getProject(files);
             if (project == null) return NbBundle.getMessage(CreateTestAction.class, "MSG_multiproject_selection");
             
             if (!checkPackages(files)) return NbBundle.getMessage(CreateTestAction.class, "MSG_invalid_packages");
             
             return null;
         }
         
         
         /**
          * Check that all the files (folders or java files) have correct java
          * package names.
          * @return true if all are fine
          */
         private static boolean checkPackages(FileObject [] files) {
             if (files.length == 0) return true;
             else {
                 Project project = FileOwnerQuery.getOwner(files[0]);
                 for (int i = 0 ; i < files.length; i++) {
                     String packageName = getPackage(project, files[i]);
                     if (packageName == null || !TestUtil.isValidPackageName(packageName))
                         return false;
                 }
                 return true;
             }
         }

         /**
          * Get the package name of <code>file</code>.
          *
          * @param project owner of the file (for performance reasons)
          * @param file the FileObject whose packagename to get
          * @return package name of the file or null if it cannot be retrieved
          */
         private static String getPackage(Project project, FileObject file) {
             SourceGroup srcGrp = TestUtil.findSourceGroupOwner(project, file);
             if (srcGrp!= null) {
                 ClassPath cp = ClassPathSupport.createClassPath(new FileObject [] {srcGrp.getRootFolder()});
                 return cp.getResourceName(file, '.', false);
             } else return null;
         }
             
         
         private static FileObject [] getFiles(Node [] nodes) {
             FileObject [] ret = new FileObject[nodes.length];
             for (int i = 0 ; i < nodes.length ; i++) {
                 ret[i]  = TestUtil.getFileObjectFromNode(nodes[i]);
             }
             return ret;
         }
         
         /**
          * Get the single project for <code>nodes</code> if there is such.
          * If the nodes belong to different projects or some of the nodes doesn't
          * have a project, return null.
          */
         private static Project getProject(FileObject [] files) {
             Project project = null;
             for (int i = 0 ; i < files.length; i++) {
                 Project nodeProject = FileOwnerQuery.getOwner(files[i]);
                 if (project == null) project = nodeProject;
                 else if (project != nodeProject) return null;
             }
             return project;
         }
         
        protected void performAction(Node[] nodes) {
            String problem;
            if ((problem = checkNodesValidity(nodes))!=null) {
                // TODO report problem
                NotifyDescriptor msg = new NotifyDescriptor.Message(problem, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return;
            } 
            
            // show configuration dialog
            // when dialog is canceled, escape the action
            JUnitCfgOfCreate cfg = new JUnitCfgOfCreate(nodes);
            if (!cfg.configure()) {
                return;
            }
            final boolean singleClass = (nodes.length == 1)
                                        && cfg.isSingleClass();
            String testClassName = singleClass ? cfg.getTestClassName() : null;

            final FileObject targetFolder = cfg.getTargetFolder();
            final ClassPath testClassPath = ClassPathSupport.createClassPath(
                                               new FileObject[] {targetFolder});

            DataObject doTestTempl;
            if ((doTestTempl = loadTestTemplate("PROP_testClassTemplate"))
                    == null) {
                return;
            }

            DataObject doSuiteTempl = null;
            if (!singleClass) {
                if ((doSuiteTempl = loadTestTemplate("PROP_testSuiteTemplate"))
                        == null) {
                    return;
                }
            }

            ProgressIndicator progress = new ProgressIndicator();
            progress.show();

            String msg = NbBundle.getMessage(
                    CreateTestAction.class,
                    "MSG_StatusBar_CreateTest_Begin");                  //NOI18N
            progress.displayStatusText(msg);

            // results will be accumulated here
            CreationResults results;
            final TestCreator testCreator = new TestCreator(true);
            try {
                if (singleClass) {
                    assert testClassName != null;

                    FileObject fo = getTestFileObject(nodes[0]);
                    if (fo != null) {
                        try {
                            results = createSingleTest(
                                    testCreator,
                                    testClassPath,
                                    fo,
                                    testClassName,
                                    doTestTempl,
                                    null,              //parent suite
                                    progress,
                                    false);            //do not skip any classes
                        } catch (CreationError ex) {
                            ErrorManager.getDefault().notify(ex);
                            results = new CreationResults();
                        }
                    } else {
                        results = new CreationResults();
                    }
                } else {
                    results = new CreationResults();

                    // go through all nodes
                    for(int nodeIdx = 0; nodeIdx < nodes.length; nodeIdx++) {
                        if (hasParentAmongNodes(nodes, nodeIdx)) {
                            continue;
                        }
                        FileObject fo = getTestFileObject(nodes[nodeIdx]);
                        if (fo == null) {
                            continue;
                        }
                        try {
                            results.combine(createTests(testCreator,
                                                        nodes[nodeIdx],
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


            if (!results.getSkipped().isEmpty()) {
                // something was skipped
                String message;
                if (results.getSkipped().size() == 1) {
                    // one class? report it
                    CreationResults.SkippedClass skippedClass = 
                            (CreationResults.SkippedClass)results.getSkipped().iterator().next();


                    message = NbBundle.getMessage(CreateTestAction.class,
                                                  "MSG_skipped_class",  //NOI18N
                                                  skippedClass.cls.getName(),
                                                  strReason(skippedClass.reason, "COMMA", "AND")); //NOI18n
                } else {
                    // more classes, report a general error
                    // combine the results
                    Iterator it = results.getSkipped().iterator();
                    TestCreator.TesteableResult reason = TestCreator.TesteableResult.OK;
                    while (it.hasNext()) {
                        CreationResults.SkippedClass sc = (CreationResults.SkippedClass)it.next();
                        reason = TestCreator.TesteableResult.combine(reason, sc.reason);
                    }

                    message = NbBundle.getMessage(CreateTestAction.class,
                                                  "MSG_skipped_classes", // NOI18N
                                                  strReason(reason, "COMMA", "OR"));// NOI18N
                }
                TestUtil.notifyUser(message,
                                    NotifyDescriptor.INFORMATION_MESSAGE);

            } else if (results.getCreated().size() == 1) {
                // created exactly one class, highlight it in the explorer
                // and open it in the editor
                DataObject dobj = (DataObject)
                                  results.getCreated().iterator().next();
                EditorCookie ec = (EditorCookie)
                                  dobj.getCookie(EditorCookie.class);
                if (ec != null) {
                    ec.open();
                }
            }
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
         * Loads a test template.
         * If the template loading fails, displays an error message.
         *
         * @param  templateID  bundle key identifying the template type
         * @return  loaded template, or <code>null</code> if the template
         *          could not be loaded
         */
        private static DataObject loadTestTemplate(String templateID) {
            // get the Test class template
            String path = NbBundle.getMessage(CreateTestAction.class,
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
         * Grabs and checks a <code>FileObject</code> from the given node.
         * If either the file could not be grabbed or the file does not pertain
         * to any project, a message is displayed.
         *
         * @param  node  node to get a <code>FileObject</code> from.
         * @return  the grabbed <code>FileObject</code>,
         *          or <code>null</code> in case of failure
         */
        private static FileObject getTestFileObject(final Node node) {
            final FileObject fo = TestUtil.getFileObjectFromNode(node);
            if (fo == null) {
                TestUtil.notifyUser(NbBundle.getMessage(
                        CreateTestAction.class,
                        "MSG_file_from_node_failed"));                  //NOI18N
                return null;
            }
            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            if (cp == null) {
                TestUtil.notifyUser(NbBundle.getMessage(
                        CreateTestAction.class,
                        "MSG_no_project",                               //NOI18N
                        fo));
                return null;
            }
            return fo;
        }
        
        private static void ensureFolder(URL url) throws java.io.IOException {
            if (url.getProtocol().equals("file")) { // NOI18N
                String path = url.getPath();
                ensureFolder(new File(path));
            }
        }
        
        private static FileObject ensureFolder(File file)
                throws java.io.IOException {
            File parent = file.getParentFile();
            String name = file.getName();
            FileObject pfo = FileUtil.toFileObject(parent);
            if (pfo == null) pfo = ensureFolder(parent);
            return pfo.createFolder(name);
        }
        
        public static DataObject createSuiteTest(
                final TestCreator testCreator,
                ClassPath testClassPath,
                DataFolder folder,
                String suiteName,
                LinkedList suite,
                DataObject doSuiteT,
                LinkedList parentSuite,
                ProgressIndicator progress) throws CreationError {
            
            // find correct package name
            FileObject fo = folder.getPrimaryFile();
            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            assert cp != null : "SOURCE classpath was not found for " + fo;
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
        
        private CreationResults createTests(
                    final TestCreator testCreator,
                    Node node,
                    ClassPath testClassPath,
                    DataObject doTestT,
                    DataObject doSuiteT,
                    LinkedList parentSuite,
                    ProgressIndicator progress) throws CreationError {
            
            FileObject foSource = TestUtil.getFileObjectFromNode(node);
            if (foSource.isFolder()) {
                // create test for all direct subnodes of the folder
                Node  childs[] = node.getChildren().getNodes(true);
                CreationResults results = new CreationResults();
                
                LinkedList  mySuite = new LinkedList(); // List<String>
                progress.setMessage(getScanningMsg(foSource.getName()), false);
                
                for (int ch = 0; ch < childs.length;ch++) {
                    
                    if (progress.isCanceled()) {
                        results.setAbborted();
                        break;
                    }
                    
                    results.combine(createTests(testCreator,
                                                childs[ch],
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
                                    DataFolder.findFolder(foSource),
                                    (String) null,
                                    mySuite,
                                    doSuiteT,
                                    parentSuite,
                                    progress);
                }
                
                return results;
            } else {
                // is not folder, create test for the fileObject of the node
                if (foSource.isData()
                        && !("java".equals(foSource.getExt()))) {       //NOI18N
                    return CreationResults.EMPTY;
                } else {
                    return createSingleTest(testCreator,
                                            testClassPath,
                                            foSource,
                                            null,      //use the default clsname
                                            doTestT,
                                            parentSuite,
                                            progress,
                                            true);
                }
            }
        }
        
        public static CreationResults createSingleTest(
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
                Element el = (Element)scit.next();
                if (!(el instanceof JavaClass)) {
                    continue;
                }
                
                JavaClass theClass = (JavaClass)el;
                
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
        
        private static String packageName(String fullName) {
            int i = fullName.lastIndexOf('.');
            return fullName.substring(0, i > 0 ? i : 0);
        }
        
        private static DataObject getTestClass(
                ClassPath cp,
                String testClassName,
                DataObject doTemplate) throws DataObjectNotFoundException,
                                              IOException {
            FileObject fo = cp.findResource(testClassName + ".java");   //NOI18N
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
        
        private boolean hasParentAmongNodes(Node[] nodes, int idx) {
            Node node;
            
            node = nodes[idx].getParentNode();
            while (null != node) {
                for (int i = 0; i < nodes.length; i++) {
                    if (i == idx) {
                        continue;
                    }
                    if (node == nodes[i]) {
                        return true;
                    }
                }
                node = node.getParentNode();
            }
            return false;
        }
        
        private static void save(DataObject dO) throws IOException {
            SaveCookie sc = (SaveCookie) dO.getCookie(SaveCookie.class);
            if (null != sc)
                sc.save();
        }
        
        private static String getCreatingMsg(String className) {
            String fmt = NbBundle.getMessage(
                    CreateTestAction.class,
                    "FMT_generator_status_creating");                   //NOI18N
            return MessageFormat.format(fmt, new Object[] { className });
        }
        
        private static String getScanningMsg(String sourceName) {
            String fmt = NbBundle.getMessage(
                    CreateTestAction.class,
                    "FMT_generator_status_scanning");                   //NOI18N
            return MessageFormat.format(fmt, new Object[] { sourceName });
        }
        
        private static String getIgnoringMsg(String sourceName, String reason) {
            String fmt = NbBundle.getMessage(
                    CreateTestAction.class,
                    "FMT_generator_status_ignoring");                   //NOI18N
            return MessageFormat.format(fmt, new Object[] { sourceName});
        }
        
        
        
        /**
         * Error thrown by failed test creation.
         */
        public static final class CreationError extends Exception {
            public CreationError() {};
            public CreationError(Throwable cause) {
                super(cause);
            }
        };
        
        /**
         * Utility class representing the results of a test creation
         * process. It gatheres all tests (as DataObject) created and all
         * classes (as JavaClasses) for which no test was created.
         */
        public static class CreationResults {
            public static final CreationResults EMPTY = new CreationResults();
            
            /**
             * Class for holding skipped java class together with the reason
             * why it was skipped.
             */
            public static final class SkippedClass {
                public final JavaClass cls;
                public final TestCreator.TesteableResult reason;
                public SkippedClass(JavaClass cls, TestCreator.TesteableResult reason) {
                    this.cls = cls;
                    this.reason = reason;
                }
            }
            
            Set created; // Set< createdTest : DataObject >
            Set skipped; // Set< SkippedClass >
            boolean abborted = false;
            
            public CreationResults() { this(20);}
            
            public CreationResults(int expectedSize) {
                created = new HashSet(expectedSize * 2 , 0.5f);
                skipped = new HashSet(expectedSize * 2 , 0.5f);
            }
            
            public void setAbborted() {
                abborted = true;
            }
            
            /**
             * Returns true if the process of creation was abborted. The
             * result contains the results gathered so far.
             */
            public boolean isAbborted() {
                return abborted;
            }
            
            
            /**
             * Adds a new entry to the set of created tests.
             * @return true if it was added, false if it was present before
             */
            public boolean addCreated(DataObject test) {
                return created.add(test);
            }
            
            /**
             * Adds a new <code>JavaClass</code> to the collection of
             * skipped classes.
             * @return true if it was added, false if it was present before
             */
            public boolean addSkipped(JavaClass c, TestCreator.TesteableResult reason) {
                return skipped.add(new SkippedClass(c, reason));
            }
            
            /**
             * Returns a set of classes that were skipped in the process.
             * @return Set<SkippedClass>
             */
            public Set getSkipped() {
                return skipped;
            }
            
            /**
             * Returns a set of test data objects created.
             * @return Set<DataObject>
             */
            public Set getCreated() {
                return created;
            }
            
            /**
             * Combines two results into one. If any of the results is an
             * abborted result, the combination is also abborted. The
             * collections of created and skipped classes are unified.
             * @param rhs the other CreationResult to combine into this
             */
            public void combine(CreationResults rhs) {
                if (rhs.abborted) {
                    this.abborted = true;
                }
                
                this.created.addAll(rhs.created);
                this.skipped.addAll(rhs.skipped);
            }
            
        }
        
    }
