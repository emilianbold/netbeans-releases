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
 * CreateTestAction.java
 *
 * Created on January 19, 2001, 1:00 PM
 */

package org.netbeans.modules.junit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.jmi.javamodel.*;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.util.Utilities;



/** Action sensitive to some cookie that does something useful.
 *
 * @author  vstejskal, David Konecny
 * @author  Marian Petras
 * @author  Ondrej Rypacek
 */
public class CreateTestAction extends CookieAction {
    
    /* public members */
    public String getName() {
        return NbBundle.getMessage(CreateTestAction.class, "LBL_Action_CreateTest");
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CreateTestAction.class);
    }
    
    /* protected members */
    protected Class[] cookieClasses() {
        return new Class[] { DataFolder.class, SourceCookie.class };
    }
    
    /** Perform special enablement check in addition to the normal one.
     * protected boolean enable (Node[] nodes) {
     * if (! super.enable (nodes)) return false;
     * if (...) ...;
     * }
     */
    /*
    protected boolean enable (Node[] nodes) {
        if (nodes.length == 0) {
            return false;
        }
        for (int i=0; i < nodes.length; i++) {
            Cookie cookie = nodes[i].getCookie(type
        }
    }
     **/
    
    protected void initialize() {
        super.initialize();
        putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(CreateTestAction.class, "HINT_Action_CreateTest"));
    }
    
