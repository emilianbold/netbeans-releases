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

package org.netbeans.core;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.loaders.Environment;
import org.openide.loaders.InstanceSupport;
import org.openide.loaders.XMLDataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Document;

public class NbKeymapTest extends NbTestCase {
    public NbKeymapTest(String name) {
        super(name);
    }

    static {
        System.setProperty("os.name", "Linux"); // just to standardize modifier key binding
    }

    protected @Override void setUp() throws Exception {
        for (FileObject f : FileUtil.getConfigRoot().getChildren()) {
            f.delete();
        }
    }
    
    private FileObject make(String path) throws IOException {
        return FileUtil.createData(FileUtil.getConfigRoot(), path);
    }

    private FileObject makeFolder(String path) throws IOException {
        return FileUtil.createFolder(FileUtil.getConfigRoot(), path);
    }

    private void assertMapping(NbKeymap km, KeyStroke stroke, FileObject presenterDefinition, String actionName) throws Exception {
        Action a = km.getAction(stroke);
        assertNotNull("for " + stroke, a);
        assertEquals(actionName, a.getValue(Action.NAME));
        assertEquals("for " + stroke + " from " + presenterDefinition.getPath(),
                stroke, km.keyStrokeForAction(a, presenterDefinition));
    }
    
    public void testAcceleratorMapping() throws Exception {
        FileObject def1 = make("Actions/DummyAction1.instance");
        def1.setAttribute("instanceCreate", new DummyAction("one"));
        FileObject def2 = make("Actions/DummyAction2.instance");
        def2.setAttribute("instanceCreate", new DummyAction("two"));
        FileObject def3 = make("Actions/DummySystemAction1.instance");
        def3.setAttribute("instanceClass", DummySystemAction1.class.getName());
        FileObject def4 = make("Actions/" + DummySystemAction2.class.getName().replace('.', '-') + ".instance");
        DataFolder shortcuts = DataFolder.findFolder(makeFolder("Shortcuts"));
        DataShadow.create(shortcuts, "1", DataObject.find(def1)).getPrimaryFile();
        DataShadow.create(shortcuts, "2", DataObject.find(def2)).getPrimaryFile();
        DataShadow.create(shortcuts, "3", DataObject.find(def3)).getPrimaryFile();
        DataShadow.create(shortcuts, "C-4", DataObject.find(def4)).getPrimaryFile();
        DataFolder menu = DataFolder.findFolder(makeFolder("Menu/Tools"));
        FileObject menuitem1 = DataShadow.create(menu, "whatever1", DataObject.find(def1)).getPrimaryFile();
        FileObject menuitem2 = DataShadow.create(menu, "whatever2", DataObject.find(def2)).getPrimaryFile();
        FileObject menuitem3 = DataShadow.create(menu, "whatever3", DataObject.find(def3)).getPrimaryFile();
        FileObject menuitem4 = DataShadow.create(menu, "whatever4", DataObject.find(def4)).getPrimaryFile();
        NbKeymap km = new NbKeymap();
        assertMapping(km, KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), menuitem1, "one");
        assertMapping(km, KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), menuitem2, "two");
        assertMapping(km, KeyStroke.getKeyStroke(KeyEvent.VK_3, 0), menuitem3, "DummySystemAction1");
        assertMapping(km, KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.CTRL_MASK), menuitem4, "DummySystemAction2");
    }

    public void testMultipleAcceleratorMapping() throws Exception {
        FileObject def = make("Actions/paste.instance");
        def.setAttribute("instanceCreate", new DummyAction("paste"));
        DataFolder shortcuts = DataFolder.findFolder(makeFolder("Shortcuts"));
        DataShadow.create(shortcuts, "C-V", DataObject.find(def));
        DataShadow.create(shortcuts, "PASTE", DataObject.find(def));
        NbKeymap km = new NbKeymap();
        assertMapping(km, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK), def, "paste");
    }

    public void testKeymapOverride() throws Exception { // #170677
        FileObject def1 = make("Actions/start.instance");
        def1.setAttribute("instanceCreate", new DummyAction("start"));
        FileObject def2 = make("Actions/continue.instance");
        def2.setAttribute("instanceCreate", new DummyAction("continue"));
        DataFolder shortcuts = DataFolder.findFolder(makeFolder("Shortcuts"));
        DataShadow.create(shortcuts, "F5", DataObject.find(def1));
        DataShadow.create(shortcuts, "C-F5", DataObject.find(def2));
        DataFolder netbeans = DataFolder.findFolder(makeFolder("Keymaps/NetBeans"));
        DataShadow.create(netbeans, "C-F5", DataObject.find(def1));
        DataShadow.create(netbeans, "F5", DataObject.find(def2));
        NbKeymap km = new NbKeymap();
        assertMapping(km, KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK), def1, "start");
        assertMapping(km, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), def2, "continue");
    }
    
    public void testUnusualInstanceFileExtensions() throws Exception {
        MockServices.setServices(ENV.class);
        FileObject inst = make("Shortcuts/C-F11.xml");
        OutputStream os = inst.getOutputStream();
        os.write("<action/>".getBytes());
        os.close();
        assertMapping(new NbKeymap(), KeyStroke.getKeyStroke(KeyEvent.VK_F11, KeyEvent.CTRL_MASK), inst, "whatever");
    }

    public void testAbstractModifiers() throws Exception {
        NbKeymap km = new NbKeymap();
        FileObject inst1 = make("Shortcuts/D-1.instance");
        inst1.setAttribute("instanceCreate", new DummyAction("one"));
        FileObject inst2 = make("Shortcuts/O-1.instance");
        inst2.setAttribute("instanceCreate", new DummyAction("two"));
        assertMapping(km, KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_MASK), inst1, "one");
        assertMapping(km, KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.ALT_MASK), inst2, "two");
    }

    public void testDifferentKeymaps() throws Exception {
        make("Shortcuts/C-A.instance").setAttribute("instanceCreate", new DummyAction("one"));
        make("Keymaps/NetBeans/C-A.instance").setAttribute("instanceCreate", new DummyAction("two"));
        make("Keymaps/Eclipse/C-A.instance").setAttribute("instanceCreate", new DummyAction("three"));
        Keymap km = new NbKeymap();
        KeyStroke controlA = KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK);
        assertEquals("two", km.getAction(controlA).getValue(Action.NAME));
        FileUtil.getConfigFile("Keymaps").setAttribute("currentKeymap", "Eclipse");
        assertEquals("three", km.getAction(controlA).getValue(Action.NAME));
        FileUtil.getConfigFile("Keymaps").setAttribute("currentKeymap", "IDEA");
        assertEquals("one", km.getAction(controlA).getValue(Action.NAME));
    }

    public void testChangeOfAcceleratorFromKeymap() throws Exception {
        Action a = new DummyAction("one");
        FileObject def = make("Actions/one.instance");
        def.setAttribute("instanceCreate", a);
        DataShadow.create(DataFolder.findFolder(makeFolder("Keymaps/NetBeans")), "C-A", DataObject.find(def));
        DataShadow.create(DataFolder.findFolder(makeFolder("Keymaps/Eclipse")), "C-B", DataObject.find(def));
        NbKeymap km = new NbKeymap();
        assertMapping(km, KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK), def, "one");
        assertEquals(null, a.getValue(Action.ACCELERATOR_KEY));
        FileUtil.getConfigFile("Keymaps").setAttribute("currentKeymap", "Eclipse");
        // let former EQ task finish
        EventQueue.invokeAndWait(new Runnable() {public void run() {}});
        // Any actions ever passed to getKeyStrokesForAction should get updated when keymap changes:
        assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK), a.getValue(Action.ACCELERATOR_KEY));
    }

    public void testMultiKeyShortcuts() throws Exception {
        final AtomicReference<String> ran = new AtomicReference<String>();
        class A extends AbstractAction {
            final String s;
            A(String s) {
                this.s = s;
            }
            public void actionPerformed(ActionEvent e) {
                ran.set(s);
            }
        }
        make("Shortcuts/C-X 1.instance").setAttribute("instanceCreate", new A("C-X 1"));
        make("Shortcuts/C-X 2.instance").setAttribute("instanceCreate", new A("C-X 2"));
        make("Shortcuts/C-U A B.instance").setAttribute("instanceCreate", new A("C-U A B"));
        Keymap km = new NbKeymap();
        Action a = km.getAction(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
        assertNotNull(a);
        a.actionPerformed(null);
        assertEquals(null, ran.getAndSet(null));
        a = km.getAction(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0));
        assertNotNull(a);
        a.actionPerformed(null);
        assertEquals("C-X 1", ran.getAndSet(null));
        a = km.getAction(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
        assertNotNull(a);
        a.actionPerformed(null);
        assertEquals(null, ran.getAndSet(null));
        a = km.getAction(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0));
        assertNotNull(a);
        a.actionPerformed(null);
        assertEquals("C-X 2", ran.getAndSet(null));
        a = km.getAction(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK));
        assertNotNull(a);
        a.actionPerformed(null);
        assertEquals(null, ran.getAndSet(null));
        a = km.getAction(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
        assertNotNull(a);
        a.actionPerformed(null);
        assertEquals(null, ran.getAndSet(null));
        a = km.getAction(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0));
        assertNotNull(a);
        a.actionPerformed(null);
        assertEquals("C-U A B", ran.getAndSet(null));
    }

    public void testChangesInShortcutRegistrations() throws Exception {
        make("Shortcuts/C-A.instance").setAttribute("instanceCreate", new DummyAction("one"));
        make("Keymaps/NetBeans/C-B.instance").setAttribute("instanceCreate", new DummyAction("two"));
        Keymap km = new NbKeymap();
        assertEquals("one", km.getAction(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK)).getValue(Action.NAME));
        assertEquals("two", km.getAction(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK)).getValue(Action.NAME));
        assertEquals(null, km.getAction(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK)));
        FileUtil.getConfigFile("Shortcuts/C-A.instance").delete();
        assertEquals(null, km.getAction(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK)));
        make("Shortcuts/C-C.instance").setAttribute("instanceCreate", new DummyAction("three"));
        assertEquals("three", km.getAction(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK)).getValue(Action.NAME));
        make("Keymaps/NetBeans/C-C.instance").setAttribute("instanceCreate", new DummyAction("four"));
        assertEquals("four", km.getAction(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK)).getValue(Action.NAME));
    }

    public void testBrokenShadow() throws Exception { // #169887
        FileObject def = make("Menu/x.shadow");
        def.setAttribute("originalFile", "Action/nonexistent.instance");
        Action a = new DummyAction("x");
        new NbKeymap().keyStrokeForAction(a, def);
    }

    private static final class DummyAction extends AbstractAction {
        public DummyAction(String name) {
            super(name);
        }
        public void actionPerformed(ActionEvent e) {}
    }
    
    public static class ENV extends Object implements Environment.Provider {
        public Lookup getEnvironment(DataObject obj) {
            if (obj instanceof XMLDataObject) {
                try {
                    Document doc = ((XMLDataObject) obj).getDocument();
                    if (doc.getDocumentElement().getNodeName().equals("action")) {
                        return Lookups.singleton(new InstanceSupport.Instance(new DummyAction("whatever")));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    fail("No exception: " + ex.getMessage());
                }
            }
            return Lookup.EMPTY;
        }
    }

    public static final class DummySystemAction1 extends CallableSystemAction {
        public void performAction() {}
        public String getName() {
            return "DummySystemAction1";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
    }

    public static final class DummySystemAction2 extends CallableSystemAction {
        public void performAction() {}
        public String getName() {
            return "DummySystemAction2";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
    }

}
