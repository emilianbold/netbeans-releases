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
 * CreateTestAction.java
 *
 * Created on January 19, 2001, 1:00 PM
 */

package org.netbeans.modules.junit;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.loaders.*;
import org.openide.src.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem; // override java.io.FileSystem
import org.openide.cookies.*;

import java.util.*;
import java.io.*;
import java.text.MessageFormat;

/** Action sensitive to some cookie that does something useful.
 *
 * @author  vstejskal
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
        //return new Class[] { DataFolder.class, DataObject.class, ClassElement.class };
        // return new Class[] { DataFolder.class, SourceCookie.Editor.class, ClassElement.class };
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
        putProperty(javax.swing.Action.SHORT_DESCRIPTION, NbBundle.getMessage(CreateTestAction.class, "HINT_Action_CreateTest"));
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
        FileSystem      fsTest = null;
        DataObject      doTestTempl = null;
        DataObject      doSuiteTempl = null;
        String          temp;
        FileObject      fo;
        
        // show configuration dialog - get the test file system and other settings
        // when dialog is canceled, escape the action
        if (!JUnitCfgOfCreate.configure())
            return;
        
        // get the target file system
        temp = JUnitSettings.getDefault().getFileSystem();
        if (null == (fsTest = Repository.getDefault().findFileSystem(temp))) {
            String msg = NbBundle.getMessage(CreateTestAction.class, "MSG_file_system_not_found");
            NotifyDescriptor descr = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            org.openide.DialogDisplayer.getDefault().notify(descr);
            return;
        }
        
        try {
            // get the Suite class template
            temp = JUnitSettings.getDefault().getSuiteTemplate();
            fo = Repository.getDefault().getDefaultFileSystem().findResource(temp);
            doSuiteTempl = DataObject.find(fo);
            
            // get the Test class template
            temp = JUnitSettings.getDefault().getClassTemplate();
            fo = Repository.getDefault().getDefaultFileSystem().findResource(temp);
            doTestTempl = DataObject.find(fo);
        }
        catch (DataObjectNotFoundException e) {
            String msg = NbBundle.getMessage(CreateTestAction.class, "MSG_template_not_found");
            msg += " (" + temp + ")";
            NotifyDescriptor descr = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            org.openide.DialogDisplayer.getDefault().notify(descr);
            return;
        }
        
        TestCreator.initialize();
        
        String msg;
        ProgressIndicator progress = new ProgressIndicator();
        progress.show();
        
        msg = NbBundle.getMessage(
                CreateTestAction.class,
                "MSG_StatusBar_CreateTest_Begin");                      //NOI18N
        progress.displayStatusText(msg);
        try {
            // go through all nodes
            for(int nodeIdx = 0; nodeIdx < nodes.length; nodeIdx++) {
                if (!hasParentAmongNodes(nodes, nodeIdx)) {
                    if (null != (fo = TestUtil.getFileObjectFromNode(nodes[nodeIdx])))
                        createTest(fsTest, fo, doTestTempl, doSuiteTempl, null, progress);
                    else {
                        // @@ log - the node has no file associated
                        // System.out.println("@@ log - the node has no file associated");
                    }
                }
            }
            msg = NbBundle.getMessage(
                    CreateTestAction.class,
                    "MSG_StatusBar_CreateTests_Finished");              //NOI18N
            progress.displayStatusText(msg);
        }
        catch (CreateTestCanceledException e) {
            // tests creation has been canceled by the user
            msg = NbBundle.getMessage(
                    CreateTestAction.class,
                    "MSG_StatusBar_CreateTests_Cancelled");             //NOI18N
            progress.displayStatusText(msg);
        }
        finally {
            progress.hide();
        }
    }
    
    /* private members */
    private final int NODETYPE_UNKNOWN  = 0;
    private final int NODETYPE_CLASS    = 1;
    private final int NODETYPE_PACKAGE  = 2;
    
    private class CreateTestCanceledException extends Exception {}
    
    private void createSuiteTest(FileSystem fsTest,
                                 DataFolder folder,
                                 LinkedList suite,
                                 DataObject doSuiteT,
                                 LinkedList parentSuite,
                                 ProgressIndicator progress)
    {
        ClassElement[]      classTargets;
        DataObject          doTarget;
        FileObject          fo;
        
        try {
            fo = folder.getPrimaryFile();
            // find the suite class, if it exists or create one from active template
            doTarget = getTestClass(fsTest, TestUtil.getTestSuiteFullName(fo), doSuiteT);
            // generate the test suite for all listed test classes
            classTargets = TestUtil.getAllClassElementsFromDataObject(doTarget);
            
            for (int i=0; i < classTargets.length; i++) {
                ClassElement classTarget = classTargets[i];
                progress.setMessage(getCreatingMsg(classTarget.getName().getFullName()), false);
                
                TestCreator.createTestSuite(suite, fo.getPackageName('.'), classTarget);
                save(doTarget);
                
                // add the suite class to the list of members of the parent
                if (null != parentSuite) {
                    parentSuite.add(classTarget.getName().getFullName());
                }
            }
        }
        catch (Exception e) {
            // @@ log - the suite file creation failure
            // System.out.println("@@ log - the suite file creation failure");
        }
    }
    
    private void createTest(FileSystem fsTest,
                            FileObject foSource,
                            DataObject doTestT,
                            DataObject doSuiteT,
                            LinkedList parentSuite,
                            ProgressIndicator progress)
        throws CreateTestCanceledException
    {
        if (foSource.isFolder()) {
            // recurse of subfolders
            FileObject  childs[] = foSource.getChildren();
            LinkedList  mySuite = new LinkedList();
            
            progress.setMessage(getScanningMsg(foSource.getName()), false);
            for( int i = 0; i < childs.length; i++) {
                boolean recurse;
                
                if (childs[i].isFolder())
                    recurse = true;
                else {
                    String ext = childs[i].getNameExt().substring(childs[i].getNameExt().lastIndexOf('.') + 1);
                    recurse = ext.equals("java");
                }
                
                if (recurse) {
                    createTest(fsTest, childs[i], doTestT, doSuiteT, mySuite, progress);
                }
            }
            
            if ((0 < mySuite.size())&(JUnitSettings.getDefault().isGenerateSuiteClasses())) {
                createSuiteTest(fsTest, DataFolder.findFolder(foSource), mySuite, doSuiteT, parentSuite, progress);
            }
        }
        else {
            ClassElement[]  classSources;
            ClassElement    classTarget;
            DataObject      doTarget;
            String          name;
            
            try {
                classSources = TestUtil.getAllClassElementsFromDataObject(DataObject.find(foSource));
                for (int i=0; i < classSources.length; i++) {
                    ClassElement classSource = classSources[i];
                    if (null != classSource) {
                        if (TestCreator.isClassTestable(classSource)) {
                            // find the test class, if it exists or create one from active template
                            doTarget = getTestClass(fsTest, TestUtil.getTestClassFullName(classSource), doTestT);
                            
                            // generate the test of current node
                            classTarget = TestUtil.getClassElementFromDataObject(doTarget);
                            
                            progress.setMessage(getCreatingMsg(classTarget.getName().getFullName()), false);
                            
                            TestCreator.createTestClass(classSource, classTarget);
                            save(doTarget);
                            
                            name = classTarget.getName().getFullName();
                            // add the test class to the parent's suite
                            if (null != parentSuite) {
                                parentSuite.add(name);
                            }
                        }
                        else
                            progress.setMessage(getIgnoringMsg(classSource.getName().getFullName()), false);
                    }
                    else {
                        // @@ log - the tested class file can't be parsed
                        // System.out.println("@@ log - the tested class file can't be parsed.");
                    }
                }
            }
            catch (Exception e) {
                // @@ log - the test file creation failure
                // System.out.println("@@ log - the test file creation failure");
                e.printStackTrace();
            }
        }
        
        if (progress.isCanceled())
            throw new CreateTestCanceledException();
    }
    
    
    
    private DataObject getTestClass(FileSystem fsTest, String testClassName, DataObject doTemplate) throws IOException, DataObjectNotFoundException {
        FileObject      fo;
        DataObject      doTarget = null;
        
        if (null != (fo = fsTest.findResource(testClassName))) {
            // target class already exists, get reference
            doTarget = DataObject.find(fo);
        }
        else {
            // create target class from the template
            String  name;
            File    f = new File(testClassName);
            
            name = f.getName();
            if (null != f.getParent())
                fo = FileUtil.createFolder(fsTest.getRoot(), f.getParent().replace('\\', '/'));
            else
                fo = fsTest.getRoot();
            
            // create the name as a correct name of class
            name = name.substring(0, name.lastIndexOf("."));
            doTarget = doTemplate.createFromTemplate(DataFolder.findFolder(fo), name);
        }
        
        return doTarget;
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
