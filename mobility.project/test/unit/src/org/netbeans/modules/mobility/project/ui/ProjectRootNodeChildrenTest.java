/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mobility.project.ui;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.mobility.project.ChildKind;
import org.netbeans.api.mobility.project.ProjectChildKeyProvider;
import org.netbeans.api.project.Project;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.util.ChangeSupport;
import org.openide.util.lookup.Lookups;
import static org.junit.Assert.*;
/**
 *
 * @author tim
 */
public class ProjectRootNodeChildrenTest {

    private static File ud;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ProjectRootNodeChildren.LOGGER.setLevel(Level.ALL);
        ud = new File (new File(System.getProperty("java.io.tmpdir")), ProjectRootNodeChildrenTest.class.getName() + System.currentTimeMillis());
        assertTrue (ud.mkdirs());
        System.setProperty("netbeans.user", ud.getAbsolutePath());
        MockServices.setServices(PCKP.class);
        assertTrue(true);
        FileObject fld = FileUtil.getConfigFile(ProjectRootNodeChildren.FOREIGN_NODES_PATH);
        if (fld == null) {
            fld = FileUtil.createFolder(FileUtil.getConfigRoot(), ProjectRootNodeChildren.FOREIGN_NODES_PATH);
        }
        String nm = NF.class.getName();
        nm = nm.replace('.', '-') + ".instance";
        FileObject nue = fld.createData(nm);
        nfinstance = null;
        nlinstance = null;
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (ud.exists()) {
            FileObject fo = FileUtil.toFileObject(ud);
            fo.delete();
        }
    }

    @Test
    public void testChildrenRefreshOnChange() throws Exception {
        final Object lock = new Object();
        ProjectRootNodeChildren.LOGGER.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().startsWith("Foreign nodes created for J2ME Project")) {
                    synchronized(lock) {
                        lock.notifyAll();
                    }
                }
            }

            @Override
            public void flush() {
                //do nothing
            }

            @Override
            public void close() throws SecurityException {
                //do nothing
            }

        });
        ProjectRootNodeChildren cf = new ProjectRootNodeChildren(null);
        Node root = new AbstractNode(Children.create(cf, false));
        NodeListener nl = new NodeAdapter() {
            volatile boolean notified;
            void waitFor() throws InterruptedException {
                boolean old = notified;
                notified = false;
                if (!old) {
                    synchronized(this) {
                        wait();
                    }
                }
            }

            @Override
            public void childrenAdded(NodeMemberEvent ev) {
                synchronized (this) {
                    notifyAll();
                }
            }

            @Override
            public void propertyChange(PropertyChangeEvent ev) {
            }
        };
        root.addNodeListener(nl);
        Pnl pnl = new Pnl(root);
        TML l = new TML();
        pnl.mdl().addTreeModelListener(l);
        pnl.showFrame();
        Children kids = root.getChildren();
        Node[] n = kids.getNodes(true);
        assertEquals (4, n.length);
        for (int i = 0; i < n.length; i++) {
            assertEquals (i + "", n[i].getName());
        }
        l.notified = false;
        nlinstance.addChild();
        synchronized(lock) {
            lock.wait();
        }
        l.waitFor();

        n = kids.getNodes(true);
        assertEquals(5, n.length);
    }

    public static final class PCKP implements ProjectChildKeyProvider {
        public Collection<? extends ChildKind> getKeys() {
            return Collections.singleton(ChildKind.Foreign);
        }
    }

    static final class TML implements TreeModelListener {
            volatile boolean notified;
            public void treeNodesChanged(TreeModelEvent e) {
            }

            void waitFor() throws InterruptedException {
                boolean old = notified;
                notified = false;
                if (!old) {
                    synchronized(this) {
                        wait();
                    }
                }
            }

            public void treeNodesInserted(TreeModelEvent e) {
                notified = true;
                    synchronized(this) {
                        notifyAll();
                    }
            }

            public void treeNodesRemoved(TreeModelEvent e) {
            }

            public void treeStructureChanged(TreeModelEvent e) {
            }
    }

    static NF nfinstance;
    public static final class NF implements NodeFactory {
        NL nl = new NL();
        public NF() {
            nfinstance = this;
        }
        public NodeList<?> createNodes(Project p) {
            return nl;
        }
    }

    static NL nlinstance;
    public static final class NL implements NodeList<Integer> {
        final List<Integer> l = new ArrayList<Integer>();
        private final ChangeSupport supp = new ChangeSupport(this);
        private boolean active;
        private boolean keysCalled;
        public NL() {
//            assertNull (nlinstance);
            nlinstance = this;
            l.add(0);
            l.add(1);
            l.add(2);
            l.add(3);
        }

        public void addChild() {
            int val = l.get(l.size() - 1) + 1;
            l.add(val);
            supp.fireChange();
        }

        public void addChangeListener(ChangeListener l) {
            supp.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            supp.removeChangeListener(l);
        }

        public void assertActive() {
            assertTrue(active);
        }

        public void assertInactive() {
            assertFalse(active);
        }

        public void assertKeysCalled() {
            boolean old = keysCalled;
            keysCalled = false;
            assertTrue(old);
        }

        public void assertCreatedNodeFor(int val) {
            assertTrue("No node created for " + val +": " + nodeCalled, nodeCalled.contains(new Integer(val)));
            nodeCalled.remove(val);
        }

        public void addNotify() {
            active = true;
        }

        public void removeNotify() {
            active = false;
        }

        public List<Integer> keys() {
            keysCalled = true;
            return new ArrayList<Integer>(l);
        }

        private Set<Integer> nodeCalled = new HashSet<Integer>();
        public Node node(Integer key) {
            nodeCalled.add(key);
            AbstractNode node = new AbstractNode(Children.LEAF, Lookups.singleton(key));
            node.setName(key.toString());
            node.setDisplayName(node.getName());
            return node;
        }
    }

    private static final class Pnl extends JPanel implements ExplorerManager.Provider, WindowListener {
        private final ExplorerManager mgr = new ExplorerManager();
        private final BeanTreeView btv = new BeanTreeView();
        Pnl(Node n) {
            setLayout(new BorderLayout());
            btv.setRootVisible(false);
            add (btv, BorderLayout.CENTER);
            mgr.setRootContext(n);
        }

        TreeModel mdl() {
            JTree tree = (JTree) btv.getViewport().getView();
            return tree.getModel();
        }

        public ExplorerManager getExplorerManager() {
            return mgr;
        }

        void showFrame() {
            JFrame jf = new JFrame();
            jf.getContentPane().setLayout(new BorderLayout());
            jf.getContentPane().add(this, BorderLayout.CENTER);
            jf.pack();
            jf.addWindowListener(this);
            jf.setVisible(true);
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    throw new Error(ex);
                }
            }
        }

        public void windowOpened(WindowEvent e) {
            synchronized(this) {
                notifyAll();
            }
        }

        public void windowClosing(WindowEvent e) {

        }

        public void windowClosed(WindowEvent e) {

        }

        public void windowIconified(WindowEvent e) {

        }

        public void windowDeiconified(WindowEvent e) {

        }

        public void windowActivated(WindowEvent e) {

        }

        public void windowDeactivated(WindowEvent e) {
            
        }
    }
}