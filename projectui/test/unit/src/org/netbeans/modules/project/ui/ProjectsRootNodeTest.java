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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.ui;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.Status;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class ProjectsRootNodeTest extends NbTestCase {
    
    public ProjectsRootNodeTest(String testName) {
        super(testName);
    }            

    public void testBehaviourOfProjectsLogicNode() throws Exception {
        MockLookup.setInstances(new TestSupport.TestProjectFactory());
        CountDownLatch down = new CountDownLatch(1);
        List<URL> list = new ArrayList<URL>();
        List<ExtIcon> icons = new ArrayList<ExtIcon>();
        List<String> names = new ArrayList<String>();
        clearWorkDir();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        assertNotNull(workDir);
        for (int i = 0; i < 30; i++) {
            FileObject prj = TestSupport.createTestProject(workDir, "prj" + i);
            URL url = URLMapper.findURL(prj, URLMapper.EXTERNAL);
            list.add(url);
            names.add(url.toExternalForm());
            icons.add(new ExtIcon());
            TestSupport.TestProject tmp = (TestSupport.TestProject) ProjectManager.getDefault().findProject(prj);
            assertNotNull("Project found", tmp);
            tmp.setLookup(Lookups.singleton(new TestProjectOpenedHookImpl(down)));
        }

        OpenProjectListSettings.getInstance().setOpenProjectsURLs(list);
        OpenProjectListSettings.getInstance().setOpenProjectsDisplayNames(names);
        OpenProjectListSettings.getInstance().setOpenProjectsIcons(icons);

        Node logicalView = new ProjectsRootNode(ProjectsRootNode.LOGICAL_VIEW);
        L listener = new L();
        logicalView.addNodeListener(listener);
        
        assertEquals("30 children", 30, logicalView.getChildren().getNodesCount());
        listener.assertEvents("None", 0);
        assertEquals("No project opened yet", 0, TestProjectOpenedHookImpl.opened);
        
        for (Node n : logicalView.getChildren().getNodes()) {
            TestSupport.TestProject p = n.getLookup().lookup(TestSupport.TestProject.class);
            assertNull("No project of this type, yet", p);
        }
        
        // let project open code run
        down.countDown();
        TestProjectOpenedHookImpl.toOpen.await();
        
        assertEquals("All projects opened", 30, TestProjectOpenedHookImpl.opened);
        
        OpenProjectList.waitProjectsFullyOpen();

        for (Node n : logicalView.getChildren().getNodes()) {
            TestSupport.TestProject p = n.getLookup().lookup(TestSupport.TestProject.class);
            assertNotNull("Nodes have correct project of this type", p);
        }
        
        listener.assertEvents("Goal is to receive no events at all", 0);
        assertTrue("Finished", OpenProjects.getDefault().openProjects().isDone());
        assertFalse("Not cancelled, Finished", OpenProjects.getDefault().openProjects().isCancelled());
        Project[] arr = OpenProjects.getDefault().openProjects().get();
        assertEquals("30", 30, arr.length);
    }
    
    private static class L implements NodeListener {
        public List<EventObject> events = new ArrayList<EventObject>();
        
        public void childrenAdded(NodeMemberEvent ev) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(ev);
        }

        public void childrenRemoved(NodeMemberEvent ev) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(ev);
        }

        public void childrenReordered(NodeReorderEvent ev) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(ev);
        }

        public void nodeDestroyed(NodeEvent ev) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(ev);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(evt);
        }

        final void assertEvents(String string, int i) {
            assertEquals(string + events, i, events.size());
            events.clear();
        }
        
    }
    
    private static class TestProjectOpenedHookImpl extends ProjectOpenedHook 
    implements Runnable {
        
        public static CountDownLatch toOpen = new CountDownLatch(30);
        public static int opened = 0;
        public static int closed = 0;
        
        
        private CountDownLatch toWaitOn;
        
        public TestProjectOpenedHookImpl(CountDownLatch toWaitOn) {
            this.toWaitOn = toWaitOn;
        }
        
        protected void projectClosed() {
            closed++;
        }
        
        Project[] arr;
        public void run() {
            try {
                arr = OpenProjects.getDefault().openProjects().get(50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                fail("Wrong exception");
            } catch (ExecutionException ex) {
                fail("Wrong exception");
            } catch (TimeoutException ex) {
                // OK
            }
        }
        
        protected void projectOpened() {
            assertFalse("Running", OpenProjects.getDefault().openProjects().isDone());
            // now verify that other threads do not see results from the Future
            RequestProcessor.getDefault().post(this).waitFinished();
            assertNull("TimeoutException thrown", arr);
            if (toWaitOn != null) {
                try {
                    toWaitOn.await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            opened++;
            toOpen.countDown();
        }
        
    }

    public void testBadging() throws Exception { // #135399
        class BadgedImage extends Image {
            final Image original;
            BadgedImage(Image original) {this.original = original;}
            public @Override int getWidth(ImageObserver observer) {return original.getWidth(observer);}
            public @Override int getHeight(ImageObserver observer) {return original.getHeight(observer);}
            public @Override ImageProducer getSource() {return original.getSource();}
            public @Override Graphics getGraphics() {return original.getGraphics();}
            public @Override Object getProperty(String name, ImageObserver observer) {return original.getProperty(name, observer);}
            public @Override void flush() {original.flush();}
        }
        class TestFS extends MultiFileSystem {
            String nameBadge = "";
            String htmlBadge = "";
            boolean badging;
            Set<? extends FileObject> badgedFiles;
            TestFS() throws Exception {
                super(new FileSystem[] {FileUtil.createMemoryFileSystem()});
            }
            public @Override Status getStatus() {
                return new HtmlStatus() {
                    public String annotateName(String name, Set<? extends FileObject> files) {
                        badgedFiles = files;
                        return name + nameBadge;
                    }
                    public String annotateNameHtml(String name, Set<? extends FileObject> files) {
                        badgedFiles = files;
                        return name + htmlBadge;
                    }
                    public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
                        badgedFiles = files;
                        return badging ? new BadgedImage(icon) : icon;
                    }
                };
            }
            void fireChange(boolean icon, boolean name, FileObject... files) {
                fireFileStatusChanged(new FileStatusEvent(this, new HashSet<FileObject>(Arrays.asList(files)), icon, name));
            }
        }
        TestFS fs = new TestFS();
        FileObject root = fs.getRoot();
        FileObject k1 = root.createData("hello");
        FileObject k2 = root.createFolder("there");
        MockLookup.setInstances(new ProjectFactory() {
            public boolean isProject(FileObject projectDirectory) {return projectDirectory.isRoot();}
            public Project loadProject(final FileObject projectDirectory, ProjectState state) throws IOException {
                if (projectDirectory.isRoot()) {
                    return new Project() {
                        public FileObject getProjectDirectory() {return projectDirectory;}
                        public Lookup getLookup() {return Lookup.EMPTY;}
                    };
                } else {
                    return null;
                }
            }
            public void saveProject(Project project) throws IOException, ClassCastException {}
        });
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        System.setProperty("test.nodelay", "true");
        ProjectsRootNode.BadgingNode node = new ProjectsRootNode.BadgingNode(null, new ProjectsRootNode.ProjectChildren.Pair(prj),
                new AbstractNode(Children.LEAF, Lookups.singleton(prj)) {
                    public @Override String getDisplayName() {return "Prj";}
                    public @Override String getHtmlDisplayName() {return "Prj";}
                }, false, true);
        final AtomicInteger nameChanges = new AtomicInteger();
        final AtomicInteger iconChanges = new AtomicInteger();
        node.addNodeListener(new NodeAdapter() {
            public @Override void propertyChange(PropertyChangeEvent ev) {
                String p = ev.getPropertyName();
                if (p.equals(Node.PROP_DISPLAY_NAME)) {
                    nameChanges.incrementAndGet();
                } else if (p.equals(Node.PROP_ICON)) {
                    iconChanges.incrementAndGet();
                }
            }
        });
        assertEquals("Prj", node.getDisplayName());
        assertEquals("Prj", node.getHtmlDisplayName());
        assertFalse(node.getIcon(BeanInfo.ICON_COLOR_16x16) instanceof BadgedImage);
        fs.nameBadge = " *";
        fs.htmlBadge = " <mod>";
        fs.fireChange(false, true, k1);
        assertNotNull(node.task);
        node.task.waitFinished();
        assertEquals(1, nameChanges.intValue());
        assertEquals(0, iconChanges.intValue());
        assertEquals("Prj *", node.getDisplayName());
        assertEquals("Prj <mod>", node.getHtmlDisplayName());
        assertFalse(node.getIcon(BeanInfo.ICON_COLOR_16x16) instanceof BadgedImage);
        assertEquals(new HashSet<FileObject>(Arrays.asList(k1, k2)), fs.badgedFiles);
        fs.badging = true;
        fs.fireChange(true, false, k2);
        node.task.waitFinished();
        assertEquals(1, nameChanges.intValue());
        assertEquals(1, iconChanges.intValue());
        assertEquals("Prj *", node.getDisplayName());
        assertEquals("Prj <mod>", node.getHtmlDisplayName());
        assertTrue(node.getIcon(BeanInfo.ICON_COLOR_16x16) instanceof BadgedImage);
        assertEquals(new HashSet<FileObject>(Arrays.asList(k1, k2)), fs.badgedFiles);
        FileObject k3 = root.createData("again");
        fs.nameBadge = " +";
        fs.fireChange(false, true, k3);
        node.task.waitFinished();
        assertEquals(2, nameChanges.intValue());
        assertEquals(1, iconChanges.intValue());
        assertEquals("Prj +", node.getDisplayName());
        assertEquals("Prj <mod>", node.getHtmlDisplayName());
        assertTrue(node.getIcon(BeanInfo.ICON_COLOR_16x16) instanceof BadgedImage);
        assertEquals(new HashSet<FileObject>(Arrays.asList(k1, k2, k3)), fs.badgedFiles);
    }

}
