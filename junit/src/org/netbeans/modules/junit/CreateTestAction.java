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

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
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
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.src.ClassElement;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/** Action sensitive to some cookie that does something useful.
 *
 * @author  vstejskal, David Konecny
 * @version 1.0
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
        return new Class[] { DataFolder.class, SourceCookie.class, ClassElement.class };
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
    
    protected String iconResource() {
        return "org/netbeans/modules/junit/resources/CreateTestActionIcon.gif";
    }
    
    protected int mode() {
        return MODE_ANY;    // allow creation of tests for multiple selected nodes (classes, packages)
    }
    
    public boolean asynchronous() {
        return true; // yes, this action should run asynchronously
        // would be better to rewrite it to synchronous (running in AWT thread),
        // just replanning test generation to RequestProcessor
    }

    protected void performAction(Node[] nodes) {
        DataObject doTestTempl = null;
        DataObject doSuiteTempl = null;
        
        // show configuration dialog
        // when dialog is canceled, escape the action
        if (!JUnitCfgOfCreate.configure())
            return;
        
        String temp = null;
        try {
            // get the Suite class template
            temp = JUnitSettings.getDefault().getSuiteTemplate();
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(temp);
            doSuiteTempl = DataObject.find(fo);
            
            // get the Test class template
            temp = JUnitSettings.getDefault().getClassTemplate();
            fo = Repository.getDefault().getDefaultFileSystem().findResource(temp);
            doTestTempl = DataObject.find(fo);
        }
        catch (DataObjectNotFoundException e) {
            String msg = NbBundle.getMessage(CreateTestAction.class, "MSG_template_not_found", temp);
            NotifyDescriptor descr = new Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(descr);
            return;
        }
        
        TestCreator.initialize();
        
        ProgressIndicator progress = new ProgressIndicator();
        progress.show();
        
        String msg = NbBundle.getMessage(CreateTestAction.class,
            "MSG_StatusBar_CreateTest_Begin"); //NOI18N
        progress.displayStatusText(msg);
        
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
                FileObject packageRoot = cp.findOwnerRoot(fo);
                String resource = cp.getResourceName(fo, '/', false);
                
                URL testRoot = UnitTestForSourceQuery.findUnitTest(packageRoot);
                if (testRoot == null) {
                    TestUtil.notifyUser(NbBundle.getMessage(CreateTestAction.class,
                        "MSG_no_tests_in_project", fo));
                    continue;
                }
                ArrayList cpItems = new ArrayList();
                cpItems.add(ClassPathSupport.createResource(testRoot));
                ClassPath testClassPath = ClassPathSupport.createClassPath(cpItems);
                createTests(testClassPath, fo, doTestTempl, doSuiteTempl, null, progress);
            }
        } finally {
            progress.hide();
        }
    }
    
    private void createSuiteTest(ClassPath testClassPath, DataFolder folder,
            LinkedList suite, DataObject doSuiteT, LinkedList parentSuite,
            ProgressIndicator progress) {
        
        // find correct package name
        FileObject fo = folder.getPrimaryFile();
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        assert cp != null : "SOURCE classpath was not found for "+fo;
        if (cp == null) {
            return;
        }
        String pkg = cp.getResourceName(fo, '/', false);
        
        // find the suite class, if it exists or create one from active template
        DataObject doTarget = getTestClass(testClassPath, TestUtil.convertPackage2SuiteName(pkg), doSuiteT);
        // generate the test suite for all listed test classes
        ClassElement[] classTargets = TestUtil.getAllClassElementsFromDataObject(doTarget);

        for (int i=0; i < classTargets.length; i++) {
            ClassElement classTarget = classTargets[i];
            progress.setMessage(getCreatingMsg(classTarget.getName().getFullName()), false);

            try {
                TestCreator.createTestSuite(suite, pkg.replace('/', '.'), classTarget);
                save(doTarget);
            } catch (Exception e) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, e.toString());
                return;
            }

            // add the suite class to the list of members of the parent
            if (null != parentSuite) {
                parentSuite.add(classTarget.getName().getFullName());
            }
        }
    }
    
    private boolean createTests(ClassPath testClassPath, FileObject foSource, 
            DataObject doTestT, DataObject doSuiteT, LinkedList parentSuite,
            ProgressIndicator progress) {
                
        if (foSource.isFolder()) {
            // recurse of subfolders
            FileObject  childs[] = foSource.getChildren();
            LinkedList  mySuite = new LinkedList();
            
            progress.setMessage(getScanningMsg(foSource.getName()), false);
            for( int i = 0; i < childs.length; i++) {
                if (progress.isCanceled()) {
                    return false;
                }
                if (childs[i].isData() && !("java".equals(childs[i].getExt()))) {
                    continue;
                }
                if (!createTests(testClassPath, childs[i], doTestT, doSuiteT, mySuite, progress)) {
                    // aborted
                    return false;
                }
            }
            
            if ((0 < mySuite.size())&(JUnitSettings.getDefault().isGenerateSuiteClasses())) {
                createSuiteTest(testClassPath, DataFolder.findFolder(foSource), mySuite, doSuiteT, parentSuite, progress);
            }
        } else {
            createSingleTest(testClassPath, foSource, doTestT, doSuiteT, parentSuite, progress);
        }
        return true;
    }
    
    private void createSingleTest(ClassPath testClassPath, FileObject foSource,
            DataObject doTestT, DataObject doSuiteT, LinkedList parentSuite,
            ProgressIndicator progress) {
                
        DataObject dobj;
        try {
            dobj = DataObject.find(foSource);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, ex.toString());
            return;
        }

        ClassElement[] classSources = TestUtil.getAllClassElementsFromDataObject(dobj);
        for (int i=0; i < classSources.length; i++) {
            ClassElement classSource = classSources[i];
            if (classSource == null) {
                continue;
            }
            if (TestCreator.isClassTestable(foSource, classSource)) {
                // find the test class, if it exists or create one from active template
                DataObject doTarget = getTestClass(testClassPath, TestUtil.getTestClassFullName(classSource), doTestT);

                // generate the test of current node
                ClassElement classTarget = TestUtil.getClassElementFromDataObject(doTarget);

                progress.setMessage(getCreatingMsg(classTarget.getName().getFullName()), false);

                try {
                    TestCreator.createTestClass(foSource, classSource, doTarget.getPrimaryFile(), classTarget);
                    save(doTarget);
                } catch (Exception e) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, e.toString());
                    return;
                }

                String name = classTarget.getName().getFullName();
                // add the test class to the parent's suite
                if (null != parentSuite) {
                    parentSuite.add(name);
                }
            }
            else {
                progress.setMessage(getIgnoringMsg(classSource.getName().getFullName()), false);
            }
        }
    }
    
    private DataObject getTestClass(ClassPath cp, String testClassName, DataObject doTemplate) {
        FileObject fo = cp.findResource(testClassName+".java");
        if (fo != null) {
            try {
                return DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, e.toString());
                return null;
            }
        } else {
            // test file does not exist yet so create it:
            assert cp.getRoots().length == 1;
            FileObject root = cp.getRoots()[0];
            int index = testClassName.lastIndexOf('/');
            String pkg = index > -1 ? testClassName.substring(0, index) : "";
            String clazz = index > -1 ? testClassName.substring(index+1) : testClassName;
            try {
                // create package if it does not exist
                if (pkg.length() > 0) {
                    root = FileUtil.createFolder(root, pkg);
                }
                // instantiate template into the package
                return doTemplate.createFromTemplate(DataFolder.findFolder(root), clazz);
            } catch (IOException e) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, e.toString());
                return null;
            }
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
    
    private void save(DataObject dO) throws IOException {
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
}
