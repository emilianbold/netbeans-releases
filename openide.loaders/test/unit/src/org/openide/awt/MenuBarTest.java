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

package org.openide.awt;

import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import junit.framework.*;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.LoggingTestCaseHid.ErrManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallbackSystemAction;

/**
 *
 * @author Jaroslav Tulach
 */
public class MenuBarTest extends LoggingTestCaseHid implements ContainerListener {
    private DataFolder df;
    private MenuBar mb;
    
    private int add;
    private int remove;
    
    public MenuBarTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        FileObject fo = FileUtil.createFolder(
            Repository.getDefault().getDefaultFileSystem().getRoot(),
            "Folder" + getName()
        );
        df = DataFolder.findFolder(fo);
        mb = new MenuBar(df);
        mb.waitFinished();
    }

    protected void tearDown() throws Exception {
    }

    public void testHowManyRepaintsPerOneChangeAreThere() throws Exception {
        mb.addContainerListener(this);
        assertEquals("No children now", 0, mb.getComponentCount());
        
        class Atom implements FileSystem.AtomicAction {
            FileObject m1, m2;
            
            public void run() throws IOException {
                m1 = FileUtil.createFolder(df.getPrimaryFile(), "m1");
                m2 = FileUtil.createFolder(df.getPrimaryFile(), "m2");
            }
        }
        Atom atom = new Atom();
        df.getPrimaryFile().getFileSystem().runAtomicAction(atom);
        mb.waitFinished();
        
        assertEquals("Two children there", 2, mb.getComponentCount());
        
        assertEquals("No removals", 0, remove);
        assertEquals("Two additions", 2, add);
        
        DataFolder f1 = DataFolder.findFolder(atom.m1);
        InstanceDataObject.create(f1, "Kuk", OpenAction.class);
        mb.waitFinished();
        
        assertEquals("Two children there", 2, mb.getComponentCount());
        Object o1 = mb.getComponent(0);
        if (!(o1 instanceof JMenu)) {
            fail("It has to be menu: " + o1);
        }
        JMenu m1 = (JMenu)o1;
        // simulate expansion in the menu
        m1.setSelected(true);
        java.awt.Component[] content = m1.getPopupMenu().getComponents();
        assertEquals("Now it has one child", 1, content.length);
        
        assertEquals("Still No removals in MenuBar", 0, remove);
        assertEquals("Still Two additions in MenuBar", 2, add);
    }

    
    public void testMenusAreResolvedLazilyUntilTheyAreReallyNeeded() throws Exception {
        mb.addContainerListener(this);
        assertEquals("No children now", 0, mb.getComponentCount());
        
        class Atom implements FileSystem.AtomicAction {
            FileObject m1, m2;
            
            public void run() throws IOException {
                m1 = FileUtil.createFolder(df.getPrimaryFile(), "m1");
                DataFolder f1 = DataFolder.findFolder(m1);
                InstanceDataObject.create(f1, "X", MyAction.class);
            }
        }
        Atom atom = new Atom();
        df.getPrimaryFile().getFileSystem().runAtomicAction(atom);
        mb.waitFinished();
        
        assertEquals("One submenu is there", 1, mb.getComponentCount());
        
        assertEquals("No removals", 0, remove);
        assertEquals("One addition", 1, add);
        
        Object o1 = mb.getComponent(0);
        if (!(o1 instanceof JMenu)) {
            fail("It has to be menu: " + o1);
        }
        JMenu m1 = (JMenu)o1;
        
        assertEquals("We have the menu, but the content is still not computed", 0, MyAction.counter);
        
        // simulate expansion in the menu
        m1.setSelected(true);
        java.awt.Component[] content = m1.getPopupMenu().getComponents();
        assertEquals("Now it has one child", 1, content.length);
        
        assertEquals("Still No removals in MenuBar", 0, remove);
        assertEquals("Still one addition in MenuBar", 1, add);
        
        assertEquals("And now the action is created", 1, MyAction.counter);
    }
    
    public void testSurviveInvalidationOfAFolder() throws Exception {
        
        FileObject m1 = FileUtil.createFolder(df.getPrimaryFile(), "m1");
        final DataFolder f1 = DataFolder.findFolder(m1);

        mb.waitFinished();

        JMenu menu;
        {
            Object o1 = mb.getComponent(0);
            if (!(o1 instanceof JMenu)) {
                fail("It has to be menu: " + o1);
            }
            menu = (JMenu)o1;
            assertEquals("simple name ", "m1", menu.getText());
        }
        
        Node n = f1.getNodeDelegate();
        f1.setValid(false);
        mb.waitFinished();
        
        n.setName("othername");

        mb.waitFinished();
        
        assertEquals("updated the folder is deleted now", f1.getName(), menu.getText());
        
        
        
        if (ErrManager.messages.indexOf("fix your code") >= 0) {
            fail("There were warnings about the use of invalid nodes");
        }
    }
    
    public void componentAdded(ContainerEvent e) {
        add++;
    }

    public void componentRemoved(ContainerEvent e) {
        remove++;
    }
    
    public static final class MyAction extends CallbackSystemAction {
        public static int counter;
        
        public MyAction() {
            counter++;
        }

        public String getName() {
            return "MyAction";
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        
        
    }
}
