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
 * Portions Copyrighted 2007 Nokia Siemens Networks Oy
 */
package org.netbeans.core.windows.view.ui;

import java.awt.*;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import junit.framework.*;
import org.netbeans.core.windows.IDEInitializer;
import org.netbeans.junit.*;
import org.openide.windows.*;
import org.openide.awt.ToolbarPool;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;


/**
 * Tests whether the system properties <code>
 * netbeans.winsys.menu_bar.path, netbeans.winsys.status_line.path
 * </code> and <code>netbeans.winsys.no_toolbars</code> really do
 * something.
 * 
 * @author David Strupl
 */
public class CustomMenuBarTest extends NbTestCase {

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.winsys.menu_bar.path", "LookAndFeel/MenuBar.instance");
        System.setProperty("netbeans.winsys.status_line.path", "LookAndFeel/StatusLine.instance");
        System.setProperty("netbeans.winsys.no_toolbars", "true");
    }

    @Override
    protected void tearDown() throws Exception {
        System.getProperties().remove("netbeans.winsys.menu_bar.path");
        System.getProperties().remove("netbeans.winsys.status_line.path");
        System.getProperties().remove("netbeans.winsys.no_toolbars");
    }

    private static JMenuBar myMenuBar;
    private static JComponent myStatusLine;

    /** Creates a new instance of SFSTest */
    public CustomMenuBarTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }


    public void testAlternativeMenuBar() throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);

        IDEInitializer.addLayers(new String[]{"org/netbeans/core/windows/resources/layer-CustomMenuBarTest.xml"});

        //Verify that test layer was added to default filesystem
        assertNotNull(Repository.getDefault().getDefaultFileSystem().findResource("LookAndFeel/MenuBar.instance"));

        MainWindow mw = (MainWindow) WindowManager.getDefault().getMainWindow();
        mw.initializeComponents();
        assertEquals(mw.getJMenuBar(), createMenuBar());
        IDEInitializer.removeLayers();
    }

    public void testAlternativeStatusLine() throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);

        IDEInitializer.addLayers(new String[]{"org/netbeans/core/windows/resources/layer-CustomMenuBarTest.xml"});

        //Verify that test layer was added to default filesystem
        assertNotNull(Repository.getDefault().getDefaultFileSystem().findResource("LookAndFeel/StatusLine.instance"));
        MainWindow mw = (MainWindow) WindowManager.getDefault().getMainWindow();
        mw.initializeComponents();
        assertTrue(findComponent(mw, createStatusLine()));

        IDEInitializer.removeLayers();
    }

    public void testNoToolbar() throws Exception {
        MainWindow mw = (MainWindow) WindowManager.getDefault().getMainWindow();
        mw.initializeComponents();
        ToolbarPool tp = ToolbarPool.getDefault();
        assertTrue(!findComponent(mw, tp));
    }

    private static boolean findComponent(Container cont, Component comp) {
        if (cont == null || comp == null) {
            return false;
        }
        if (cont.equals(comp)) {
            return true;
        }
        Component[] children = cont.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i].equals(comp)) {
                return true;
            }
            if (children[i] instanceof Container) {
                if (findComponent((Container) children[i], comp)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class MyMenuBar extends JMenuBar {
    }

    private static class MyStatus extends JComponent {
    }

    private static JMenuBar createMenuBar() {
        if (myMenuBar == null) {
            myMenuBar = new MyMenuBar();
        }
        return myMenuBar;
    }

    private static JComponent createStatusLine() {
        if (myStatusLine == null) {
            myStatusLine = new MyStatus();
        }
        return myStatusLine;
    }
}
