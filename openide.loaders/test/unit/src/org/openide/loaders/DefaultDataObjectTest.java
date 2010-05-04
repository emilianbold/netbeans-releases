/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.openide.loaders;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.Level;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.text.DataEditorSupport;
import org.openide.util.Enumerations;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class DefaultDataObjectTest extends NbTestCase {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    private FileSystem lfs;
    private DataObject obj;
    
    public DefaultDataObjectTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 15000;
    }
    
    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(Pool.class);
        
        clearWorkDir();
        JspLoader.cnt = 0;

        String fsstruct [] = new String [] {
            "AA/a.test"
        };
        

        lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);
        Repository.getDefault().addFileSystem(lfs);

        FileObject fo = lfs.findResource("AA/a.test");
        assertNotNull("file not found", fo);
        obj = DataObject.find(fo);

        assertEquals("The right class", obj.getClass(), DefaultDataObject.class);

        assertFalse("Designed to run outside of AWT", SwingUtilities.isEventDispatchThread());

        JspLoader.nodeListener = null;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRenameName() throws Exception {
        Node node = obj.getNodeDelegate();
        
        class L extends NodeAdapter implements Runnable, VetoableChangeListener {
            StyledDocument doc;
            @Override
            public void nodeDestroyed(NodeEvent ev) {
                assertEquals(1, JspLoader.cnt);
                try {
                    DataObject nobj = DataObject.find(obj.getPrimaryFile());
                    assertEquals(JspLoader.class, nobj.getLoader().getClass());
                    EditorCookie ec = nobj.getLookup().lookup(EditorCookie.class);
                    assertNotNull("Cookie found", ec);
                    doc =ec.openDocument();
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            public void run() {
                try {
                    obj.rename("x.jsp");
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            }
        }
        L listener = new L();
//        node.addNodeListener(listener);
        JspLoader.nodeListener = listener;
        obj.addVetoableChangeListener(listener);
        
        SwingUtilities.invokeAndWait(listener);
        assertEquals("One object created", 1, JspLoader.cnt);
        
        DataObject nobj = DataObject.find(obj.getPrimaryFile());
        assertEquals(JspLoader.class, nobj.getLoader().getClass());
        
        assertFalse("Invalidated", obj.isValid());
        
        assertNotNull("Document can be created", listener.doc);
    }

    @RandomlyFails // #181230
    public void testRenameOpenComponent() throws Exception {
        {
            OpenCookie oc = obj.getLookup().lookup(OpenCookie.class);
            assertNotNull("We have open cookie", oc);
            oc.open();
        }

        waitEQ();
        EditorCookie ec = obj.getLookup().lookup(EditorCookie.class);
        JEditorPane[] arr = getEPanes(ec);
        assertNotNull("Editor is open", arr);
        assertEquals("One Editor is open", 1, arr.length);

        Node[] origNodes = obj.getFolder().getNodeDelegate().getChildren().getNodes();
        assertEquals("One node", 1, origNodes.length);
        assertEquals("the obj", obj, origNodes[0].getLookup().lookup(DataObject.class));

        obj.rename("ToSomeStrangeName.jsp");
        assertFalse("Invalid now", obj.isValid());

        DataObject newObj = DataObject.find(obj.getPrimaryFile());
        if (newObj == obj) {
            fail("They should be different now: " + obj + ", " + newObj);
        }

        {
            OpenCookie oc = newObj.getLookup().lookup(OpenCookie.class);
            assertNotNull("We have open cookie", oc);
            oc.open();
        }
        ec = newObj.getLookup().lookup(EditorCookie.class);
        JEditorPane[] arr2 = getEPanes(ec);
        assertNotNull("Editor is open", arr2);
        assertEquals("One Editor is open", 1, arr2.length);

        Node[] newNodes = obj.getFolder().getNodeDelegate().getChildren().getNodes();
        assertEquals("One new node", 1, newNodes.length);
        assertEquals("the new obj.\nOld nodes: " + Arrays.toString(origNodes) + "\nNew nodes: " + Arrays.toString(newNodes),
            newObj, newNodes[0].getLookup().lookup(DataObject.class)
        );
    }

    private JEditorPane[] getEPanes(final EditorCookie ec) throws Exception {
        class R implements Runnable {
            JEditorPane[] arr;
            public void run() {
                arr = ec == null ? null : ec.getOpenedPanes();
            }
        }
        R r = new R();
        SwingUtilities.invokeAndWait(r);
        return r.arr;
    }

    private void waitEQ() throws Exception {
        getEPanes(null);
    }

    public static final class Pool extends DataLoaderPool {

        @Override
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(JspLoader.getLoader(JspLoader.class));
        }
        
    }

    public static final class JspLoader extends UniFileLoader {
        
        static int cnt; 
        static NodeListener nodeListener;
        
        public JspLoader() {
            super(MultiDataObject.class.getName());
        }

        @Override
        protected void initialize() {
            super.initialize();
            
            getExtensions().addExtension("jsp");
        }
        
        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            MultiDataObject obj = new MultiDataObject(primaryFile, this);
            cnt++;
            obj.getCookieSet().assign(EditorCookie.class, DataEditorSupport.create(obj, obj.getPrimaryEntry(), obj.getCookieSet()));

            if (nodeListener != null) {
                nodeListener.nodeDestroyed(null);
            }
            
            return obj;
        }
        
    }
        }