//      protected String iconResource() {
//          return "org/netbeans/modules/junit/resources/CreateTestActionIcon.gif";
//     }
    
    protected int mode() {
        return MODE_ANY;    // allow creation of tests for multiple selected nodes (classes, packages)
    }
    
    public boolean asynchronous() {
        return true; // yes, this action should run asynchronously
        // would be better to rewrite it to synchronous (running in AWT thread),
        // just replanning test generation to RequestProcessor
    }

    private static void noTemplateMessage(String temp) {
        String msg = NbBundle.getMessage(CreateTestAction.class, "MSG_template_not_found", temp);
        NotifyDescriptor descr = new Message(msg, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(descr);
    }
    
    protected void performAction(Node[] nodes) {
        boolean folderSelected = isFolderSelected(nodes);
        
        DataObject doTestTempl = null;
        DataObject doSuiteTempl = null;
        
        // show configuration dialog
        // when dialog is canceled, escape the action
        if (!JUnitCfgOfCreate.configure(folderSelected))
            return;
        
        String temp = null;
        try {
            // get the Suite class template
            temp = NbBundle.getMessage(CreateTestAction.class,
                                       "PROP_testSuiteTemplate");       //NOI18N
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(temp);
            if (fo == null) { noTemplateMessage(temp); return;}
            doSuiteTempl = DataObject.find(fo);

            // get the Test class template
            temp = NbBundle.getMessage(CreateTestAction.class,
                                       "PROP_testClassTemplate");       //NOI18N
            fo = Repository.getDefault().getDefaultFileSystem().findResource(temp);

            if (fo == null) { noTemplateMessage(temp); return;}
            doTestTempl = DataObject.find(fo);
        }
        catch (DataObjectNotFoundException e) {
            noTemplateMessage(temp);
            return;
        }
        
        TestCreator.initialize();
        
        ProgressIndicator progress = new ProgressIndicator();
        progress.show();
        
        String msg = NbBundle.getMessage(CreateTestAction.class,
            "MSG_StatusBar_CreateTest_Begin"); //NOI18N
        progress.displayStatusText(msg);
        
        // results will be accumulated here
        CreationResults results = new CreationResults();

        try {

            // go through all nodes
            for(int nodeIdx = 0; nodeIdx < nodes.length; nodeIdx++) {
                if (hasParentAmongNodes(nodes, nodeIdx)) {
                    continue;
                }
                FileObject fo = TestUtil.getFileObjectFromNode(nodes[nodeIdx]);
                if (fo == null) {
                    TestUtil.notifyUser(NbBundle.getMessage(CreateTestAction.class, "MSG_file_from_node_failed"));
                    continue;
                }
                ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                if (cp == null) {
                    TestUtil.notifyUser(NbBundle.getMessage(CreateTestAction.class,
                        "MSG_no_project", fo));
                    continue;
                }
                ClassPath testClassPath = null;

                FileObject packageRoot = cp.findOwnerRoot(fo);
                String resource = cp.getResourceName(fo, '/', false);
                
                URL testRoot = UnitTestForSourceQuery.findUnitTest(packageRoot);                              
                if (testRoot == null) {
                    testClassPath = cp;
//                     TestUtil.notifyUser(NbBundle.getMessage(CreateTestAction.class,
//                         "MSG_no_tests_in_project", fo));
//                     continue;
                } else {
                    // TODO : workaround for #50173
                    if (!testRoot.getFile().endsWith("/")) {
                        try {
                            testRoot = new URL(testRoot.getProtocol(), testRoot.getHost(), testRoot.getPort(), testRoot.getFile()+"/");                                                                   
                        } catch (java.net.MalformedURLException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                    // workaround ends
                    
                    if (testRoot.getProtocol().equals("file") && URLMapper.findFileObject(testRoot) == null) { // NOI18N
                        try {
                         ensureFolder(testRoot);   
                        } catch (java.io.IOException ex) {
                            ErrorManager.getDefault().notify(ex);
                            continue;
                        }
                    }

                    ArrayList cpItems = new ArrayList();                   
                    cpItems.add(ClassPathSupport.createResource(testRoot));
                    testClassPath = ClassPathSupport.createClassPath(cpItems);
                }

                try {
                    results.combine(createTests(testClassPath, fo, doTestTempl, doSuiteTempl, null, progress));
                } catch (CreationError e) {
                    ErrorManager.getDefault().notify(e);
                }

            }

        } finally {
            progress.hide();
        }


        if (!results.getSkipped().isEmpty()) {
            // something was skipped
            if (results.getSkipped().size()==1) {
                // one class? report it
                TestUtil.notifyUser
                    (NbBundle.getMessage(CreateTestAction.class,
                                         "MSG_skipped_class",   // NOI18N
                                         ((JavaClass)results.getSkipped().iterator().next()).getName()),
                     NotifyDescriptor.INFORMATION_MESSAGE );

            } else {
                // more classes, report a general error
                TestUtil.notifyUser
                    (NbBundle.getMessage(CreateTestAction.class,
                                         "MSG_skipped_classes"), // NOI18N
                     NotifyDescriptor.INFORMATION_MESSAGE);
            }
                        
        } else if (results.getCreated().size()==1) {
            // created exactly one class, highlight it in the explorer
            // and open it in the editor
            DataObject dobj = (DataObject)results.getCreated().iterator().next();
            EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
            if (ec != null) {
                ec.open();
            }
        }
    }

    private static void ensureFolder(URL url) throws java.io.IOException {
        if (url.getProtocol().equals("file")) { // NOI18N
            String path = url.getPath();
            ensureFolder(new File(path));          
        } 
    }
     
    private static FileObject ensureFolder(File file) throws java.io.IOException {          
        File parent = file.getParentFile();
        String name = file.getName();
        FileObject pfo = FileUtil.toFileObject(parent);
        if (pfo == null) pfo = ensureFolder(parent);
        return pfo.createFolder(name);        
    }
    
    /**
     * Detects whether at least one of the given nodes represents
     * a <code>DataFolder</code>.
     *
     * @return  <code>true</code> if at least one of the nodes represents
     *          a <code>DataFolder</code>; <code>false</code> otherwise
     */
    private static boolean isFolderSelected(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].getCookie(DataFolder.class) != null) {
                return true;
            }
        }
        return false;
    }
    
    public static DataObject createSuiteTest(ClassPath testClassPath, 
                                             DataFolder folder,
                                             String suiteName,
                                             LinkedList suite, 
                                             DataObject doSuiteT, 
                                             LinkedList parentSuite,
                                             ProgressIndicator progress) 
                                             throws CreationError
    {
        
        // find correct package name
        FileObject fo = folder.getPrimaryFile();
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        assert cp != null : "SOURCE classpath was not found for "+fo;
        if (cp == null) {
            return null;
        }
        String pkg = cp.getResourceName(fo, '/', false);
        String dotPkg = pkg.replace('/', '.');
        String fullSuiteName = (suiteName != null)
                               ? pkg + '/' + suiteName
                               : TestUtil.convertPackage2SuiteName(pkg);
        
        try {
            // find the suite class, if it exists or create one from active template
            DataObject doTarget = getTestClass(testClassPath, fullSuiteName, doSuiteT);

            // generate the test suite for all listed test classes
            Collection targetClasses = TestUtil.getAllClassesFromFile(doTarget.getPrimaryFile());

            Iterator tcit = targetClasses.iterator();
            while (tcit.hasNext()) {
                JavaClass targetClass = (JavaClass)tcit.next();

                if (progress != null) {
                    progress.setMessage(getCreatingMsg(targetClass.getName()), false);
                }

                try {
                    TestCreator.createTestSuite(suite, dotPkg, targetClass);
                    save(doTarget);
                } catch (Exception e) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, e.toString());
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
    
    private CreationResults createTests(ClassPath testClassPath, FileObject foSource, 
            DataObject doTestT, DataObject doSuiteT, LinkedList parentSuite,
            ProgressIndicator progress) throws CreationError {

                
        if (foSource.isFolder()) {
            // create test for all files (not folders) in the folder
            // don't recurse           
            FileObject  childs[] = foSource.getChildren();
            LinkedList  mySuite = new LinkedList();
            progress.setMessage(getScanningMsg(foSource.getName()), false);

            CreationResults results = new CreationResults();

            for( int i = 0; i < childs.length; i++) {

                if (progress.isCanceled()) {
                    results.setAbborted();
                    break;
                }

                if (childs[i].isFolder() || (childs[i].isData() && !("java".equals(childs[i].getExt())))) {
                    continue;
                }

                results.combine(createTests(testClassPath, childs[i], doTestT, doSuiteT, mySuite, progress));
                if (results.isAbborted()) {
                    break;
                }

            }
            
            // if everything went ok, and the option is enabled,
            // create a suite for the folder . 
            if (!results.isAbborted() && ((0 < mySuite.size())&(JUnitSettings.getDefault().isGenerateSuiteClasses()))) {
                createSuiteTest(testClassPath, DataFolder.findFolder(foSource), (String) null, mySuite, doSuiteT, parentSuite, progress);
            }

            return results;

        } else {
            return createSingleTest(testClassPath, foSource, doTestT, doSuiteT, parentSuite, progress, true);
        }
    }
    
    public static CreationResults createSingleTest(ClassPath testClassPath,  
                                                   FileObject foSource, 
                                                   DataObject doTestT, 
                                                   DataObject doSuiteT, 
                                                   LinkedList parentSuite,
                                                   ProgressIndicator progress, 
                                                   boolean skipNonTestable) 
        throws CreationError
    {
        // create tests for all classes in the source 
        Resource srcRc = JavaModel.getResource(foSource);
        CreationResults result = new CreationResults(srcRc.getChildren().size());

        List srcChildren = srcRc.getChildren();
        Iterator scit = srcChildren.iterator();
        while (scit.hasNext()) {
            Element el = (Element)scit.next();
            if (el instanceof JavaClass) {
                JavaClass theClass = (JavaClass)el;
                if (!skipNonTestable || TestCreator.isClassTestable(theClass)) {
                    // find the test class, if it exists or create one
                    // from active template
                    try {
                        DataObject doTarget = getTestClass(testClassPath, 
                                                       TestUtil.getTestClassFullName(theClass.getSimpleName(),
                                                       packageName(theClass.getName())), 
                                                       doTestT);

                        // generate the test of current node
                        Resource tgtRc = JavaModel.getResource(doTarget.getPrimaryFile());
                        JavaClass targetClass = TestUtil.getMainJavaClass(tgtRc);

                        if (targetClass != null) {                  
                            if (progress != null) {
                                progress.setMessage(getCreatingMsg(targetClass.getName()), false);
                            }                    

                            TestCreator.createTestClass(srcRc, theClass, tgtRc, targetClass);
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
                } else {
                    if (progress != null) {
                        // ignoring because untestable
                        progress.setMessage(getIgnoringMsg(theClass.getName()), false);
                        result.addSkipped(theClass);
                    }
                }
            }
        }

        return result;

    }
    
    private static String packageName(String fullName) {
        int i = fullName.lastIndexOf('.');
        return fullName.substring(0, i > 0 ? i : 0);
    }

    private static DataObject getTestClass(ClassPath cp, String testClassName, DataObject doTemplate) 
        throws DataObjectNotFoundException, IOException
    {
        FileObject fo = cp.findResource(testClassName+".java");
        if (fo != null) {
            return DataObject.find(fo);
        } else {
            // test file does not exist yet so create it:
            assert cp.getRoots().length == 1;
            FileObject root = cp.getRoots()[0];
            int index = testClassName.lastIndexOf('/');
            String pkg = index > -1 ? testClassName.substring(0, index) : "";
            String clazz = index > -1 ? testClassName.substring(index+1) : testClassName;
      
            // create package if it does not exist
            if (pkg.length() > 0) {
                root = FileUtil.createFolder(root, pkg);
            }
            // instantiate template into the package
            return doTemplate.createFromTemplate(DataFolder.findFolder(root), clazz);
        }
    }
    
    private boolean hasParentAmongNodes(Node[] nodes, int idx) {
        Node node;
        
        node = nodes[idx].getParentNode();
        while (null != node) {
            for(int i = 0; i < nodes.length; i++) {
                if (i == idx)
                    continue;
                if (node == nodes[i])
                    return true;
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
        String fmt = NbBundle.getMessage(CreateTestAction.class,
                                         "FMT_generator_status_creating"); // NOI18N
        return MessageFormat.format(fmt, new Object[] { className });
    }

    private static String getScanningMsg(String sourceName) {
        String fmt = NbBundle.getMessage(CreateTestAction.class,
                                         "FMT_generator_status_scanning"); // NOI18N
        return MessageFormat.format(fmt, new Object[] { sourceName });
    }

    private static String getIgnoringMsg(String sourceName) {
        String fmt = NbBundle.getMessage(CreateTestAction.class,
                                         "FMT_generator_status_ignoring"); // NOI18N
        return MessageFormat.format(fmt, new Object[] { sourceName });
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
        
        Set created; // Set< createdTest : DataObject >
        Set skipped; // Set< sourceClass : JavaClass >
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
        public boolean addSkipped(JavaClass c) {
            return skipped.add(c);
        }

        /**
         * Returns a set of classes that were skipped in the process.
         * @return Set<JavaClass>
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
