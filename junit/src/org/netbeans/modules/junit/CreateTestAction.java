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
import org.openide.cookies.*;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

/** Action sensitive to some cookie that does something useful.
 *
 * @author  vstejskal
 * @version 1.0
 */
public class CreateTestAction extends CookieAction {

    /* public members */
    public String getName () {
        return NbBundle.getMessage (CreateTestAction.class, "LBL_Action_CreateTest");
    }

    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx (CreateTestAction.class);
    }

    /* protected members */
    protected Class[] cookieClasses () {
        return new Class[] { DataFolder.class, DataObject.class, ClassElement.class };
    }

    /** Perform special enablement check in addition to the normal one.
    protected boolean enable (Node[] nodes) {
	if (! super.enable (nodes)) return false;
	if (...) ...;
    }
    */

    /** Perform extra initialization of this action's singleton.
     * PLEASE do not use constructors for this purpose!
    protected void initialize () {
	super.initialize ();
        putProperty (Action.SHORT_DESCRIPTION, NbBundle.getMessage (CreateTestAction.class, "HINT_Action"));
    }
    */

    protected String iconResource () {
        return "CreateTestActionIcon.gif";
    }

    protected int mode () {
        return MODE_ANY;    // allow creation of tests for multiple selected nodes (classes, packages)
    }

    protected void performAction (Node[] nodes) {
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
        System.out.println("~~~" + temp);
        if (null == (fsTest = TopManager.getDefault().getRepository().findFileSystem(temp))) {
            String msg = NbBundle.getMessage(CreateTestAction.class, "MSG_file_system_not_found");
            NotifyDescriptor descr = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(descr);
            return;
        }
        
        try {
            // get the Suite class template
            temp = JUnitSettings.getDefault().getSuiteTemplate();
            fo = TopManager.getDefault().getRepository().getDefaultFileSystem().findResource(temp);
            doSuiteTempl = DataObject.find(fo);

            // get the Test class template
            temp = JUnitSettings.getDefault().getClassTemplate();
            fo = TopManager.getDefault().getRepository().getDefaultFileSystem().findResource(temp);
            doTestTempl = DataObject.find(fo);
        }
        catch (DataObjectNotFoundException e) {
            String msg = NbBundle.getMessage(CreateTestAction.class, "MSG_template_not_found");
            msg += " (" + temp + ")";
            NotifyDescriptor descr = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(descr);
            return;
        }

        TestCreator.initialize();
        progress.showMe();

        try {
            // go through all nodes
            for(int nodeIdx = 0; nodeIdx < nodes.length; nodeIdx++) {
                if (!hasParentAmongNodes(nodes, nodeIdx)) {
                    if (null != (fo = TestUtil.getFileObjectFromNode(nodes[nodeIdx])))
                        createTest(fsTest, fo, doTestTempl, doSuiteTempl, null); 
                    else {
                        // @@ log - the node has no file associated
                        System.out.println("@@ log - the node has no file associated");
                    }
                }
            }
        }
        catch (CreateTestCanceledException e) {
            // tests creation has been canceled by the user
        }
        finally {
            progress.hideMe();
        }
    }
    
    /* private members */
    private final int NODETYPE_UNKNOWN  = 0;
    private final int NODETYPE_CLASS    = 1;
    private final int NODETYPE_PACKAGE  = 2;
    private static final String msgCreating = NbBundle.getMessage(CreateTestAction.class, "LBL_generator_status_creating");
    private static final String msgScanning = NbBundle.getMessage(CreateTestAction.class, "LBL_generator_status_scanning");

    private JUnitProgress progress = new JUnitProgress();
    private class CreateTestCanceledException extends Exception {}
    
    private void createSuitTest(FileSystem fsTest, DataFolder folder, LinkedList suite, DataObject doSuitT, LinkedList parentSuite) {
        ClassElement        classTarget;
        DataObject          doTarget;
        FileObject          fo;

        try {
            fo = folder.getPrimaryFile();
            // find the suite class, if it exists or create one from active template
            doTarget = getTestClass(fsTest, TestUtil.getTestSuitFullName(fo), doSuitT);

            // generate the test suite for all listed test classes
            classTarget = getClassElementFromDO(doTarget);

            progress.setMessage(msgCreating + classTarget.getName().getFullName() + " ...");

            TestCreator.createTestSuit(suite, fo.getPackageName('.'), classTarget);
            save(doTarget);
            
            // add the suite class to the list of members of the parent
            if (null != parentSuite)
                parentSuite.add(classTarget.getName().getFullName());
        } 
        catch (Exception e) {
            e.printStackTrace();
            // @@ log - the suite file creation failure
            System.out.println("@@ log - the suite file creation failure");
        }
    }

    private void createTest(FileSystem fsTest, FileObject foSource, DataObject doTestT, DataObject doSuitT, LinkedList parentSuite) throws CreateTestCanceledException {
        if (foSource.isFolder()) {
            // recurse of subfolders
            FileObject  childs[] = foSource.getChildren();
            LinkedList  mySuite = new LinkedList();
            
            progress.setMessage(msgScanning + foSource.getName() + " ...");
            for( int i = 0; i < childs.length; i++) {
                boolean recurse;
                
                if (childs[i].isFolder())
                    recurse = true;
                else {
                    String ext = childs[i].getNameExt().substring(childs[i].getNameExt().lastIndexOf('.') + 1);
                    recurse = ext.equals("java");
                }
                
                if (recurse) {
                    createTest(fsTest, childs[i], doTestT, doSuitT, mySuite);
                }
            }
            
            if (0 < mySuite.size()) 
                createSuitTest(fsTest, DataFolder.findFolder(foSource), mySuite, doSuitT, parentSuite);
        }
        else {
            ClassElement    classSource;
            ClassElement    classTarget;
            DataObject      doTarget;
            String          name;
            
            try {
                classSource = getClassElementFromDO(DataObject.find(foSource));
                if (null != classSource) {
                    // find the test class, if it exists or create one from active template
                    doTarget = getTestClass(fsTest, TestUtil.getTestClassFullName(foSource), doTestT);

                    // generate the test of current node
                    classTarget = getClassElementFromDO(doTarget);

                    progress.setMessage(msgCreating + classTarget.getName().getFullName() + " ...");

                    TestCreator.createTestClass(classSource, classTarget);
                    save(doTarget);

                    name = classTarget.getName().getFullName();
                    // add the test class to the parent's suite
                    if (null != parentSuite) {
                        parentSuite.add(name);
                    }
                }
                else {
                    // @@ log - the tested class file can't be parsed or contains only abstract or non-public classes
                    System.out.println("@@ log - the tested class file can't be parsed or contains only abstract or non-public classes");
                }
            } 
            catch (Exception e) {
                e.printStackTrace();
                // @@ log - the test file creation failure
                System.out.println("@@ log - the test file creation failure");
            }
        }
        
        if (progress.isCanceled())
            throw new CreateTestCanceledException();
    }
    
    private ClassElement getClassElementFromDO(DataObject dO) {
        SourceCookie    sc;
        SourceElement   se;
        ClassElement    ce;
        ClassElement[]  allClasses;

        sc = (SourceCookie) dO.getCookie(SourceCookie.class);
        se = sc.getSource();
        allClasses = se.getClasses();

        for (int j = 0; j < allClasses.length; j++) {
            ce = allClasses[j];
            if (null != ce && ce.isClass() && 
                (0 != (ce.getModifiers() & Modifier.PUBLIC)) &&
                (0 == (ce.getModifiers() & Modifier.ABSTRACT))) {
                    return ce;
            }
        }
        return null;
    }
    
    private ClassElement getClassElementCookie(DataObject doTarget, String name) {
        return (ClassElement) doTarget.getNodeDelegate().getChildren().findChild(name).getCookie(ClassElement.class);
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
}
