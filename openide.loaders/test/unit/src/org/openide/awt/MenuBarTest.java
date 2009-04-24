/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.awt;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.actions.OpenAction;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.XMLFileSystem;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Jaroslav Tulach
 */
@RandomlyFails // Temporary solution
public class MenuBarTest extends NbTestCase implements ContainerListener {
    private DataFolder df;
    private MenuBar mb;
    
    private int add;
    private int remove;
    
    public MenuBarTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.WARNING;
    }
    
    @Override
    protected boolean runInEQ () {
        return true;
    }
    
    @Override
    protected void setUp() throws Exception {
        CreateOnlyOnceAction.instancesCount = 0;
        CreateOnlyOnceAction.w = new StringWriter();
        CreateOnlyOnceAction.pw = new PrintWriter(CreateOnlyOnceAction.w);

        MyAction.counter = 0;

        FileObject fo = FileUtil.createFolder(
            FileUtil.getConfigRoot(),
            "Folder" + getName()
        );
        df = DataFolder.findFolder(fo);
        mb = new MenuBar(df);
        mb.waitFinished();
    }

    @Override
    protected void tearDown() throws Exception {
    }
    
    public void testAllInstances() throws Exception {
        InstanceCookie[] ics = new InstanceCookie[] {
            new IC(false),
            new IC(true)
        };
        MenuBar.allInstances(ics, new ArrayList<Object>());
    }
    
    private static class IC implements InstanceCookie {
        private boolean throwing;
        IC(boolean throwing) {
            this.throwing = throwing;
        }
        public String instanceName() {
            return "dummy";
        }

        public Class<?> instanceClass() throws IOException, ClassNotFoundException {
            return Object.class;
        }

        public Object instanceCreate() throws IOException, ClassNotFoundException {
            if (throwing) {
                Exception e = new Exception("original");
                throw (IOException) new IOException("inited").initCause(e);
            }
            return new Object();
        }
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
        CharSequence seq = Log.enable("", Level.ALL);
        
        
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
        
        
        
        if (seq.toString().indexOf("fix your code") >= 0) {
            fail("There were warnings about the use of invalid nodes: " + seq);
        }
    }

    public void testActionIsCreatedOnlyOnce_13195() throws Exception {
        doActionIsCreatedOnlyOnce_13195("Menu");
    }

    public void testActionIsCreatedOnlyOnceWithNewValue() throws Exception {
        doActionIsCreatedOnlyOnce_13195("MenuWithNew");
    }

    public void testActionFactoryCanReturnNull() throws Exception {
        CharSequence log = Log.enable("", Level.WARNING);
        doActionIsCreatedOnlyOnce_13195("ReturnsNull");
        if (log.length() > 0) {
            fail("No warnings please:\n" + log);
        }
    }

    private void doActionIsCreatedOnlyOnce_13195(String name) throws Exception {
        // crate XML FS from data
        String[] stringLayers = new String [] { "/org/openide/awt/data/testActionOnlyOnce.xml" };
        URL[] layers = new URL[stringLayers.length];

        for (int cntr = 0; cntr < layers.length; cntr++) {
            layers[cntr] = Utilities.class.getResource(stringLayers[cntr]);
        }

        XMLFileSystem system = new XMLFileSystem();
        system.setXmlUrls(layers);

        // build menu
        DataFolder dataFolder = DataFolder.findFolder(system.findResource(name));
        MenuBar menuBar = new MenuBar(dataFolder);
        menuBar.waitFinished();

        if (CreateOnlyOnceAction.instancesCount != 1) {
            // ensure that only one instance of action was created
            fail("Action created only once, but was: " + CreateOnlyOnceAction.instancesCount + "\n" + CreateOnlyOnceAction.w);
        }
    }

    public void componentAdded(ContainerEvent e) {
        add++;
    }

    public void componentRemoved(ContainerEvent e) {
        remove++;
    }
    
    public static final class MyAction extends CallbackSystemAction
    implements Presenter.Menu, Presenter.Toolbar {
        public static int counter;
        
        public MyAction() {
            assertFalse("Not initialized in AWT thread", EventQueue.isDispatchThread());
            counter++;
        }

        public String getName() {
            return "MyAction";
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        public Component getToolbarPresenter() {
            assertTrue("Presenters created only in AWT", EventQueue.isDispatchThread());
            return super.getToolbarPresenter();
        }

        @Override
        public JMenuItem getMenuPresenter() {
            assertTrue("Presenters created only in AWT", EventQueue.isDispatchThread());
            return super.getMenuPresenter();
        }
    }

    public static class NullOnlyAction extends AbstractAction {
        private NullOnlyAction() {}

        public static synchronized NullOnlyAction getNull() {
            CreateOnlyOnceAction.instancesCount++;
            return null;
        }

        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static class CreateOnlyOnceAction extends AbstractAction {

        static int instancesCount = 0;
        static StringWriter w;
        static PrintWriter pw;

        public static synchronized CreateOnlyOnceAction create() {
            return new CreateOnlyOnceAction();
        }

        public void actionPerformed(ActionEvent e) {
            // no op
        }

        public CreateOnlyOnceAction() {
            new Exception("created for " + (++instancesCount) + " time").printStackTrace(pw);
            putValue(NAME, "TestAction");
            assertFalse("Not initialized in AWT thread", EventQueue.isDispatchThread());
        }

    }


}
